package com.example.data.audio

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.playlistsDataStore by preferencesDataStore(name = "quran_audio_playlists_store")

class AudioPlaylistRepository(private val context: Context) {
    private val customPlaylistsKey = stringPreferencesKey("playlists_json")
    private val hiddenPresetsKey = stringPreferencesKey("hidden_presets_json")

    val playlistsFlow: Flow<List<AudioPlaylist>> = context.playlistsDataStore.data.map { prefs ->
        mergePlaylists(
            stored = decodePlaylists(prefs[customPlaylistsKey].orEmpty()),
            hiddenPresetIds = decodeStringSet(prefs[hiddenPresetsKey].orEmpty())
        )
    }

    val hiddenPresetsCountFlow: Flow<Int> = context.playlistsDataStore.data.map { prefs ->
        decodeStringSet(prefs[hiddenPresetsKey].orEmpty()).size
    }

    suspend fun upsertPlaylist(playlist: AudioPlaylist) {
        context.playlistsDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[customPlaylistsKey].orEmpty()).toMutableList()
            val idx = current.indexOfFirst { it.id == playlist.id }
            if (idx >= 0) current[idx] = playlist else current.add(0, playlist)
            prefs[customPlaylistsKey] = encodePlaylists(current)
            if (playlist.isPreset) {
                val hidden = decodeStringSet(prefs[hiddenPresetsKey].orEmpty()) - playlist.id
                prefs[hiddenPresetsKey] = encodeStringSet(hidden)
            }
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        context.playlistsDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[customPlaylistsKey].orEmpty()).filterNot { it.id == playlistId }
            prefs[customPlaylistsKey] = encodePlaylists(current)
            if (PresetAudioPlaylists.all.any { it.id == playlistId }) {
                val hidden = decodeStringSet(prefs[hiddenPresetsKey].orEmpty()) + playlistId
                prefs[hiddenPresetsKey] = encodeStringSet(hidden)
            }
        }
    }

    suspend fun updatePlaylistById(playlistId: String, transform: (AudioPlaylist) -> AudioPlaylist) {
        context.playlistsDataStore.edit { prefs ->
            val stored = decodePlaylists(prefs[customPlaylistsKey].orEmpty()).toMutableList()
            val hidden = decodeStringSet(prefs[hiddenPresetsKey].orEmpty())
            val merged = mergePlaylists(stored, hidden)
            val target = merged.firstOrNull { it.id == playlistId } ?: return@edit
            val updated = transform(target)
            val idx = stored.indexOfFirst { it.id == playlistId }
            if (idx >= 0) stored[idx] = updated else stored.add(0, updated)
            prefs[customPlaylistsKey] = encodePlaylists(stored)
        }
    }

    suspend fun restorePresetPlaylists() {
        context.playlistsDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[customPlaylistsKey].orEmpty()).filterNot { it.isPreset }
            prefs[customPlaylistsKey] = encodePlaylists(current)
            prefs[hiddenPresetsKey] = "[]"
        }
    }

    suspend fun exportJson(): JSONObject {
        val prefs = context.playlistsDataStore.data.first()
        return JSONObject().apply {
            put("playlists", JSONArray(prefs[customPlaylistsKey].orEmpty().ifBlank { "[]" }))
            put("hiddenPresets", JSONArray(prefs[hiddenPresetsKey].orEmpty().ifBlank { "[]" }))
        }
    }

    suspend fun importJson(obj: JSONObject) {
        val playlistsArr = obj.optJSONArray("playlists")?.toString().orEmpty()
        val hiddenArr = obj.optJSONArray("hiddenPresets")?.toString().orEmpty()
        val parsedPlaylists = decodePlaylists(playlistsArr)
        val parsedHidden = decodeStringSet(hiddenArr)
        context.playlistsDataStore.edit { prefs ->
            prefs[customPlaylistsKey] = encodePlaylists(parsedPlaylists)
            prefs[hiddenPresetsKey] = encodeStringSet(parsedHidden)
        }
    }

    companion object {
        fun mergePlaylists(stored: List<AudioPlaylist>, hiddenPresetIds: Set<String>): List<AudioPlaylist> {
            val storedById = stored.associateBy { it.id }
            val customOnly = stored.filterNot { item ->
                item.isPreset || PresetAudioPlaylists.all.any { it.id == item.id }
            }
            val activePresets = PresetAudioPlaylists.all
                .filterNot { it.id in hiddenPresetIds }
                .map { preset -> storedById[preset.id]?.copy(isPreset = true) ?: preset }
            return customOnly + activePresets
        }

        fun encodePlaylists(list: List<AudioPlaylist>): String {
            val arr = JSONArray()
            for (p in list) {
                val tracksArr = JSONArray()
                for (t in p.tracks) {
                    tracksArr.put(
                        JSONObject().apply {
                            put("id", t.id)
                            put("surah", t.surahNumber)
                            put("start", t.startAyah)
                            put("end", t.endAyah)
                            if (!t.labelAr.isNullOrBlank()) put("label", t.labelAr)
                        }
                    )
                }
                arr.put(
                    JSONObject().apply {
                        put("id", p.id)
                        put("title", p.titleAr)
                        put("subtitle", p.subtitleAr)
                        put("icon", p.iconKey.id)
                        put("accent", p.accentIndex)
                        put("preset", p.isPreset)
                        put("repeat", p.repeatPlaylist)
                        put("shuffle", p.shuffleEnabled)
                        if (!p.preferredReciterId.isNullOrBlank()) put("reciterId", p.preferredReciterId)
                        put("ts", p.createdAtMillis)
                        put("tracks", tracksArr)
                    }
                )
            }
            return arr.toString()
        }

        fun decodePlaylists(raw: String): List<AudioPlaylist> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                buildList(arr.length()) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
                        val title = obj.optString("title").takeIf { it.isNotBlank() } ?: continue
                        val tracksArr = obj.optJSONArray("tracks") ?: JSONArray()
                        val tracks = buildList(tracksArr.length()) {
                            for (j in 0 until tracksArr.length()) {
                                val tObj = tracksArr.optJSONObject(j) ?: continue
                                val surah = tObj.optInt("surah", 1).coerceIn(1, 114)
                                val start = tObj.optInt("start", 1).coerceAtLeast(1)
                                val end = tObj.optInt("end", start).coerceAtLeast(start)
                                add(
                                    AudioPlaylistTrack(
                                        id = tObj.optString("id").ifBlank { "${id}_t_$j" },
                                        surahNumber = surah,
                                        startAyah = start,
                                        endAyah = end,
                                        labelAr = tObj.optString("label").takeIf { it.isNotBlank() }
                                    )
                                )
                            }
                        }
                        add(
                            AudioPlaylist(
                                id = id,
                                titleAr = title,
                                subtitleAr = obj.optString("subtitle", ""),
                                iconKey = PlaylistIconKey.fromId(obj.optString("icon")),
                                accentIndex = obj.optInt("accent", 0),
                                isPreset = obj.optBoolean("preset", false),
                                repeatPlaylist = obj.optBoolean("repeat", false),
                                shuffleEnabled = obj.optBoolean("shuffle", false),
                                preferredReciterId = obj.optString("reciterId").takeIf { it.isNotBlank() },
                                tracks = tracks,
                                createdAtMillis = obj.optLong("ts", 0L)
                            )
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }

        private fun encodeStringSet(values: Set<String>): String = JSONArray(values.toList()).toString()

        private fun decodeStringSet(raw: String): Set<String> {
            if (raw.isBlank()) return emptySet()
            return runCatching {
                val arr = JSONArray(raw)
                buildSet {
                    for (i in 0 until arr.length()) {
                        arr.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                    }
                }
            }.getOrDefault(emptySet())
        }
    }
}
