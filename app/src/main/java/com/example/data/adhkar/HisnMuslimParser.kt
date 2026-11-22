package com.example.data.adhkar

import com.example.data.repository.QuranRepository
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import org.json.JSONArray
import org.json.JSONObject

object HisnMuslimParser {

    fun normalizeHttpsUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://", ignoreCase = true)) {
            "https://${trimmed.substring(7)}"
        } else {
            trimmed
        }
    }

    fun parseIndex(rawJson: String): List<HisnChapterEntry> {
        val clean = rawJson.trimStart('\uFEFF').trim()
        if (clean.isBlank()) return emptyList()

        val array = extractRootArray(clean) ?: return emptyList()
        return buildList(array.length()) {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optInt("ID", obj.optInt("id", i + 1))
                val title = firstNonBlank(obj, "TITLE", "title", "CATEGORY", "category")
                val textUrl = normalizeHttpsUrl(firstNonBlank(obj, "TEXT", "text", "url"))
                val audioUrl = normalizeHttpsUrl(firstNonBlank(obj, "AUDIO_URL", "audio_url", "audio"))
                if (title.isNotBlank() && textUrl.isNotBlank()) {
                    add(HisnChapterEntry(id = id, title = title, textUrl = textUrl, audioUrl = audioUrl))
                }
            }
        }
    }

    fun parseChapterItems(
        rawJson: String,
        chapter: HisnChapterEntry,
        category: DhikrCategory
    ): List<DhikrItem> {
        val clean = rawJson.trimStart('\uFEFF').trim()
        if (clean.isBlank()) return emptyList()

        val array = extractRootArray(clean, preferredKey = chapter.title) ?: return emptyList()
        val result = mutableListOf<DhikrItem>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val text = firstNonBlank(obj, "ARABIC_TEXT", "zekr", "TEXT", "arabic_text", "text")
                .trim()
            if (text.isBlank()) continue

            val rawId = obj.optInt("ID", obj.optInt("id", i + 1))
            val repeat = obj.optInt("REPEAT", obj.optInt("repeat", 1)).coerceAtLeast(1)
            val scopedLabel = resolveScopedChapterLabel(category, chapter.title)
            val rawNote = firstNonBlank(
                obj,
                "LANGUAGE_ARABIC_TRANSLATED_TEXT",
                "TRANSLATED_TEXT",
                "reference",
                "bless",
                "description"
            ).ifBlank { "من $scopedLabel" }
            val itemAudio = normalizeHttpsUrl(
                firstNonBlank(obj, "AUDIO", "audio", "AUDIO_URL", "audio_url")
            ).ifBlank { chapter.audioUrl }

            val baseId = "hisn_${category.id}_${chapter.id}_$rawId"
            val splitTasbih = trySplitCombinedTasbih(baseId, category, scopedLabel, text, repeat, rawNote, itemAudio)
            if (splitTasbih != null) {
                result.addAll(splitTasbih)
            } else {
                result.add(
                    DhikrItem(
                        id = baseId,
                        category = category,
                        title = buildConciseTitle(scopedLabel, text, i + 1),
                        arabicText = text,
                        virtueOrNote = rawNote,
                        targetCount = repeat,
                        reference = "حصن المسلم • $scopedLabel",
                        audioUrl = itemAudio,
                        chapterTitle = scopedLabel
                    )
                )
            }
        }
        return result
    }

    fun encodeCache(cachedAtMillis: Long, map: Map<DhikrCategory, List<DhikrItem>>): String {
        val root = JSONObject()
        root.put("cachedAtMillis", cachedAtMillis)
        root.put("sourceUrl", HISN_MUSLIM_INDEX_URL)
        val catsObj = JSONObject()
        for ((cat, items) in map) {
            val arr = JSONArray()
            for (item in items) {
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("arabicText", item.arabicText)
                        put("virtueOrNote", item.virtueOrNote)
                        put("targetCount", item.targetCount)
                        put("reference", item.reference)
                        if (item.chapterTitle.isNotBlank()) put("chapterTitle", item.chapterTitle)
                        if (item.audioUrl.isNotBlank()) put("audioUrl", item.audioUrl)
                    }
                )
            }
            catsObj.put(cat.id, arr)
        }
        root.put("categories", catsObj)
        return root.toString()
    }

    fun decodeCache(rawJson: String): CachedHisnCatalog? = runCatching {
        val clean = rawJson.trimStart('\uFEFF').trim()
        if (clean.isBlank()) return@runCatching null
        val root = JSONObject(clean)
        val cachedAt = root.optLong("cachedAtMillis", 0L)
        val catsObj = root.optJSONObject("categories") ?: return@runCatching null
        val map = linkedMapOf<DhikrCategory, List<DhikrItem>>()
        for (cat in DhikrCategory.entries) {
            val arr = catsObj.optJSONArray(cat.id) ?: continue
            val list = buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val text = obj.optString("arabicText")
                    if (id.isNotBlank() && text.isNotBlank()) {
                        val rawChapter = obj.optString("chapterTitle", "")
                        val scopedChapter = resolveScopedChapterLabel(cat, rawChapter)
                        val rawTitle = obj.optString("title", cat.titleAr)
                        val hasNumericSuffix = Regex("""\(\d+\)$""").containsMatchIn(rawTitle.trim())
                        val cleanTitle = if (rawTitle.contains("الصباح والمساء") || hasNumericSuffix) {
                            buildConciseTitle(scopedChapter.ifBlank { cat.titleAr }, text, i + 1)
                        } else {
                            rawTitle
                        }
                        val rawRef = obj.optString("reference", "حصن المسلم")
                        val cleanRef = if ((cat == DhikrCategory.MORNING || cat == DhikrCategory.EVENING) && rawRef.contains("الصباح والمساء")) {
                            "حصن المسلم • ${cat.titleAr}"
                        } else {
                            rawRef
                        }
                        val rawNote = obj.optString("virtueOrNote")
                        val cleanNote = if ((cat == DhikrCategory.MORNING || cat == DhikrCategory.EVENING) && rawNote.contains("الصباح والمساء")) {
                            "من ${cat.titleAr}"
                        } else {
                            rawNote
                        }
                        add(
                            DhikrItem(
                                id = id,
                                category = cat,
                                title = cleanTitle,
                                arabicText = text,
                                virtueOrNote = cleanNote,
                                targetCount = obj.optInt("targetCount", 1).coerceAtLeast(1),
                                reference = cleanRef,
                                audioUrl = obj.optString("audioUrl", ""),
                                chapterTitle = scopedChapter
                            )
                        )
                    }
                }
            }
            if (list.isNotEmpty()) map[cat] = list
        }
        if (map.isEmpty()) null else CachedHisnCatalog(cachedAt, map)
    }.getOrNull()

    private fun extractRootArray(cleanJson: String, preferredKey: String? = null): JSONArray? {
        if (cleanJson.startsWith("[")) return JSONArray(cleanJson)
        if (!cleanJson.startsWith("{")) return null
        val obj = JSONObject(cleanJson)
        if (!preferredKey.isNullOrBlank()) {
            obj.optJSONArray(preferredKey)?.let { return it }
        }
        for (candidate in listOf("العربية", "ar", "content", "data", "items")) {
            obj.optJSONArray(candidate)?.let { return it }
        }
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            obj.optJSONArray(k)?.let { return it }
        }
        return null
    }

    private fun firstNonBlank(obj: JSONObject, vararg keys: String): String {
        for (k in keys) {
            if (obj.has(k) && !obj.isNull(k)) {
                val v = obj.optString(k, "").trim()
                if (v.isNotBlank() && !v.equals("null", ignoreCase = true)) return v
            }
        }
        return ""
    }

    private fun resolveScopedChapterLabel(category: DhikrCategory, rawChapterTitle: String): String {
        val trimmed = rawChapterTitle.trim()
        return when {
            (category == DhikrCategory.MORNING || category == DhikrCategory.EVENING) &&
                trimmed.contains("الصباح والمساء") -> category.titleAr
            else -> trimmed
        }
    }

    internal fun buildConciseTitle(chapterTitle: String, arabicText: String, index: Int = 1): String {
        val cleanText = arabicText.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        val firstChunk = cleanText.split('،', '.', '؟', '(', ':').firstOrNull()?.trim().orEmpty()
        if (firstChunk.length in 6..42) return firstChunk
        if (cleanText.length <= 42 && cleanText.isNotEmpty()) return cleanText
        if (cleanText.isNotEmpty()) {
            val slice = cleanText.take(38)
            val lastSpace = slice.lastIndexOf(' ')
            val clipped = if (lastSpace >= 16) slice.substring(0, lastSpace) else slice
            return "${clipped.trimEnd('،', '.', ' ')}…"
        }
        return "$chapterTitle ($index)"
    }

    private fun trySplitCombinedTasbih(
        baseId: String,
        category: DhikrCategory,
        chapterTitle: String,
        arabicText: String,
        repeat: Int,
        note: String,
        audioUrl: String
    ): List<DhikrItem>? {
        val norm = QuranRepository.normalizeArabic(arabicText)
        val hasAllThree = norm.contains("سبحان الله") &&
            norm.contains("الحمد لله") &&
            norm.contains("الله اكبر")
        val isCount33or34 = repeat == 33 || repeat == 34 ||
            norm.contains("ثلاثين") || norm.contains("ثلاثا وثلاثين")
        if (!hasAllThree || !isCount33or34) return null

        val takbirTarget = if (category == DhikrCategory.SLEEP) 34 else 33
        val ref = "حصن المسلم • $chapterTitle"
        return listOf(
            DhikrItem("${baseId}_subhan", category, "التسبيح عند ${category.shortTitleAr}", "سُبْحَانَ اللَّهِ", note, 33, ref, audioUrl, chapterTitle = chapterTitle),
            DhikrItem("${baseId}_hamd", category, "التحميد عند ${category.shortTitleAr}", "الْحَمْدُ لِلَّهِ", note, 33, ref, audioUrl, chapterTitle = chapterTitle),
            DhikrItem("${baseId}_akbar", category, "التكبير عند ${category.shortTitleAr}", "اللَّهُ أَكْبَرُ", note, takbirTarget, ref, audioUrl, chapterTitle = chapterTitle)
        )
    }
}
