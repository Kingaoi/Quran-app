package com.example.domain.quran

import androidx.compose.runtime.Immutable
import com.example.domain.model.AyahItem

enum class WaqfMarkType(
    val unicodeChar: Char?,
    val symbolGlyph: String,
    val badgeShort: String,
    val titleAr: String,
    val rulingAr: String,
    val explanationAr: String,
    val recitationTipAr: String
) {
    LAZIM(
        unicodeChar = '\u06D8',
        symbolGlyph = "مـ",
        badgeShort = "لازم",
        titleAr = "الوقف اللازم (مـ)",
        rulingAr = "يلزم الوقف",
        explanationAr = "علامة تدل على وجوب الوقف عندها وابتداء ما بعدها، لأن وصلها بما بعدها يوهم معنىً غير مراد.",
        recitationTipAr = "قف عند الكلمة التي تسبق العلامة ثم استأنف القراءة بعدها."
    ),
    AL_WAQF_AWLA(
        unicodeChar = '\u06D7',
        symbolGlyph = "قلى",
        badgeShort = "الوقف أولى",
        titleAr = "الوقف أولى (قلى)",
        rulingAr = "جائز والوقف أولى",
        explanationAr = "يجوز عندها الوصل والوقف، إلا أن الوقف أتمّ للمعنى وأولى في الأداء.",
        recitationTipAr = "يُستحسن الوقف هنا مع إتمام المعنى قبل المتابعة."
    ),
    AL_WASL_AWLA(
        unicodeChar = '\u06D6',
        symbolGlyph = "صلى",
        badgeShort = "الوصل أولى",
        titleAr = "الوصل أولى (صلى)",
        rulingAr = "جائز والوصل أولى",
        explanationAr = "يجوز الوقف عند انقطاع النّفَس، غير أن وصل الآية بما بعدها أولى لتعلّق المعنى.",
        recitationTipAr = "استمر في القراءة دون وقف ما لم يضق بك النّفَس."
    ),
    JAIZ(
        unicodeChar = '\u06DA',
        symbolGlyph = "ج",
        badgeShort = "وقف جائز",
        titleAr = "الوقف الجائز (ج)",
        rulingAr = "مستوي الطرفين",
        explanationAr = "علامة الوقف الجائز جوازاً مستوي الطرفين؛ يستوي عندها الوقف والوصل في المعنى.",
        recitationTipAr = "اختر الوقف أو الوصل بحسب راحتك ونَفَسك."
    ),
    MAMNU(
        unicodeChar = '\u06D9',
        symbolGlyph = "لا",
        badgeShort = "ممنوع",
        titleAr = "الوقف الممنوع (لا)",
        rulingAr = "يُنهى عن الوقف",
        explanationAr = "تدل على تعلّق الكلام بما بعده لفظاً ومعنىً، فلا يوقف عندها عمداً لأن المعنى لم يكتمل.",
        recitationTipAr = "تابِع القراءة ولا تقف؛ وإن اضطررت لوقوف فابدأ من كلمة مناسبة قبلها."
    ),
    MUANAQAH(
        unicodeChar = '\u06DB',
        symbolGlyph = "∴ ∴",
        badgeShort = "معانقة",
        titleAr = "وقف المعانقة (∴ ∴)",
        rulingAr = "الوقف على أحد الموضعين",
        explanationAr = "تأتيان متقاربتين؛ فإن وقفت على الأولى وصلت الثانية، وإن وصلت الأولى وقفت على الثانية.",
        recitationTipAr = "لا تقف على الموضعين معاً، واختر أحدهما فقط."
    ),
    SAKT(
        unicodeChar = '\u06DC',
        symbolGlyph = "س",
        badgeShort = "سكتة",
        titleAr = "السكتة اللطيفة (س)",
        rulingAr = "سكت بلا تنفّس",
        explanationAr = "تدل على قطع الصوت زمناً يسيراً (نحو حركتين) من غير تنفّس ثم متابعة القراءة.",
        recitationTipAr = "اسكت سكتة خفيفة دون أخذ نَفَس جديد ثم أكمل."
    ),
    SAJDAH(
        unicodeChar = '\u06E9',
        symbolGlyph = "۩",
        badgeShort = "سجدة",
        titleAr = "موضع سجدة التلاوة (۩)",
        rulingAr = "سجود تلاوة",
        explanationAr = "علامة تشير إلى آية فيها سجدة تلاوة؛ يُسنّ عند تلاوتها أو سماعها السجود لله تعالى.",
        recitationTipAr = "كبّر واسجد سجدة واحدة للتلاوة وسبّح ثم قم."
    ),
    HIZB_QUARTER(
        unicodeChar = '\u06DE',
        symbolGlyph = "۞",
        badgeShort = "ربع حزب",
        titleAr = "بداية الربع / الحزب (۞)",
        rulingAr = "علامة تقسيم",
        explanationAr = "نجمة ثمانية تشير إلى بداية ربع الحزب أو نصفه أو الحزب لتنظيم القراءة والحفظ.",
        recitationTipAr = "تفيد في تحديد مواضع الأوراد ومقدار المراجعة."
    )
}

@Immutable
data class DetectedWaqfMark(
    val type: WaqfMarkType,
    val snippet: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameAr: String
)

object QuranWaqfCatalog {

    private val charToType: Map<Char, WaqfMarkType> = WaqfMarkType.entries
        .mapNotNull { t -> t.unicodeChar?.let { it to t } }
        .toMap()

    fun analyzeAyah(ayah: AyahItem): List<DetectedWaqfMark> {
        val text = ayah.text
        if (text.isBlank()) return emptyList()

        val found = mutableListOf<DetectedWaqfMark>()
        val seenTypes = mutableSetOf<WaqfMarkType>()
        var muanaqahCount = 0

        text.forEachIndexed { idx, ch ->
            val type = charToType[ch] ?: return@forEachIndexed
            if (type == WaqfMarkType.MUANAQAH) {
                muanaqahCount++
                if (muanaqahCount > 1) return@forEachIndexed
            } else if (!seenTypes.add(type)) {
                return@forEachIndexed
            }
            val snippet = extractSnippetAround(text, idx)
            found.add(
                DetectedWaqfMark(
                    type = type,
                    snippet = snippet,
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.ayahNumber,
                    surahNameAr = ayah.surahNameAr
                )
            )
        }

        if (ayah.hasSajdah && WaqfMarkType.SAJDAH !in seenTypes) {
            found.add(
                DetectedWaqfMark(
                    type = WaqfMarkType.SAJDAH,
                    snippet = ayah.text.takeLast(36).trim(),
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.ayahNumber,
                    surahNameAr = ayah.surahNameAr
                )
            )
        }
        return found
    }

    fun analyzePage(pageAyahs: List<AyahItem>): List<DetectedWaqfMark> =
        pageAyahs.flatMap { analyzeAyah(it) }

    fun countMarksInAyah(ayah: AyahItem): Int = analyzeAyah(ayah).size

    private fun extractSnippetAround(text: String, markIndex: Int): String {
        val wordsBefore = text.substring(0, markIndex).trimEnd().split(' ').takeLast(3)
        val afterPart = text.substring((markIndex + 1).coerceAtMost(text.length)).trimStart()
        val wordsAfter = if (afterPart.isBlank()) emptyList() else afterPart.split(' ').take(2)
        val ch = text[markIndex]
        return buildString {
            if (wordsBefore.isNotEmpty()) append(wordsBefore.joinToString(" "))
            append(' ')
            append(ch)
            if (wordsAfter.isNotEmpty()) {
                append(' ')
                append(wordsAfter.joinToString(" "))
            }
        }.trim()
    }
}
