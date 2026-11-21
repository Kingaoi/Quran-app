package com.example.domain.model

import androidx.compose.runtime.Immutable

enum class SourceCategoryId(val shortLabelAr: String) {
    MUSHAF_TEXT("المصحف والنص"),
    TAFSIR_SCIENCES("التفسير والعلوم"),
    AUDIO_MEDIA("التلاوات والأذان"),
    ADHKAR_DUAS("الأذكار والأدعية"),
    PRAYER_CALENDAR("الصلاة والفلك"),
    FONTS_TECH("الخطوط والتقنية")
}

@Immutable
data class AppSourceDetailItem(
    val id: String,
    val titleAr: String,
    val badgeAr: String,
    val roleAr: String,
    val referenceText: String,
    val detailsAr: String
)

@Immutable
data class AppSourceGroup(
    val id: SourceCategoryId,
    val titleAr: String,
    val subtitleAr: String,
    val accentIndex: Int,
    val items: List<AppSourceDetailItem>
)

object AppSourcesCatalog {
    val groups: List<AppSourceGroup> = listOf(
        AppSourceGroup(
            id = SourceCategoryId.MUSHAF_TEXT,
            titleAr = "القرآن الكريم ومخطوطات المصحف",
            subtitleAr = "النص العثماني، الرسم الإملائي، وصفحات المصحف المتجهية بالروايات",
            accentIndex = 1,
            items = listOf(
                AppSourceDetailItem(
                    id = "ws_quran_text",
                    titleAr = "مكتبة النص القرآني والفهرس الشامل",
                    badgeAr = "مدمج بالكامل",
                    roleAr = "النص العثماني والإملائي، مواضع السور والآيات، والأجزاء والأرباع",
                    referenceText = "ws.quran:quran-text (الإصدار 0.1.0)",
                    detailsAr = "توفر كامل النص القرآني (١١٤ سورة، ٦٢٣٦ آية) بالرسم العثماني والرسم الإملائي مع مواضع الصفحات (٦٠٤ صفحات)، والأجزاء الثلاثين، والأرباع الـ ٢٤٠، ومواضع سجدات التلاوة، بالإضافة إلى خط حفص العثماني المدمج."
                ),
                AppSourceDetailItem(
                    id = "quran_ws_svg",
                    titleAr = "صفحات المصحف المتجهية (مصحف المدينة)",
                    badgeAr = "SVG عالي الدقة",
                    roleAr = "عرض صفحات المصحف وتنزيلها للعمل دون اتصال بالروايات الخمس",
                    referenceText = "cdn.quran.ws/svg/pages  •  github.com/quran-ws/quran-svg",
                    detailsAr = "تُجلب صفحات المصحف المتجهية من شبكة التوزيع الأساسية مع مسار احتياطي من مستودع quran-svg، وتشمل روايات: حفص عن عاصم (hafs-kfqc)، ورش عن نافع (warsh-kfqc)، قالون عن نافع (qalon-kfqc)، الدوري عن أبي عمرو (douri-kfqc)، وشعبة عن عاصم (shubah-kfqc)، مع زخارف رؤوس السور المدمجة (surah_header_madinah)."
                )
            )
        ),
        AppSourceGroup(
            id = SourceCategoryId.TAFSIR_SCIENCES,
            titleAr = "كتب التفسير وعلوم القرآن",
            subtitleAr = "التفاسير المعتمدة، أسباب النزول، غريب الكلمات، وأدوات البحث",
            accentIndex = 2,
            items = listOf(
                AppSourceDetailItem(
                    id = "tafsir_api_cdn",
                    titleAr = "موسوعة كتب التفسير الأربعة",
                    badgeAr = "٤ تفاسير كاملة",
                    roleAr = "عرض وتنزيل تفسير كامل القرآن (١١٤ سورة) للعمل دون اتصال",
                    referenceText = "cdn.jsdelivr.net/gh/spa5k/tafsir_api  •  api.alquran.cloud",
                    detailsAr = "تضم أربع طبعات عربية معتمدة: التفسير الميسر (ar-tafsir-muyassar) مع مرجع احتياطي للآية المفردة عبر api.alquran.cloud، وتيسير الكريم الرحمن للشيخ السعدي (ar-tafseer-al-saddi)، وتفسير القرآن العظيم لابن كثير (ar-tafsir-ibn-kathir)، ومعالم التنزيل للإمام البغوي (ar-tafsir-al-baghawi)."
                ),
                AppSourceDetailItem(
                    id = "asbab_gharib_waqf",
                    titleAr = "أسباب النزول وغريب القرآن وعلامات الوقف",
                    badgeAr = "مراجع تراثية",
                    roleAr = "تخريج أسباب النزول ومعاني الألفاظ الغريبة وبيان علامات الوقف",
                    referenceText = "الواحدي • البخاري • مسلم • أحمد • الترمذي • ابن كثير • البغوي • ابن هشام",
                    detailsAr = "تُعرض مرويات أسباب النزول ومقاصد السور موثّقة بعزوها إلى كتب الحديث والسيرة والتفسير، مع قاموس غريب القرآن وعلامات الوقف والابتداء (مـ، قلى، صلى، ج، لا، المعانقة، السكتة)، ومحرك محلي للجذور الثلاثية والمتشابهات اللفظية والبحث الدلالي."
                )
            )
        ),
        AppSourceGroup(
            id = SourceCategoryId.AUDIO_MEDIA,
            titleAr = "التلاوات الصوتية والمؤذن والصور",
            subtitleAr = "مقرئو الآيات والسور، أصوات الأذان، وصور القرّاء التعريفية",
            accentIndex = 4,
            items = listOf(
                AppSourceDetailItem(
                    id = "everyayah_reciters",
                    titleAr = "تلاوات القرآن المجوّدة والمرتّلة — آية بآية",
                    badgeAr = "١١ قارئاً",
                    roleAr = "تلاوة آية بآية مع التظليل المتزامن والتنزيل المسبق",
                    referenceText = "everyayah.com/data",
                    detailsAr = "يوفّر ملفات التلاوة برواية حفص عن عاصم للقرّاء: عبد الباسط عبد الصمد، ماهر المعيقلي، مشاري العفاسي، محمود خليل الحصري، محمد صديق المنشاوي، عبد الرحمن السديس، علي الحذيفي، وسعد الغامدي؛ وبرواية ورش عن نافع للقرّاء: عبد الباسط، إبراهيم الدوسري، وياسين الجزائري."
                ),
                AppSourceDetailItem(
                    id = "mp3quran_riwayat",
                    titleAr = "دليل قرّاء الروايات — بالسورة الكاملة",
                    badgeAr = "قائمة متجددة",
                    roleAr = "تلاوات السور الكاملة لروايات قالون والدوري وشعبة وورش",
                    referenceText = "mp3quran.net/api/v3/reciters?language=ar",
                    detailsAr = "يجلب قائمة القرّاء الإضافيين بالسورة الكاملة ويخزن الفهرس محلياً لتحسين الاستجابة وتوسيع خيارات التلاوة للروايات المتعددة."
                ),
                AppSourceDetailItem(
                    id = "islamcan_adhan",
                    titleAr = "تسجيلات أصوات الأذان",
                    badgeAr = "٧ صيغ للأذان",
                    roleAr = "تنبيهات الأذان ومعاينة وحفظ صوت المؤذن دون إنترنت",
                    referenceText = "islamcan.com/audio/adhan",
                    detailsAr = "يضم تسجيلات صوتية لأذان المسجد الحرام بمكة، والمسجد النبوي بالمدينة، والمسجد الأقصى، والأذان الخليجي، والمدرسة المصرية، بالإضافة إلى صيغتي الأذان القصير والتكبير المختصر."
                ),
                AppSourceDetailItem(
                    id = "wikimedia_portraits",
                    titleAr = "الصور التعريفية للقرّاء",
                    badgeAr = "صور متاحة عاماً",
                    roleAr = "عرض صور القرّاء الأعلام في قائمة اختيار القارئ ومشغّل التلاوة",
                    referenceText = "commons.wikimedia.org",
                    detailsAr = "تُحمّل صور القرّاء عند الاتصال من أرشيف ويكيميديا كومنز مع استخدام أحرف تعريفية مدمجة تلقائياً عند تعذر التحميل أو عدم توفر صورة."
                )
            )
        ),
        AppSourceGroup(
            id = SourceCategoryId.ADHKAR_DUAS,
            titleAr = "الأذكار والأدعية وأسماء الله الحسنى",
            subtitleAr = "متون الأذكار اليومية، الرقية الشرعية، والأدعية المأثورة",
            accentIndex = 0,
            items = listOf(
                AppSourceDetailItem(
                    id = "hisn_muslim",
                    titleAr = "كتاب حصن المسلم من أذكار الكتاب والسنة",
                    badgeAr = "نص وصوت",
                    roleAr = "أذكار الصباح والمساء والنوم والصلاة والاستيقاظ والرقية",
                    referenceText = "hisnmuslim.com/api/ar/husn_ar.json",
                    detailsAr = "يعتمد على أبواب حصن المسلم في النصوص والأصوات عبر واجهة الكتاب الرقمية مع نسخة احتياطية مدمجة بالكامل داخل التطبيق لضمان العمل الفوري حتى دون اتصال بالشبكة."
                ),
                AppSourceDetailItem(
                    id = "duas_asma_khatmah",
                    titleAr = "الأدعية المأثورة ودعاء الختمة وأسماء الله الحسنى",
                    badgeAr = "محتوى مدمج",
                    roleAr = "أدعية الأنبياء والقرآن والسنة وشرح الأسماء الحسنى (٩٩ اسماً)",
                    referenceText = "بيانات مدمجة موثّقة من القرآن الكريم والسنّة النبوية",
                    detailsAr = "تتضمن باب دعاء ختم القرآن الكريم بصيغته المنسّقة، ومجموعة الأدعية القرآنية والنبوية، ومعاني أسماء الله الحسنى التسعة والتسعين."
                )
            )
        ),
        AppSourceGroup(
            id = SourceCategoryId.PRAYER_CALENDAR,
            titleAr = "مواقيت الصلاة والقبلة والتقويم الهجري",
            subtitleAr = "المعادلات الفلكية، هيئات الحساب المعتمدة، وتقويم أم القرى",
            accentIndex = 3,
            items = listOf(
                AppSourceDetailItem(
                    id = "astronomical_prayer_calc",
                    titleAr = "محرّك الحساب الفلكي لمواقيت الصلاة والقبلة",
                    badgeAr = "حساب محلي",
                    roleAr = "حساب الصلوات الخمس والشروق ومنتصف الليل والثلث الأخير واتجاه القبلة",
                    referenceText = "معادلات موقع الشمس الفلكية وإحداثيات الكعبة المشرفة (21.4225°N, 39.8262°E)",
                    detailsAr = "يدعم هيئات: أم القرى بمكة، رابطة العالم الإسلامي، الهيئة المصرية العامة للمساحة، الإمارات ودبي، جامعة كراتشي، وأمريكا الشمالية (ISNA)، مع مذهبي الجمهور والحنفي في صلاة العصر، وقاعدة مدن عربية وعالمية مدمجة أو عبر موقع الجهاز."
                ),
                AppSourceDetailItem(
                    id = "umm_al_qura_calendar",
                    titleAr = "التقويم الهجري ومناسبات السنن",
                    badgeAr = "تقويم أم القرى",
                    roleAr = "حساب التاريخ الهجري وتنبيهات الأيام البيض وصيام الاثنين والخميس والجمعة",
                    referenceText = "Android ICU IslamicCalendar (islamic-umalqura)",
                    detailsAr = "يستخدم تقويم أم القرى المعتمد داخل النظام مع إمكانية التعديل اليدوي بمقدار يومين تقديماً أو تأخيراً وفق رؤية الهلال المحلية."
                )
            )
        ),
        AppSourceGroup(
            id = SourceCategoryId.FONTS_TECH,
            titleAr = "الخطوط العربية والمكتبات التقنية",
            subtitleAr = "الخطوط المدمجة وأدوات العرض والتشغيل والتخزين المحلي",
            accentIndex = 5,
            items = listOf(
                AppSourceDetailItem(
                    id = "bundled_arabic_fonts",
                    titleAr = "مجموعة الخطوط العربية والقرآنية",
                    badgeAr = "٥ خطوط مدمجة",
                    roleAr = "عرض المصحف والأذكار وواجهة القراءة بوضوح وتشكيل دقيق",
                    referenceText = "UthmanicHafs v3.0 • Amiri • Amiri Quran • Scheherazade New • Alyamama",
                    detailsAr = "يُستخدم خط اليمامة (Alyamama) بأوزانه الخمسة لواجهة التطبيق، بينما تتاح للقراءة خطوط: حفص العثماني، الأميري القرآني، الأميري، شهرزاد الجديد، وخط اليمامة."
                ),
                AppSourceDetailItem(
                    id = "android_open_libraries",
                    titleAr = "المكتبات البرمجية الأساسية",
                    badgeAr = "مكونات التطبيق",
                    roleAr = "واجهة المستخدم، تشغيل الصوت، رسم الصفحات المتجهية، والنسخ الاحتياطي",
                    referenceText = "Jetpack Compose (Material 3) • Media3 ExoPlayer • Coil / Coil-SVG • OkHttp • DataStore",
                    detailsAr = "تعمل الواجهة بمنظومة Jetpack Compose و Material 3، وتُشغّل التلاوات والأذان عبر Media3 ExoPlayer، وتُعرض صفحات SVG والصور عبر Coil، مع حفظ الإعدادات محلياً عبر DataStore وتصديرها بصيغة JSON."
                )
            )
        )
    )

    val totalGroupsCount: Int get() = groups.size
    val totalSourcesCount: Int get() = groups.sumOf { it.items.size }
}
