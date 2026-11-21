package com.example

import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistPlayerHelper
import com.example.data.audio.AudioPlaylistRepository
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.audio.PlaylistIconKey
import com.example.data.audio.PresetAudioPlaylists
import com.example.domain.model.ReciterOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ws.quran.qurantext.Mushaf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class AudioPlaylistsTest {

    private val surahMetaByNum: Map<Int, Pair<String, Int>> = Mushaf.hafs().surahs.associate { it.number to (it.nameAr to it.ayahCount) }

    @Test
    fun presetPlaylistsAndFamousPassages_haveValidSurahAndAyahBounds() {
        assertEquals(5, PresetAudioPlaylists.all.size)
        PresetAudioPlaylists.all.forEach { playlist ->
            assertTrue("Preset ${playlist.id} must have tracks", playlist.tracks.isNotEmpty())
            assertTrue(playlist.isPreset)
            playlist.tracks.forEach { track ->
                val meta = surahMetaByNum[track.surahNumber]
                assertNotNull("Unknown surah ${track.surahNumber} in ${playlist.id}", meta)
                val maxAyah = meta!!.second
                assertTrue("Invalid startAyah in ${playlist.id}: ${track.startAyah}", track.startAyah in 1..maxAyah)
                assertTrue("Invalid endAyah in ${playlist.id}: ${track.endAyah} > $maxAyah", track.endAyah in track.startAyah..maxAyah)
            }
        }
        PresetAudioPlaylists.famousPassages.forEach { passage ->
            val maxAyah = surahMetaByNum[passage.surahNumber]!!.second
            assertTrue(passage.startAyah in 1..passage.endAyah)
            assertTrue(passage.endAyah <= maxAyah)
        }
    }

    @Test
    fun playlistRepository_roundTripsJsonAndMergesHiddenAndEditedPresets() {
        val custom = AudioPlaylist(
            id = "custom_1",
            titleAr = "مختارات الفجر",
            subtitleAr = "قراءة يومية",
            iconKey = PlaylistIconKey.MOSQUE,
            accentIndex = 2,
            isPreset = false,
            repeatPlaylist = true,
            shuffleEnabled = false,
            preferredReciterId = "Alafasy_128kbps",
            tracks = listOf(
                AudioPlaylistTrack("t1", surahNumber = 50, startAyah = 1, endAyah = 45),
                AudioPlaylistTrack("t2", surahNumber = 2, startAyah = 255, endAyah = 255, labelAr = "آية الكرسي")
            ),
            createdAtMillis = 123456L
        )
        val editedPreset = PresetAudioPlaylists.all.first().copy(repeatPlaylist = true)

        val encoded = AudioPlaylistRepository.encodePlaylists(listOf(custom, editedPreset))
        val decoded = AudioPlaylistRepository.decodePlaylists(encoded)
        assertEquals(2, decoded.size)
        assertEquals(custom.titleAr, decoded[0].titleAr)
        assertEquals(PlaylistIconKey.MOSQUE, decoded[0].iconKey)
        assertEquals("Alafasy_128kbps", decoded[0].preferredReciterId)
        assertEquals("آية الكرسي", decoded[0].tracks[1].labelAr)

        val hidden = setOf("preset_ruqyah", "preset_short")
        val merged = AudioPlaylistRepository.mergePlaylists(decoded, hidden)
        assertEquals(custom.id, merged.first().id)
        assertFalse(merged.any { it.id == "preset_ruqyah" })
        assertFalse(merged.any { it.id == "preset_short" })
        assertTrue(merged.first { it.id == editedPreset.id }.repeatPlaylist)
        assertEquals(4, merged.size)
    }

    @Test
    fun playlistPlayerHelper_advancesAyahsAndTransitionsAcrossTracksAndRepeat() {
        val playlist = AudioPlaylist(
            id = "pl_test",
            titleAr = "اختبار",
            repeatPlaylist = false,
            tracks = listOf(
                AudioPlaylistTrack("t1", surahNumber = 2, startAyah = 285, endAyah = 286),
                AudioPlaylistTrack("t2", surahNumber = 112, startAyah = 1, endAyah = 2)
            )
        )
        val session0 = AudioPlaylistPlayerHelper.buildSession(playlist, 0) { surahMetaByNum[it] ?: ("الفاتحة" to 7) }!!
        assertEquals(2, session0.totalTracks)
        assertTrue(session0.hasNext)
        assertFalse(session0.hasPrevious)

        val ayahState = AudioPlaybackState(isActive = true, currentSurah = 2, currentAyah = 285, playlistSession = session0)
        val step1 = AudioPlaylistPlayerHelper.onAyahCompleted(ayahState, session0)
        assertTrue(step1 is AudioPlaylistPlayerHelper.StepOutcome.PlayAyah)
        val play1 = step1 as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah
        assertEquals(2, play1.surah)
        assertEquals(286, play1.ayah)
        assertEquals(0, play1.updatedSession.currentTrackIndex)

        val step2 = AudioPlaylistPlayerHelper.onAyahCompleted(ayahState.copy(currentAyah = 286), play1.updatedSession)
        val play2 = step2 as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah
        assertEquals(112, play2.surah)
        assertEquals(1, play2.ayah)
        assertEquals(1, play2.updatedSession.currentTrackIndex)
        assertFalse(play2.updatedSession.hasNext)
        assertTrue(play2.updatedSession.hasPrevious)

        val endState = ayahState.copy(currentSurah = 112, currentAyah = 2, playlistSession = play2.updatedSession)
        val stepEnd = AudioPlaylistPlayerHelper.onAyahCompleted(endState, play2.updatedSession)
        assertEquals(AudioPlaylistPlayerHelper.StepOutcome.StopPlayback, stepEnd)

        val repeatingSession = play2.updatedSession.copy(repeatPlaylist = true)
        assertTrue(repeatingSession.hasNext)
        val stepWrap = AudioPlaylistPlayerHelper.onAyahCompleted(endState, repeatingSession)
        val playWrap = stepWrap as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah
        assertEquals(2, playWrap.surah)
        assertEquals(285, playWrap.ayah)
        assertEquals(0, playWrap.updatedSession.currentTrackIndex)
    }

    @Test
    fun playlistPlayerHelper_supportsSurahLevelReciterManualNavigationAndShuffleAnchor() {
        val playlist = AudioPlaylist(
            id = "pl_shuffle",
            titleAr = "عشوائي",
            shuffleEnabled = true,
            tracks = listOf(
                AudioPlaylistTrack("t1", surahNumber = 1, startAyah = 1, endAyah = 7),
                AudioPlaylistTrack("t2", surahNumber = 67, startAyah = 1, endAyah = 30),
                AudioPlaylistTrack("t3", surahNumber = 112, startAyah = 1, endAyah = 4)
            )
        )
        val shuffledSession = AudioPlaylistPlayerHelper.buildSession(playlist, 1) { surahMetaByNum[it] ?: ("الفاتحة" to 7) }!!
        assertEquals("t2", shuffledSession.currentTrack?.id)
        assertEquals(0, shuffledSession.currentTrackIndex)
        assertEquals(3, shuffledSession.tracks.map { it.id }.toSet().size)

        val surahReciter = ReciterOption(
            id = "surah_rec", nameAr = "قارئ سورة", styleAr = "مرتل",
            everyAyahFolder = "", isSurahLevelAudio = true, directBaseUrl = "https://example.com/s/"
        )
        val surahState = AudioPlaybackState(
            isActive = true, currentSurah = 67, currentAyah = 1,
            reciter = surahReciter, playlistSession = shuffledSession
        )
        val nextTrackOutcome = AudioPlaylistPlayerHelper.onAyahCompleted(surahState, shuffledSession)
        assertTrue(nextTrackOutcome is AudioPlaylistPlayerHelper.StepOutcome.PlayAyah)
        assertEquals(1, (nextTrackOutcome as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah).updatedSession.currentTrackIndex)

        val manualBack = AudioPlaylistPlayerHelper.onManualPreviousAyah(
            surahState.copy(currentSurah = nextTrackOutcome.surah, currentAyah = nextTrackOutcome.ayah),
            nextTrackOutcome.updatedSession
        )
        assertNotNull(manualBack)
        assertEquals(0, (manualBack as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah).updatedSession.currentTrackIndex)

        val atVeryStartNoRepeat = AudioPlaybackState(isActive = true, currentSurah = 67, currentAyah = 1, playlistSession = shuffledSession)
        val nextWithin = AudioPlaylistPlayerHelper.onManualNextAyah(atVeryStartNoRepeat, shuffledSession) as AudioPlaylistPlayerHelper.StepOutcome.PlayAyah
        assertEquals(2, nextWithin.ayah)
        assertNull(AudioPlaylistPlayerHelper.onManualNextAyah(
            AudioPlaybackState(isActive = true, currentSurah = 112, currentAyah = 4),
            shuffledSession.copy(currentTrackIndex = 2, tracks = listOf(shuffledSession.tracks[0], shuffledSession.tracks[1], AudioPlaylistTrack("t3", 112, 1, 4)))
        ))
    }
}
