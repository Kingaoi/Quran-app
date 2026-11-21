package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

internal object MosqueAndWakeAdhkarData {

    val mosqueItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "mosque_going",
            category = DhikrCategory.MOSQUE,
            title = "دعاء الذهاب إلى المسجد",
            arabicText = "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي لِسَانِي نُورًا، وَاجْعَلْ فِي سَمْعِي نُورًا، وَاجْعَلْ فِي بَصَرِي نُورًا، وَاجْعَلْ مِنْ خَلْفِي نُورًا، وَمِنْ أَمَامِي نُورًا، وَاجْعَلْ مِنْ فَوْقِي نُورًا، وَمِنْ تَحْتِي نُورًا، اللَّهُمَّ أَعْطِنِي نُورًا.",
            virtueOrNote = "يُقال عند الخروج من البيت متجهاً إلى المسجد لأداء الصلاة.",
            targetCount = 1,
            reference = "رواه البخاري ومسلم",
            chapterTitle = "الذهاب إلى المسجد"
        ),
        DhikrItem(
            id = "mosque_entering",
            category = DhikrCategory.MOSQUE,
            title = "دعاء دخول المسجد",
            arabicText = "أَعُوذُ بِاللَّهِ الْعَظِيمِ، وَبِوَجْهِهِ الْكَرِيمِ، وَسُلْطَانِهِ الْقَدِيمِ، مِنَ الشَّيْطَانِ الرَّجِيمِ. بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ.",
            virtueOrNote = "يُستحب تقديم الرجل اليمنى عند دخول المسجد وقول هذا الذكر.",
            targetCount = 1,
            reference = "صحيح مسلم وأبو داود",
            chapterTitle = "دعاء دخول المسجد"
        ),
        DhikrItem(
            id = "mosque_leaving",
            category = DhikrCategory.MOSQUE,
            title = "دعاء الخروج من المسجد",
            arabicText = "بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ.",
            virtueOrNote = "يُستحب تقديم الرجل اليسرى عند الخروج وطلب الفضل والرزق الحلال.",
            targetCount = 1,
            reference = "صحيح مسلم وابن ماجه",
            chapterTitle = "دعاء الخروج من المسجد"
        )
    )

    val wakeUpItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "wake_alhamdulillah",
            category = DhikrCategory.WAKE_UP,
            title = "دعاء الاستيقاظ من النوم",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.",
            virtueOrNote = "كان النبي ﷺ إذا استيقظ من منامه حمد الله بهذا الدعاء المبارك.",
            targetCount = 1,
            reference = "رواه البخاري",
            chapterTitle = "أذكار الاستيقاظ من النوم"
        ),
        DhikrItem(
            id = "wake_aafani",
            category = DhikrCategory.WAKE_UP,
            title = "الحمد على العافية ورد الروح",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ.",
            virtueOrNote = "شكر الله على تجدد الحياة والعافية والقدرة على الذكر والطاعة.",
            targetCount = 1,
            reference = "سنن الترمذي",
            chapterTitle = "أذكار الاستيقاظ من النوم"
        ),
        DhikrItem(
            id = "wake_taar_night",
            category = DhikrCategory.WAKE_UP,
            title = "الذكر عند التقلّب والاستيقاظ ليلاً",
            arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، الْحَمْدُ لِلَّهِ، وَسُبْحَانَ اللَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، اللَّهُمَّ اغْفِرْ لِي.",
            virtueOrNote = "من قالها إذا تعارّ من الليل ثم دعا استُجيب له، فإن توضأ وصلى قُبلت صلاته.",
            targetCount = 1,
            reference = "رواه البخاري",
            chapterTitle = "الذكر عند التقلّب ليلاً"
        )
    )

    val travelItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "travel_main",
            category = DhikrCategory.TRAVEL,
            title = "دعاء السفر والركوب",
            arabicText = "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَذَا وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ وَالْخَلِيفَةُ فِي الْأَهْلِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ وَعْثَاءِ السَّفَرِ وَكَآبَةِ الْمَنْظَرِ وَسُوءِ الْمُنْقَلَبِ فِي الْمَالِ وَالْأَهْلِ.",
            virtueOrNote = "سنة النبي ﷺ عند استوائه على دابته أو مركبته خارجاً إلى سفر.",
            targetCount = 1,
            reference = "صحيح مسلم",
            chapterTitle = "دعاء السفر والركوب"
        ),
        DhikrItem(
            id = "travel_return",
            category = DhikrCategory.TRAVEL,
            title = "دعاء الرجوع من السفر",
            arabicText = "آيِبُونَ تَائِبُونَ عَابِدُونَ لِرَبِّنَا حَامِدُونَ.",
            virtueOrNote = "يُضاف هذا الدعاء إلى دعاء السفر عند القفول والعودة إلى الوطن والأهل.",
            targetCount = 1,
            reference = "متفق عليه",
            chapterTitle = "دعاء الرجوع من السفر"
        ),
        DhikrItem(
            id = "travel_resident_to_traveler",
            category = DhikrCategory.TRAVEL,
            title = "دعاء المقيم للمسافر",
            arabicText = "أَسْتَوْدِعُ اللَّهَ دِينَكَ، وَأَمَانَتَكَ، وَخَوَاتِيمَ عَمَلِكَ.",
            virtueOrNote = "يُقال في توديع المسافر طلبًا لحفظ الله ورعايته.",
            targetCount = 1,
            reference = "أبو داود والترمذي",
            chapterTitle = "توديع المسافر"
        ),
        DhikrItem(
            id = "travel_to_resident",
            category = DhikrCategory.TRAVEL,
            title = "دعاء المسافر للمقيم",
            arabicText = "أَسْتَوْدِعُكُمُ اللَّهَ الَّذِي لَا تَضِيعُ وَدَائِعُهُ.",
            virtueOrNote = "ما يرد به المسافر على أهله ومن يودّعه.",
            targetCount = 1,
            reference = "مسند أحمد وابن ماجه",
            chapterTitle = "توديع المسافر"
        )
    )
}
