package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

internal object MorningEveningAdhkarData {

    val morningItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "morning_kursi",
            category = DhikrCategory.MORNING,
            title = "آية الكرسي",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            virtueOrNote = "من قرأها حين يصبح أُجير من الجن حتى يمسي.",
            targetCount = 1,
            reference = "سورة البقرة: ٢٥٥"
        ),
        DhikrItem(
            id = "morning_muawwidhat",
            category = DhikrCategory.MORNING,
            title = "المعوذات الثلاث",
            arabicText = "قراءة سورة الإخلاص، وسورة الفلق، وسورة الناس (ثلاث مرات لكل سورة).",
            virtueOrNote = "من قرأها ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء.",
            targetCount = 3,
            reference = "أبو داود والترمذي"
        ),
        DhikrItem(
            id = "morning_mulk",
            category = DhikrCategory.MORNING,
            title = "أصبحنا وأصبح الملك لله",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
            virtueOrNote = "ذكر جامع لافتتاح اليوم بطلب الخير والاستعاذة من الشرور.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "morning_sayyid_istighfar",
            category = DhikrCategory.MORNING,
            title = "سيد الاستغفار",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            virtueOrNote = "من قالها من النهار موقناً بها فمات من يومه قبل أن يمسي فهو من أهل الجنة.",
            targetCount = 1,
            reference = "صحيح البخاري"
        ),
        DhikrItem(
            id = "morning_bismillah_la_yadurr",
            category = DhikrCategory.MORNING,
            title = "التحصين باسم الله",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            virtueOrNote = "من قالها ثلاثاً إذا أصبح لم تصبه فجأة بلاء حتى يمسي.",
            targetCount = 3,
            reference = "أبو داود والترمذي"
        ),
        DhikrItem(
            id = "morning_raditu",
            category = DhikrCategory.MORNING,
            title = "الرضا بالله رباً",
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا.",
            virtueOrNote = "من قالها ثلاثاً حين يصبح وحين يمسي كان حقاً على الله أن يرضيه يوم القيامة.",
            targetCount = 3,
            reference = "أحمد وأبو داود"
        ),
        DhikrItem(
            id = "morning_hasbi",
            category = DhikrCategory.MORNING,
            title = "حسبي الله لا إله إلا هو",
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            virtueOrNote = "من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه.",
            targetCount = 7,
            reference = "أبو داود"
        ),
        DhikrItem(
            id = "morning_hayy_qayyum",
            category = DhikrCategory.MORNING,
            title = "الاستعانة بالحي القيوم",
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ.",
            virtueOrNote = "تفويض الأمر كله إلى الله في إصلاح الشأن.",
            targetCount = 1,
            reference = "النسائي والحاكم"
        ),
        DhikrItem(
            id = "morning_subhanallah",
            category = DhikrCategory.MORNING,
            title = "سبحان الله وبحمده",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            virtueOrNote = "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به.",
            targetCount = 100,
            reference = "صحيح مسلم"
        )
    )

    val eveningItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "evening_kursi",
            category = DhikrCategory.EVENING,
            title = "آية الكرسي",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            virtueOrNote = "من قرأها حين يمسي أُجير من الجن حتى يصبح.",
            targetCount = 1,
            reference = "سورة البقرة: ٢٥٥"
        ),
        DhikrItem(
            id = "evening_muawwidhat",
            category = DhikrCategory.EVENING,
            title = "المعوذات الثلاث",
            arabicText = "قراءة سورة الإخلاص، وسورة الفلق، وسورة الناس (ثلاث مرات لكل سورة).",
            virtueOrNote = "تحصين المسلم وحفظه من كل سوء حتى الصباح.",
            targetCount = 3,
            reference = "أبو داود والترمذي"
        ),
        DhikrItem(
            id = "evening_mulk",
            category = DhikrCategory.EVENING,
            title = "أمسينا وأمسى الملك لله",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا.",
            virtueOrNote = "ذكر افتتاح المساء بالحمد والتوكل وطلب خير الليلة.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "evening_sayyid_istighfar",
            category = DhikrCategory.EVENING,
            title = "سيد الاستغفار",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            virtueOrNote = "من قالها من الليل وهو موقن بها فمات قبل أن يصبح فهو من أهل الجنة.",
            targetCount = 1,
            reference = "صحيح البخاري"
        ),
        DhikrItem(
            id = "evening_kalimat_tammat",
            category = DhikrCategory.EVENING,
            title = "الاستعاذة بكلمات الله التامات",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            virtueOrNote = "من قالها حين يمسي ثلاث مرات لم يضره حمة تلك الليلة.",
            targetCount = 3,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "evening_bismillah_la_yadurr",
            category = DhikrCategory.EVENING,
            title = "التحصين باسم الله",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            virtueOrNote = "حفظ من البلاء المفاجئ حتى الصباح.",
            targetCount = 3,
            reference = "أبو داود والترمذي"
        ),
        DhikrItem(
            id = "evening_subhanallah",
            category = DhikrCategory.EVENING,
            title = "سبحان الله وبحمده",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ.",
            virtueOrNote = "حُطّت خطاياه وإن كانت مثل زبد البحر.",
            targetCount = 100,
            reference = "صحيح مسلم"
        )
    )
}
