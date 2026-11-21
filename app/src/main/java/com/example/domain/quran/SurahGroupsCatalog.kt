package com.example.domain.quran

import androidx.compose.runtime.Immutable

@Immutable
data class SurahGroupInfo(
    val id: String,
    val titleAr: String,
    val categoryLabelAr: String,
    val descriptionAr: String,
    val surahNumbers: List<Int>
)

object SurahGroupsCatalog {

    private val classicalDivisions: List<SurahGroupInfo> = listOf(
        SurahGroupInfo(
            id = "fatihah_mother",
            titleAr = "فاتحة الكتاب وأم القرآن",
            categoryLabelAr = "أصل القرآن",
            descriptionAr = "السورة الجامعة لمقاصد القرآن الكريم وأساس الصلاة.",
            surahNumbers = listOf(1)
        ),
        SurahGroupInfo(
            id = "sab_al_tiwal",
            titleAr = "السبع الطوال",
            categoryLabelAr = "تقسيم المصحف",
            descriptionAr = "أطول سور القرآن الكريم في صدر المصحف، وتُعدّ الأنفال وبراءة قرينتين في تمام السبع.",
            surahNumbers = listOf(2, 3, 4, 5, 6, 7, 8, 9)
        ),
        SurahGroupInfo(
            id = "al_miun",
            titleAr = "المِئون",
            categoryLabelAr = "تقسيم المصحف",
            descriptionAr = "السور التي تلي السبع الطوال وتقارب آياتها المائة آية أو تزيد عليها.",
            surahNumbers = (10..32).toList()
        ),
        SurahGroupInfo(
            id = "al_mathani",
            titleAr = "المثاني",
            categoryLabelAr = "تقسيم المصحف",
            descriptionAr = "السور التي تلي المئين وتقل آياتها عن المائة، سُمّيت بذلك لأنها تُثنّى في القراءة أكثر من الطوال.",
            surahNumbers = (33..49).toList()
        ),
        SurahGroupInfo(
            id = "tiwal_mufassal",
            titleAr = "طوال المفصّل",
            categoryLabelAr = "تقسيم المفصّل",
            descriptionAr = "أول قسم من المفصّل يبدأ من سورة ق إلى سورة المرسلات، يغلب فيه تفصيل البعث والوعد والوعيد.",
            surahNumbers = (50..77).toList()
        ),
        SurahGroupInfo(
            id = "awsat_mufassal",
            titleAr = "أوساط المفصّل",
            categoryLabelAr = "تقسيم المفصّل",
            descriptionAr = "السور المتوسطة في المفصّل من سورة النبأ إلى سورة الليل، تُستحب قراءتها في الصلوات المتوسطة.",
            surahNumbers = (78..92).toList()
        ),
        SurahGroupInfo(
            id = "qisar_mufassal",
            titleAr = "قصار المفصّل",
            categoryLabelAr = "تقسيم المفصّل",
            descriptionAr = "السور القصار في خاتمة المصحف من سورة الضحى إلى سورة الناس، يكثر تردادها في الصلاة والحفظ.",
            surahNumbers = (93..114).toList()
        )
    )

    private val sisterAndThematicGroups: List<SurahGroupInfo> = listOf(
        SurahGroupInfo(
            id = "zahrawan",
            titleAr = "الزّهراوان",
            categoryLabelAr = "قرينتان مأثورتان",
            descriptionAr = "سورتا البقرة وآل عمران؛ ورد في الحديث الصحيح أنهما تأتيان يوم القيامة كغمامتين تحاجّان عن صاحبهما.",
            surahNumbers = listOf(2, 3)
        ),
        SurahGroupInfo(
            id = "qarinatan_anfal_tawbah",
            titleAr = "القرينتان (الأنفال وبراءة)",
            categoryLabelAr = "قرينتان في المصحف",
            descriptionAr = "جُمعتا معاً في الترتيب دون بسملة بينهما لتشابه موضوعهما في الجهاد والعهود وصفات المؤمنين والمنافقين.",
            surahNumbers = listOf(8, 9)
        ),
        SurahGroupInfo(
            id = "hamd_openers",
            titleAr = "السور المفتتحة بالحمد",
            categoryLabelAr = "تماثل المطلع",
            descriptionAr = "خمس سور تبدأ بـ «الحمد لله»؛ وتتناول حمد الله في الخلق والوحي والهداية وتدبير الكون.",
            surahNumbers = listOf(1, 6, 18, 34, 35)
        ),
        SurahGroupInfo(
            id = "alif_lam_mim_group",
            titleAr = "ذوات «الم»",
            categoryLabelAr = "حروف مقطعة",
            descriptionAr = "السور المفتتحة بالحروف المقطعة «ألف لام ميم» وتؤكد إعجاز القرآن وحقيقة الوحي.",
            surahNumbers = listOf(2, 3, 29, 30, 31, 32)
        ),
        SurahGroupInfo(
            id = "alif_lam_ra_group",
            titleAr = "ذوات «الر»",
            categoryLabelAr = "حروف مقطعة",
            descriptionAr = "مجموعة سور مكية متتالية تفتتح بـ «الر» وتعتني بسير الأنبياء والثبات على الدعوة.",
            surahNumbers = listOf(10, 11, 12, 14, 15)
        ),
        SurahGroupInfo(
            id = "tawasyn",
            titleAr = "الطواسين",
            categoryLabelAr = "متآخيات متوالية",
            descriptionAr = "السور الثلاث المتتابعة (الشعراء، النمل، القصص)؛ تفتتح بـ «طسم» أو «طس» وتعرض قصص موسى والأنبياء.",
            surahNumbers = listOf(26, 27, 28)
        ),
        SurahGroupInfo(
            id = "hawamim",
            titleAr = "الحواميم السبع (آل حم)",
            categoryLabelAr = "ديباج القرآن",
            descriptionAr = "سبع سور متتابعة تفتتح بـ «حم»، تُسمى ديباج القرآن وعرائسه، وتدور حول عظمة الوحي ومجابهة المعاندين.",
            surahNumbers = listOf(40, 41, 42, 43, 44, 45, 46)
        ),
        SurahGroupInfo(
            id = "musabbihat",
            titleAr = "المسبّحات",
            categoryLabelAr = "تماثل المطلع",
            descriptionAr = "السور المفتتحة بتسبيح الله تعالى وتنزيهه («سبّح»، «يسبّح»، «سبحان»، «سبّح اسم ربك»).",
            surahNumbers = listOf(17, 57, 59, 61, 62, 64, 87)
        ),
        SurahGroupInfo(
            id = "prophets_named_surahs",
            titleAr = "سور مسمّاة بأسماء الأنبياء",
            categoryLabelAr = "وحدة موضوعية",
            descriptionAr = "ست سور تحمل أسماء أنبياء ورسل صلوات الله عليهم وتبرز سنن الابتلاء والنصر والرسالة.",
            surahNumbers = listOf(10, 11, 12, 14, 47, 71)
        ),
        SurahGroupInfo(
            id = "tabarak_surahs",
            titleAr = "سورتا «تبارك»",
            categoryLabelAr = "تماثل المطلع",
            descriptionAr = "الفرقان والملك؛ تفتتحان بثناء «تبارك الذي...» بياناً لعظمة الملك والتنزيل.",
            surahNumbers = listOf(25, 67)
        ),
        SurahGroupInfo(
            id = "duha_sharh_pair",
            titleAr = "سورتا الامتنان والجبر (الضحى والشرح)",
            categoryLabelAr = "قرينتان متكاملتان",
            descriptionAr = "متلازمتان في المعنى والسياق؛ تذكّران بنعم الله على نبيه ﷺ وتبشّران بأن مع العسر يسراً.",
            surahNumbers = listOf(93, 94)
        ),
        SurahGroupInfo(
            id = "fil_quraysh_pair",
            titleAr = "سورتا الحفظ والرزق (الفيل وقريش)",
            categoryLabelAr = "قرينتان في المعنى",
            descriptionAr = "تترابطان ترابطاً وثيقاً: أهلك الله أصحاب الفيل لتأمن قريش في رحلتي الشتاء والصيف وتعبده.",
            surahNumbers = listOf(105, 106)
        ),
        SurahGroupInfo(
            id = "tawhid_pair",
            titleAr = "سورتا التوحيد والبراءة (الكافرون والإخلاص)",
            categoryLabelAr = "سنة القراءة",
            descriptionAr = "جمعتا البراءة من الشرك في الكافرون وإفراد الله بالتوحيد الخالص في الإخلاص، ويُقرآن في السنن والوتر.",
            surahNumbers = listOf(109, 112)
        ),
        SurahGroupInfo(
            id = "muawwidhatan",
            titleAr = "المعوّذتان",
            categoryLabelAr = "خاتمة المصحف",
            descriptionAr = "الفلق والناس؛ ورد في فضلهما أن ما تعوّذ متعوّذ بمثلهما، وتُختم بهما تلاوة المصحف الشريف.",
            surahNumbers = listOf(113, 114)
        )
    )

    private val allGroups: List<SurahGroupInfo> = sisterAndThematicGroups + classicalDivisions

    fun groupsForSurah(surahNumber: Int): List<SurahGroupInfo> =
        allGroups.filter { surahNumber in it.surahNumbers }

    fun allGroupsCatalog(): List<SurahGroupInfo> = allGroups
}
