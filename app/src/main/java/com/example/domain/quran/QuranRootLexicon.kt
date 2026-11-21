package com.example.domain.quran

internal data class QuranRootSpec(
    val root: String,
    val displayLabel: String,
    val stems: List<String> = emptyList(),
    val exactWords: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val allowAlgorithmicFallback: Boolean = true
)

internal object QuranRootLexiconPart1 {
    val entries: List<QuranRootSpec> = listOf(
        QuranRootSpec(
            root = "رحم", displayLabel = "الرحمة",
            stems = listOf("رحم", "راحم", "رحيم", "رحمن", "مرحم", "ارحم"),
            aliases = listOf("الرحمن", "الرحيم", "رحمة", "ارحم")
        ),
        QuranRootSpec(
            root = "علم", displayLabel = "العلم",
            stems = listOf("علم", "عالم", "عليم", "علام", "اعلم", "تعلم", "يعلم", "نعلم", "معلوم"),
            aliases = listOf("علم", "يعلمون", "عليما", "علماء", "عالمين")
        ),
        QuranRootSpec(
            root = "صبر", displayLabel = "الصبر",
            stems = listOf("صبر", "صابر", "صبور", "اصبر", "يصبر", "تصبر", "نصبر", "اصطبر"),
            aliases = listOf("الصبر", "الصابرين", "اصبروا", "واصبر")
        ),
        QuranRootSpec(
            root = "شكر", displayLabel = "الشكر",
            stems = listOf("شكر", "شاكر", "شكور", "اشكر", "يشكر", "تشكر", "نشكر"),
            aliases = listOf("الشكر", "الشاكرين", "شكور", "تشكرون")
        ),
        QuranRootSpec(
            root = "ذكر", displayLabel = "الذكر",
            stems = listOf("ذكر", "ذاكر", "تذكر", "يذكر", "نذكر", "اذكر", "مذكور", "مذكر", "تذكرة", "ذكري"),
            aliases = listOf("الذكر", "تذكرون", "اذكروا", "الذاكرين", "ذكرى")
        ),
        QuranRootSpec(
            root = "هدي", displayLabel = "الهداية",
            stems = listOf("هدي", "هاد", "مهتد", "اهتد", "يهد", "تهد", "نهد", "اهد"),
            exactWords = listOf("هدي", "هدى", "هداه", "هداهم", "هدانا", "اهدنا", "يهدي", "تهدي", "نهدي", "اهتدي", "مهتدي", "مهتدين", "مهتدون", "الهدي", "هادي"),
            aliases = listOf("الهدى", "اهدنا", "يهدي", "مهتدين", "هدى"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "امن", displayLabel = "الإيمان والأمان",
            stems = listOf("امن", "يؤمن", "تؤمن", "نؤمن", "مؤمن", "ايمان", "امان", "امين", "مأمون", "استأمن"),
            exactWords = listOf("امنوا", "امنت", "امنا", "امن", "امان", "امنه", "الامين", "امين", "ايمان", "ايمانا", "ايمانهم", "ايمانكم"),
            aliases = listOf("آمنوا", "إيمان", "مؤمنين", "أمين", "آمن"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "توب", displayLabel = "التوبة",
            stems = listOf("توب", "تاب", "تائب", "تواب", "يتوب", "تتب", "نتوب", "متاب"),
            exactWords = listOf("تاب", "تابوا", "تبت", "تبنا", "تب", "توبة", "التواب", "تواب"),
            aliases = listOf("التوبة", "تواب", "يتوب", "تابوا"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "غفر", displayLabel = "المغفرة",
            stems = listOf("غفر", "غافر", "غفور", "غفار", "مغفر", "استغفر", "يستغفر", "يغفر", "تغفر", "نغفر", "اغفر"),
            aliases = listOf("المغفرة", "غفور", "استغفروا", "اغفر")
        ),
        QuranRootSpec(
            root = "قول", displayLabel = "القول",
            stems = listOf("قول", "قائل", "مقول", "اقاويل", "يقول", "تقول", "نقول", "اقول"),
            exactWords = listOf("قال", "قالوا", "قالت", "قالا", "قل", "قولا", "قلتم", "قلت", "قلنا", "قيل", "قول", "قوله", "قولهم", "قولك", "اقوال"),
            aliases = listOf("قال", "يقول", "قل", "قيل", "قولا"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "دعو", displayLabel = "الدعاء والدعوة",
            stems = listOf("دعو", "داع", "مدعو", "يدع", "تدع", "ندع", "ادع"),
            exactWords = listOf("دعا", "دعوا", "دعت", "دعوت", "دعونا", "دعاء", "دعائي", "دعوة", "دعوتهم", "ادع", "ادعوا", "يدعو", "يدعون", "تدعو", "تدعون", "ندعو", "الداعي", "داع"),
            aliases = listOf("الدعاء", "ادعوني", "يدعو", "دعوة"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "صلو", displayLabel = "الصلاة",
            stems = listOf("صلو", "مصلي", "يصلي", "تصلي", "نصلي"),
            exactWords = listOf("صلاه", "صلوه", "الصلاه", "الصلوه", "صلاتك", "صلاتهم", "صلاتنا", "صلوات", "صلي", "صل", "صلوا", "يصلي", "يصلون", "تصلي", "تصلون", "مصلي", "المصلين", "مصلين"),
            aliases = listOf("الصلاة", "صلوات", "صل", "المصلين", "صلاة"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "زكو", displayLabel = "الزكاة والتزكية",
            stems = listOf("زكو", "زكي", "تزكي", "يزكي"),
            exactWords = listOf("زكاه", "زكوه", "الزكاه", "الزكوه", "زكي", "زكيه", "ازكي", "يزكي", "تزكي", "نزكي", "زكاها", "تزكيه"),
            aliases = listOf("الزكاة", "يزكي", "تزكى", "زكاة"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "صوم", displayLabel = "الصيام",
            stems = listOf("صوم", "صائم", "صيام", "يصوم", "تصوم"),
            exactWords = listOf("صوم", "صوما", "صيام", "الصيام", "صائم", "الصائمين", "الصائمات", "تصوموا", "يصم"),
            aliases = listOf("الصيام", "صوم", "تصوموا", "الصائمين"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "وقي", displayLabel = "التقوى والوقاية",
            stems = listOf("تقو", "متق", "يتق", "تتق", "نتق", "اتق", "وقي", "تقي"),
            exactWords = listOf("اتقوا", "اتق", "تقوي", "التقوي", "متقي", "متقين", "المتقين", "المتقون", "يتقي", "يتق", "تتقي", "تتقون", "وقانا", "وقاهم", "قنا", "يق", "تقي", "تقيا"),
            aliases = listOf("التقوى", "اتقوا", "المتقين", "تقوى", "تقي"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "رزق", displayLabel = "الرزق",
            stems = listOf("رزق", "رازق", "مرزوق", "يرزق", "ترزق", "نرزق", "ارزق"),
            aliases = listOf("الرزق", "يرزق", "ارزقنا", "رازقين")
        ),
        QuranRootSpec(
            root = "نفق", displayLabel = "الإنفاق والنفاق",
            stems = listOf("نفق", "انفق", "ينفق", "تنفق", "ننفق", "منفق", "منافق", "نفاق"),
            aliases = listOf("أنفقوا", "ينفقون", "إنفاق", "المنافقين")
        ),
        QuranRootSpec(
            root = "صدق", displayLabel = "الصدق والصدقة",
            stems = listOf("صدق", "صادق", "صديق", "مصدق", "متصدق", "تصدق", "يصدق"),
            aliases = listOf("الصدق", "الصادقين", "صدقات", "صديقا")
        ),
        QuranRootSpec(
            root = "حق", displayLabel = "الحق",
            stems = listOf("حقق", "احق", "يحق", "تستحق", "الحق"),
            exactWords = listOf("حق", "حقا", "الحق", "حقه", "حقها", "حقهما", "حقهم", "احق", "يحق", "حقت"),
            aliases = listOf("الحق", "حقا", "أحق", "يحق"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "عدل", displayLabel = "العدل",
            stems = listOf("عدل", "عادل", "معدل", "يعدل", "تعدل", "اعدل"),
            aliases = listOf("العدل", "اعدلوا", "يعدلون")
        ),
        QuranRootSpec(
            root = "ظلم", displayLabel = "الظلم والظلمات",
            stems = listOf("ظلم", "ظالم", "ظلام", "مظلوم", "مظلم", "يظلم", "تظلم", "نظلم", "اظلم"),
            aliases = listOf("الظلم", "الظالمين", "ظلمات", "أظلم")
        ),
        QuranRootSpec(
            root = "خوف", displayLabel = "الخوف",
            stems = listOf("خوف", "خائف", "يخاف", "تخاف", "نخاف", "اخاف", "تخويف"),
            exactWords = listOf("خاف", "خافوا", "خفت", "خفتم", "خفنا", "خوف", "خوفا", "خيفة", "خائف", "خائفين", "يخاف", "يخافون", "تخاف", "تخافوا", "تخف", "اخاف"),
            aliases = listOf("الخوف", "خائفين", "يخافون", "تخف"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "رجو", displayLabel = "الرجاء",
            stems = listOf("رجو", "راج", "مرجو", "يرج", "ترج", "نرج", "ارج"),
            exactWords = listOf("رجا", "رجوا", "يرجو", "يرجون", "ترجو", "ترجون", "نرجو", "ارجو", "مرجوا", "الراجين", "ارجاء"),
            aliases = listOf("يرجو", "نرجو", "ترجون"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "حبب", displayLabel = "المحبة",
            stems = listOf("حبب", "احب", "محب", "حبيب", "يحب", "تحب", "نحب"),
            exactWords = listOf("حب", "حبا", "حبه", "احب", "احبكم", "احببتم", "يحب", "يحبون", "يحبه", "يحبهم", "تحب", "تحبون", "نحب", "محبة", "احباؤه"),
            aliases = listOf("الحب", "يحب", "محبة", "تحبون"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "رضي", displayLabel = "الرضا والرضوان",
            stems = listOf("رضي", "راض", "مرض", "يرض", "ترض", "نرض", "ارض", "رضوان", "تراض"),
            exactWords = listOf("رضي", "رضوا", "رضيت", "رضينا", "رضوان", "رضوانا", "مرضات", "مرضاة", "راضيه", "مرضيه", "يرضي", "يرضون", "ترضي", "ترضون", "نرضي", "ارض", "تراض"),
            aliases = listOf("الرضا", "رضوان", "مرضاة", "راضية", "رضي"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "نصر", displayLabel = "النصر",
            stems = listOf("نصر", "ناصر", "نصير", "منصور", "ينصر", "تنصر", "ننصر", "انصر", "استنصر", "انتصر"),
            aliases = listOf("النصر", "نصيرا", "ينصر", "انصرنا", "الأنصار")
        ),
        QuranRootSpec(
            root = "فتح", displayLabel = "الفتح",
            stems = listOf("فتح", "فاتح", "فتاح", "مفتوح", "مفاتح", "يفتح", "تفتح", "نفتح", "افتح", "استفتح"),
            aliases = listOf("الفتح", "فتحا", "فتاح", "تفتح")
        ),
        QuranRootSpec(
            root = "عزز", displayLabel = "العزة",
            stems = listOf("عزز", "عزيز", "اعز", "تعز", "يعز"),
            exactWords = listOf("عز", "عزا", "عزه", "العزه", "عزيز", "العزيز", "عزيزا", "اعز", "اعزه", "تعز", "يعز"),
            aliases = listOf("العزة", "العزيز", "أعزة", "تعز"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "قوي", displayLabel = "القوة",
            stems = listOf("قوي", "قوه", "اقو", "يقو", "تقو"),
            exactWords = listOf("قوي", "القوي", "قويا", "قوه", "بقوه", "القوه", "قواكم", "اقوي", "اقويا"),
            aliases = listOf("القوة", "القوي", "قوة"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "سلم", displayLabel = "السلام والإسلام",
            stems = listOf("سلم", "سلام", "مسلم", "اسلام", "سالم", "سليم", "يسلم", "تسلم", "نسلم", "اسلم", "مستسلم"),
            aliases = listOf("السلام", "الإسلام", "مسلمين", "سليم", "أسلم")
        )
    )
}
