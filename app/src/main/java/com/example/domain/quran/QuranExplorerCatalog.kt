package com.example.domain.quran

import androidx.compose.runtime.Immutable
import com.example.domain.model.AyahItem

@Immutable
data class SajdahPositionInfo(
    val index: Int,
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val juzNumber: Int,
    val ayahText: String,
    val classificationAr: String
)

@Immutable
data class QuranTopicItem(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameAr: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val titleAr: String,
    val ayahText: String
)

@Immutable
data class QuranTopicGroup(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val items: List<QuranTopicItem>
)

private data class TopicRefSpec(
    val surah: Int,
    val ayah: Int,
    val titleAr: String
)

object QuranExplorerCatalog {

    fun buildSajdahPositions(allAyahs: List<AyahItem>): List<SajdahPositionInfo> {
        val sajdahAyahs = allAyahs.filter { it.hasSajdah }
        return sajdahAyahs.mapIndexed { idx, ayah ->
            val note = when (ayah.surahNumber to ayah.ayahNumber) {
                22 to 77 -> "موضع سجدة تلاوة (عند الشافعية والحنابلة)"
                38 to 24 -> "سجدة شكر وتلاوة عند الجمهور"
                else -> "سجدة تلاوة مؤكّدة"
            }
            SajdahPositionInfo(
                index = idx + 1,
                surahNumber = ayah.surahNumber,
                surahNameAr = ayah.surahNameAr,
                ayahNumber = ayah.ayahNumber,
                pageNumber = ayah.pageNumber,
                juzNumber = ayah.juzNumber,
                ayahText = ayah.text,
                classificationAr = note
            )
        }
    }

    fun buildTopicGroups(allAyahs: List<AyahItem>): List<QuranTopicGroup> {
        val lookup = allAyahs.associateBy { it.surahNumber to it.ayahNumber }
        return TOPIC_DEFINITIONS.map { (id, title, subtitle, refs) ->
            val resolved = refs.mapNotNull { spec ->
                val a = lookup[spec.surah to spec.ayah] ?: return@mapNotNull null
                QuranTopicItem(
                    surahNumber = a.surahNumber,
                    ayahNumber = a.ayahNumber,
                    surahNameAr = a.surahNameAr,
                    pageNumber = a.pageNumber,
                    juzNumber = a.juzNumber,
                    titleAr = spec.titleAr,
                    ayahText = a.text
                )
            }
            QuranTopicGroup(id = id, titleAr = title, subtitleAr = subtitle, items = resolved)
        }.filter { it.items.isNotEmpty() }
    }

    private data class TopicDef(
        val id: String,
        val titleAr: String,
        val subtitleAr: String,
        val refs: List<TopicRefSpec>
    )

    private val TOPIC_DEFINITIONS = listOf(
        TopicDef(
            id = "aqeedah",
            titleAr = "التوحيد والعظمة الإلهية",
            subtitleAr = "آيات جامعة في أصول الإيمان وأسماء الله الحسنى",
            refs = listOf(
                TopicRefSpec(2, 255, "آية الكرسي — أعظم آية في كتاب الله"),
                TopicRefSpec(112, 1, "سورة الإخلاص — توحيد الخالق وصفاته"),
                TopicRefSpec(59, 22, "خواتيم سورة الحشر — الأسماء الحسنى"),
                TopicRefSpec(24, 35, "آية النور — الله نور السماوات والأرض"),
                TopicRefSpec(3, 18, "شهادة التوحيد والقيام بالقسط"),
                TopicRefSpec(2, 285, "خاتمة البقرة — أركان الإيمان")
            )
        ),
        TopicDef(
            id = "dua",
            titleAr = "الأدعية القرآنية المأثورة",
            subtitleAr = "أدعية الأنبياء والمؤمنين في الرخاء والشدة",
            refs = listOf(
                TopicRefSpec(2, 201, "ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة"),
                TopicRefSpec(3, 8, "ربنا لا تزغ قلوبنا بعد إذ هديتنا"),
                TopicRefSpec(20, 25, "رب اشرح لي صدري ويسر لي أمري"),
                TopicRefSpec(21, 87, "دعاء ذي النون — لا إله إلا أنت سبحانك"),
                TopicRefSpec(21, 83, "دعاء أيوب عليه السلام بالشفاء والرحمة"),
                TopicRefSpec(46, 15, "دعاء الشكر وصلاح الذرية"),
                TopicRefSpec(2, 286, "دعاء رفع الحرج والمغفرة والنصر")
            )
        ),
        TopicDef(
            id = "sabr_sakina",
            titleAr = "السكينة والطمأنينة والصبر",
            subtitleAr = "آيات تثبيت القلب وتفريج الكرب وحسن التوكل",
            refs = listOf(
                TopicRefSpec(13, 28, "ألا بذكر الله تطمئن القلوب"),
                TopicRefSpec(94, 5, "فإن مع العسر يسراً"),
                TopicRefSpec(2, 152, "فاذكروني أذكركم واشكروا لي"),
                TopicRefSpec(2, 155, "الابتلاء وبشارة الصابرين"),
                TopicRefSpec(39, 53, "سعة رحمة الله وعدم القنوط"),
                TopicRefSpec(65, 3, "الكفاية لمن توكّل على الله")
            )
        ),
        TopicDef(
            id = "akhlaq",
            titleAr = "الأخلاق والبرّ والإحسان",
            subtitleAr = "وصايا قرآنية في بناء السلوك والأسرة والمجتمع",
            refs = listOf(
                TopicRefSpec(17, 23, "بر الوالدين والقول الكريم"),
                TopicRefSpec(25, 63, "صفات عباد الرحمن وتواضعهم"),
                TopicRefSpec(31, 17, "وصايا لقمان في الصلاة والاعتدال"),
                TopicRefSpec(49, 11, "حفظ اللسان ونهي التنابز والسخرية"),
                TopicRefSpec(3, 134, "الإنفاق وكظم الغيظ والعفو عن الناس"),
                TopicRefSpec(41, 34, "دفع السيئة بالتي هي أحسن")
            )
        ),
        TopicDef(
            id = "qasas",
            titleAr = "القصص القرآني والعِبر",
            subtitleAr = "مواقف ملهمة من قصص الأنبياء والصالحين",
            refs = listOf(
                TopicRefSpec(18, 10, "فتية الكهف والتجائهم إلى رحمة الله"),
                TopicRefSpec(12, 90, "ثبات يوسف عليه السلام وعاقبة التقوى"),
                TopicRefSpec(28, 24, "فزع موسى عليه السلام إلى ربه في مدين"),
                TopicRefSpec(21, 69, "نجاة إبراهيم عليه السلام من النار"),
                TopicRefSpec(27, 19, "شكر سليمان عليه السلام على النعم"),
                TopicRefSpec(12, 111, "العبرة لأولي الألباب في قصصهم")
            )
        ),
        TopicDef(
            id = "ahkam",
            titleAr = "أحكام وآداب المعاملات",
            subtitleAr = "توجيهات في العبادات والعدل والأمانة والإنفاق",
            refs = listOf(
                TopicRefSpec(2, 183, "فرضية الصيام وحكمته في التقوى"),
                TopicRefSpec(4, 58, "أداء الأمانات والحكم بالعدل"),
                TopicRefSpec(16, 90, "العدل والإحسان وإيتاء ذي القربى"),
                TopicRefSpec(2, 261, "مضاعفة أجر الصدقة والإنفاق"),
                TopicRefSpec(62, 9, "السعي إلى صلاة الجمعة وذكر الله"),
                TopicRefSpec(17, 53, "اختيار أحسن الألفاظ في التخاطب")
            )
        )
    )
}
