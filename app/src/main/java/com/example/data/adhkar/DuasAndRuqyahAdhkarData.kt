package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

internal object DuasAndRuqyahAdhkarData {

    val quranicDuasItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "quran_dua_hasanah",
            category = DhikrCategory.QURANIC_DUAS,
            title = "سؤال الحسنة في الدارين",
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ.",
            virtueOrNote = "كان أكثر دعاء النبي ﷺ، وهو من أجمع الأدعية القرآنية لخير الدنيا والآخرة.",
            targetCount = 1,
            reference = "سورة البقرة: ٢٠١"
        ),
        DhikrItem(
            id = "quran_dua_hidayah",
            category = DhikrCategory.QURANIC_DUAS,
            title = "الثبات على الهداية",
            arabicText = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ.",
            virtueOrNote = "دعاء الراسخين في العلم بطلب الثبات والرحمة.",
            targetCount = 1,
            reference = "سورة آل عمران: ٨"
        ),
        DhikrItem(
            id = "quran_dua_musa",
            category = DhikrCategory.QURANIC_DUAS,
            title = "دعاء انشراح الصدر وتيسير الأمر",
            arabicText = "رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي ۝ وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي ۝ يَفْقَهُوا قَوْلِي.",
            virtueOrNote = "دعاء نبي الله موسى عليه السلام عند الشروع في المهام العظيمة.",
            targetCount = 1,
            reference = "سورة طه: ٢٥-٢٨"
        ),
        DhikrItem(
            id = "quran_dua_family",
            category = DhikrCategory.QURANIC_DUAS,
            title = "دعاء صلاح الأهل والذرية",
            arabicText = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا.",
            virtueOrNote = "من صفات عباد الرحمن دعاءُهم بصلاح أسرهم وتقواهم.",
            targetCount = 1,
            reference = "سورة الفرقان: ٧٤"
        ),
        DhikrItem(
            id = "quran_dua_salah",
            category = DhikrCategory.QURANIC_DUAS,
            title = "دعاء إقامة الصلاة والمغفرة",
            arabicText = "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ ۝ رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ.",
            virtueOrNote = "من دعاء الخليل إبراهيم عليه السلام لنفسه وذريته ووالديه والمؤمنين.",
            targetCount = 1,
            reference = "سورة إبراهيم: ٤٠-٤١"
        )
    )

    val propheticDuasItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "prophetic_huda_tuqa",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = "سؤال الهدى والتقى والعفاف",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى، وَالْعَفَافَ وَالْغِنَى.",
            virtueOrNote = "دعاء جامع لخيري القلب والجوارح والاستغناء بالله عن الناس.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prophetic_aslih_dini",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = "إصلاح الدين والدنيا والمعاد",
            arabicText = "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ.",
            virtueOrNote = "من جوامع الدعاء النبوي لصلاح المعيشة والمآل.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prophetic_muqallib",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = "دعاء ثبات القلب",
            arabicText = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ.",
            virtueOrNote = "من أكثر ما كان يدعو به النبي ﷺ طلباً للثبات على الحق.",
            targetCount = 3,
            reference = "سنن الترمذي"
        ),
        DhikrItem(
            id = "prophetic_hamm_hazan",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = "الاستعاذة من الهم والحزن",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ.",
            virtueOrNote = "دعاء عظيم لتفريج الكروب وسداد الديون واستجلاب الهمة.",
            targetCount = 1,
            reference = "رواه البخاري"
        ),
        DhikrItem(
            id = "prophetic_afiyah",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = "سؤال العفو والعافية",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي، اللَّهُمَّ اسْتُرْ عَوْرَاتِي وَآمِنْ رَوْعَاتِي.",
            virtueOrNote = "لم يُؤتَ أحد بعد اليقين خيراً من العافية.",
            targetCount = 1,
            reference = "أبو داود وابن ماجه"
        )
    )

    val ruqyahItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "ruqyah_fatihah",
            category = DhikrCategory.RUQYAH,
            title = "سورة الفاتحة",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ.",
            virtueOrNote = "أم الكتاب والرقية الشافية بإذن الله تعالى.",
            targetCount = 1,
            reference = "سورة الفاتحة: ١-٧ • متفق عليه",
            chapterTitle = "الرقية بالقرآن الكريم"
        ),
        DhikrItem(
            id = "ruqyah_kursi",
            category = DhikrCategory.RUQYAH,
            title = "آية الكرسي",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            virtueOrNote = "حرز وحفظ من الشياطين وكل سوء بإذن الله.",
            targetCount = 1,
            reference = "سورة البقرة: ٢٥٥",
            chapterTitle = "الرقية بالقرآن الكريم"
        ),
        DhikrItem(
            id = "ruqyah_adhhib_albas",
            category = DhikrCategory.RUQYAH,
            title = "دعاء الشفاء للمريض",
            arabicText = "أَذْهِبِ الْبَأْسَ رَبَّ النَّاسِ، وَاشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا.",
            virtueOrNote = "كان النبي ﷺ يمسح بيده اليمنى على المريض ويدعو بهذا الدعاء.",
            targetCount = 3,
            reference = "متفق عليه",
            chapterTitle = "الرقية النبوية للمريض"
        ),
        DhikrItem(
            id = "ruqyah_bismillah_arqik",
            category = DhikrCategory.RUQYAH,
            title = "الرقية النبوية الجامعة",
            arabicText = "بِسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِسْمِ اللَّهِ أَرْقِيكَ.",
            virtueOrNote = "رقية جبريل عليه السلام للنبي ﷺ عند المرض والعين.",
            targetCount = 3,
            reference = "صحيح مسلم",
            chapterTitle = "الرقية النبوية للمريض"
        ),
        DhikrItem(
            id = "ruqyah_kalimat_allah",
            category = DhikrCategory.RUQYAH,
            title = "التعوذ بكلمات الله التامة",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ.",
            virtueOrNote = "كان النبي ﷺ يعوّذ بها الحسن والحسين كما كان إبراهيم يعوّذ إسماعيل وإسحاق.",
            targetCount = 3,
            reference = "رواه البخاري",
            chapterTitle = "التحصين وتعويذ الأبناء"
        )
    )
}
