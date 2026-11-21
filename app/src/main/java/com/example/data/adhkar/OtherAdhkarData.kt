package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

internal object OtherAdhkarData {

    val afterPrayerItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "prayer_istighfar",
            category = DhikrCategory.AFTER_PRAYER,
            title = "الاستغفار والسلام",
            arabicText = "أَسْتَغْفِرُ اللَّهَ (ثلاثاً)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.",
            virtueOrNote = "أول ما يُبدأ به بعد التسليم من الصلاة المكتوبة.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prayer_tahlil",
            category = DhikrCategory.AFTER_PRAYER,
            title = "كلمة التوحيد والملك",
            arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.",
            virtueOrNote = "كان النبي ﷺ يقولها دبر كل صلاة مكتوبة.",
            targetCount = 1,
            reference = "متفق عليه"
        ),
        DhikrItem(
            id = "prayer_subhanallah",
            category = DhikrCategory.AFTER_PRAYER,
            title = "التسبيح دبر الصلاة",
            arabicText = "سُبْحَانَ اللَّهِ.",
            virtueOrNote = "ثلاثاً وثلاثين مرة عقب كل صلاة مكتوبة.",
            targetCount = 33,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prayer_alhamdulillah",
            category = DhikrCategory.AFTER_PRAYER,
            title = "التحميد دبر الصلاة",
            arabicText = "الْحَمْدُ لِلَّهِ.",
            virtueOrNote = "ثلاثاً وثلاثين مرة عقب كل صلاة مكتوبة.",
            targetCount = 33,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prayer_allahuakbar",
            category = DhikrCategory.AFTER_PRAYER,
            title = "التكبير دبر الصلاة",
            arabicText = "اللَّهُ أَكْبَرُ.",
            virtueOrNote = "ثلاثاً وثلاثين مرة ثم تمام المائة بالتوحيد.",
            targetCount = 33,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prayer_tamam_miah",
            category = DhikrCategory.AFTER_PRAYER,
            title = "تمام المائة",
            arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            virtueOrNote = "غُفرت خطاياه وإن كانت مثل زبد البحر.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "prayer_kursi",
            category = DhikrCategory.AFTER_PRAYER,
            title = "آية الكرسي بعد الصلاة",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            virtueOrNote = "من قرأها دبر كل صلاة مكتوبة لم يمنعه من دخول الجنة إلا أن يموت.",
            targetCount = 1,
            reference = "النسائي"
        )
    )

    val sleepItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "sleep_bismika",
            category = DhikrCategory.SLEEP,
            title = "دعاء وضع الجنب",
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
            virtueOrNote = "تحصين النفس وتفويضها إلى الله عند النوم.",
            targetCount = 1,
            reference = "متفق عليه"
        ),
        DhikrItem(
            id = "sleep_qini_adhabak",
            category = DhikrCategory.SLEEP,
            title = "اللهم قني عذابك",
            arabicText = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ.",
            virtueOrNote = "كان النبي ﷺ يقولها ثلاث مرات واضعاً يده اليمنى تحت خده.",
            targetCount = 3,
            reference = "أبو داود والترمذي"
        ),
        DhikrItem(
            id = "sleep_alhamdulillah",
            category = DhikrCategory.SLEEP,
            title = "الحمد على الكفاية والمأوى",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا، وَكَفَانَا وَآوَانَا، فَكَمْ مِمَّنْ لَا كَافِيَ لَهُ وَلَا مُؤْوِيَ.",
            virtueOrNote = "شكر الله على نعمه قبل المبيت.",
            targetCount = 1,
            reference = "صحيح مسلم"
        ),
        DhikrItem(
            id = "sleep_subhanallah",
            category = DhikrCategory.SLEEP,
            title = "تسبيح النوم (١ / ٣)",
            arabicText = "سُبْحَانَ اللَّهِ.",
            virtueOrNote = "ثلاثاً وثلاثين مرة عند أخذ المضجع؛ خيرٌ من خادم.",
            targetCount = 33,
            reference = "متفق عليه"
        ),
        DhikrItem(
            id = "sleep_tasbih_alhamdulillah",
            category = DhikrCategory.SLEEP,
            title = "تحميد النوم (٢ / ٣)",
            arabicText = "الْحَمْدُ لِلَّهِ.",
            virtueOrNote = "ثلاثاً وثلاثين مرة عند النوم اقتداءً بوصية النبي ﷺ.",
            targetCount = 33,
            reference = "متفق عليه"
        ),
        DhikrItem(
            id = "sleep_allahuakbar",
            category = DhikrCategory.SLEEP,
            title = "تكبير النوم (٣ / ٣)",
            arabicText = "اللَّهُ أَكْبَرُ.",
            virtueOrNote = "أربعاً وثلاثين مرة تماماً للمئة قبل النوم.",
            targetCount = 34,
            reference = "متفق عليه"
        ),
        DhikrItem(
            id = "sleep_aslamtu_wajhi",
            category = DhikrCategory.SLEEP,
            title = "دعاء تسليم النفس",
            arabicText = "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَوَجَّهْتُ وَجْهِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، رَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ.",
            virtueOrNote = "فإن مات من ليلته مات على الفطرة.",
            targetCount = 1,
            reference = "متفق عليه"
        )
    )

    val tasbihItems: List<DhikrItem> = listOf(
        DhikrItem(
            id = "tasbih_subhanallah",
            category = DhikrCategory.TASBIH,
            title = "التسبيح",
            arabicText = "سُبْحَانَ اللَّهِ.",
            virtueOrNote = "تُكتب له ألف حسنة أو يُحطّ عنه ألف خطيئة لمن سبّح مائة تسبيحة.",
            targetCount = 33,
            reference = "صحيح مسلم",
            chapterTitle = "التسبيح والتحميد والتكبير"
        ),
        DhikrItem(
            id = "tasbih_alhamdulillah",
            category = DhikrCategory.TASBIH,
            title = "التحميد",
            arabicText = "الْحَمْدُ لِلَّهِ.",
            virtueOrNote = "الحمد لله تملأ الميزان.",
            targetCount = 33,
            reference = "صحيح مسلم",
            chapterTitle = "التسبيح والتحميد والتكبير"
        ),
        DhikrItem(
            id = "tasbih_allahuakbar",
            category = DhikrCategory.TASBIH,
            title = "التكبير",
            arabicText = "اللَّهُ أَكْبَرُ.",
            virtueOrNote = "سبحان الله والحمد لله تملآن ما بين السماوات والأرض.",
            targetCount = 33,
            reference = "صحيح مسلم",
            chapterTitle = "التسبيح والتحميد والتكبير"
        ),
        DhikrItem(
            id = "tasbih_kalimatan",
            category = DhikrCategory.TASBIH,
            title = "كلمتان خفيفتان على اللسان",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ.",
            virtueOrNote = "كلمتان حبيبتان إلى الرحمن، ثقيلتان في الميزان.",
            targetCount = 33,
            reference = "متفق عليه",
            chapterTitle = "الباقيات الصالحات وكنوز الجنة"
        ),
        DhikrItem(
            id = "tasbih_baqiyat",
            category = DhikrCategory.TASBIH,
            title = "الباقيات الصالحات",
            arabicText = "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ.",
            virtueOrNote = "أحب الكلام إلى الله وأعظمه أجراً.",
            targetCount = 33,
            reference = "صحيح مسلم",
            chapterTitle = "الباقيات الصالحات وكنوز الجنة"
        ),
        DhikrItem(
            id = "tasbih_hawqalah",
            category = DhikrCategory.TASBIH,
            title = "كنز من كنوز الجنة",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ.",
            virtueOrNote = "كنز من كنوز الجنة وتفويض للحول والقوة.",
            targetCount = 33,
            reference = "متفق عليه",
            chapterTitle = "الباقيات الصالحات وكنوز الجنة"
        ),
        DhikrItem(
            id = "tasbih_yunus",
            category = DhikrCategory.TASBIH,
            title = "دعوة ذي النون",
            arabicText = "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ.",
            virtueOrNote = "لم يدعُ بها رجل مسلم في شيء قط إلا استجاب الله له.",
            targetCount = 10,
            reference = "سورة الأنبياء: ٨٧",
            chapterTitle = "جوامع الاستغفار والصلاة على النبي ﷺ"
        ),
        DhikrItem(
            id = "tasbih_salat_alanabi",
            category = DhikrCategory.TASBIH,
            title = "الصلاة على النبي ﷺ",
            arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ.",
            virtueOrNote = "من صلى عليَّ صلاة صلى الله عليه بها عشراً.",
            targetCount = 10,
            reference = "صحيح مسلم",
            chapterTitle = "جوامع الاستغفار والصلاة على النبي ﷺ"
        ),
        DhikrItem(
            id = "tasbih_istighfar_kabir",
            category = DhikrCategory.TASBIH,
            title = "الاستغفار والتوبة",
            arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ.",
            virtueOrNote = "غُفر له وإن كان قد فرّ من الزحف.",
            targetCount = 3,
            reference = "أبو داود والترمذي",
            chapterTitle = "جوامع الاستغفار والصلاة على النبي ﷺ"
        )
    )
}
