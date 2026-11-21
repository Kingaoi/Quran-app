package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

object ExtraAdhkarFallbackData {
    val dailyLifeItems = listOf(
        DhikrItem(
            id = "daily_home_enter",
            category = DhikrCategory.DAILY_LIFE,
            title = "دعاء دخول المنزل",
            arabicText = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            virtueOrNote = "ثم ليسلم على أهله.",
            targetCount = 1,
            reference = "حصن المسلم • أذكار دخول المنزل"
        ),
        DhikrItem(
            id = "daily_home_exit",
            category = DhikrCategory.DAILY_LIFE,
            title = "دعاء الخروج من المنزل",
            arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            virtueOrNote = "يقال له: هُديتَ وكُفيتَ ووُقيتَ وتنحّى عنه الشيطان.",
            targetCount = 1,
            reference = "حصن المسلم • الخروج من المنزل"
        ),
        DhikrItem(
            id = "daily_after_food",
            category = DhikrCategory.DAILY_LIFE,
            title = "الدعاء بعد الفراغ من الطعام",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            virtueOrNote = "غُفر له ما تقدم من ذنبه.",
            targetCount = 1,
            reference = "حصن المسلم • بعد الطعام"
        )
    )

    val worryReliefItems = listOf(
        DhikrItem(
            id = "worry_karb",
            category = DhikrCategory.WORRY_RELIEF,
            title = "دعاء الكرب العظيم",
            arabicText = "لَا إِلَهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
            virtueOrNote = "كان النبي ﷺ يدعو به عند الكرب.",
            targetCount = 1,
            reference = "حصن المسلم • دعاء الكرب"
        ),
        DhikrItem(
            id = "worry_debt",
            category = DhikrCategory.WORRY_RELIEF,
            title = "دعاء قضاء الدَّين وتفريج الهم",
            arabicText = "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
            virtueOrNote = "يقال لتفريج الكرب وأداء الدين.",
            targetCount = 3,
            reference = "حصن المسلم • قضاء الدين"
        )
    )

    val weatherNatureItems = listOf(
        DhikrItem(
            id = "weather_rain",
            category = DhikrCategory.WEATHER_NATURE,
            title = "الدعاء عند نزول المطر",
            arabicText = "اللَّهُمَّ صَيِّبًا نَافِعًا",
            virtueOrNote = "الدعاء مستجاب وقت نزول الغيث.",
            targetCount = 1,
            reference = "حصن المسلم • عند نزول المطر"
        ),
        DhikrItem(
            id = "weather_hilal",
            category = DhikrCategory.WEATHER_NATURE,
            title = "دعاء رؤية الهلال",
            arabicText = "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْيُمْنِ وَالْإِيمَانِ، وَالسَّلَامَةِ وَالْإِسْلَامِ، رَبِّي وَرَبُّكَ اللَّهُ",
            virtueOrNote = "يقال عند استهلال الشهر الهجري الجديد.",
            targetCount = 1,
            reference = "حصن المسلم • رؤية الهلال"
        )
    )

    val socialEthicsItems = listOf(
        DhikrItem(
            id = "social_majlis",
            category = DhikrCategory.SOCIAL_ETHICS,
            title = "دعاء كفارة المجلس",
            arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ",
            virtueOrNote = "يُغفر له ما كان في مجلسه ذلك.",
            targetCount = 1,
            reference = "حصن المسلم • كفارة المجلس"
        ),
        DhikrItem(
            id = "social_sneeze",
            category = DhikrCategory.SOCIAL_ETHICS,
            title = "أذكار العطاس",
            arabicText = "الْحَمْدُ لِلَّهِ — فيقول له أخوه: يَرْحَمُكَ اللَّهُ — فيرد: يَهْدِيكُمُ اللَّهُ وَيُصْلِحُ بَالَكُمْ",
            virtueOrNote = "من آداب الإسلام وحقوق المسلم على أخيه.",
            targetCount = 1,
            reference = "حصن المسلم • العطاس"
        )
    )

    val hajjWorshipItems = listOf(
        DhikrItem(
            id = "hajj_talbiyah",
            category = DhikrCategory.HAJJ_WORSHIP,
            title = "التلبية في الحج والعمرة",
            arabicText = "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
            virtueOrNote = "شعار المحرم بالحج والعمرة.",
            targetCount = 3,
            reference = "حصن المسلم • التلبية"
        ),
        DhikrItem(
            id = "hajj_fasting",
            category = DhikrCategory.HAJJ_WORSHIP,
            title = "الدعاء عند إفطار الصائم",
            arabicText = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            virtueOrNote = "يقال عند الإفطار.",
            targetCount = 1,
            reference = "حصن المسلم • عند الإفطار"
        )
    )
}
