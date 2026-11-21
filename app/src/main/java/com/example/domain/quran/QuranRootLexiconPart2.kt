package com.example.domain.quran

internal object QuranRootLexiconPart2 {
    val entries: List<QuranRootSpec> = listOf(
        QuranRootSpec("برك", "البركة", stems = listOf("برك", "مبارك", "تبارك", "يبارك", "بارك"), aliases = listOf("البركة", "مبارك", "تبارك", "بركات")),
        QuranRootSpec("طهر", "الطهارة", stems = listOf("طهر", "طاهر", "مطهر", "تطهر", "يطهر", "اطهر"), aliases = listOf("طهارة", "المطهرون", "يطهركم", "أطهر")),
        QuranRootSpec(
            root = "نور", displayLabel = "النور",
            stems = listOf("نور", "منير", "انار", "نار"),
            exactWords = listOf("نور", "نورا", "النور", "نوره", "نورهم", "نوركم", "منير", "منيرا", "انوار"),
            aliases = listOf("النور", "نورا", "منيرا"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "حيي", displayLabel = "الحياة والإحياء",
            stems = listOf("حيي", "حيا", "محي", "احي", "يحي", "تستحي", "استحي"),
            exactWords = listOf("حي", "حيا", "الحي", "احياء", "احيا", "يحيي", "محيي", "حياه", "الحياه", "حياتنا", "حياته", "حياتهم", "تحيا", "حيوا", "تحيه", "احياكم", "احيانا"),
            aliases = listOf("الحياة", "الحي", "يحيي", "أحياء"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec(
            root = "موت", displayLabel = "الموت",
            stems = listOf("موت", "ميت", "ميت", "امات", "يموت", "تموت", "نموت"),
            exactWords = listOf("مات", "ماتوا", "مت", "متنا", "متم", "موت", "الموت", "موتا", "موته", "موتهم", "موتكم", "يموت", "يموتون", "تموت", "تموتون", "نموت", "ميت", "ميتا", "الميتي", "اموات", "موتي", "الموتي", "يميت", "امات", "مميت"),
            aliases = listOf("الموت", "يموت", "أموات", "ميت"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec("خلق", "الخلق", stems = listOf("خلق", "خالق", "خلاق", "مخلوق", "يخلق", "تخلق", "نخلق"), aliases = listOf("الخلق", "الخالق", "خلقنا")),
        QuranRootSpec("بعث", "البعث", stems = listOf("بعث", "باعث", "مبعوث", "يبعث", "تبعث", "نبعث", "ابعث", "انبعث"), aliases = listOf("البعث", "يبعث", "مبعوثون")),
        QuranRootSpec("نزل", "النزول والتنزيل", stems = listOf("نزل", "نازل", "منزل", "تنزيل", "انزل", "ينزل", "تنزل", "ننزل"), aliases = listOf("أنزلنا", "تنزيل", "ينزل", "منزلين")),
        QuranRootSpec("قرأ", "القراءة والقرآن", stems = listOf("قرا", "قران", "قارئ", "يقرا", "تقرا", "نقرا", "اقرا"), aliases = listOf("القرآن", "اقرأ", "يقرأون", "قرآنا")),
        QuranRootSpec("كتب", "الكتاب والكتابة", stems = listOf("كتب", "كاتب", "كتاب", "مكتوب", "يكتب", "تكتب", "نكتب", "اكتب"), aliases = listOf("الكتاب", "كتب", "يكتبون")),
        QuranRootSpec("حكم", "الحكم والحكمة", stems = listOf("حكم", "حاكم", "حكيم", "حكمه", "محكم", "يحكم", "تحكم", "نحكم", "احكم"), aliases = listOf("الحكمة", "الحكيم", "يحكم", "أحكم")),
        QuranRootSpec(
            root = "قضي", displayLabel = "القضاء",
            stems = listOf("قضي", "قاض", "مقضي", "يقض", "تقض", "نقض"),
            exactWords = listOf("قضي", "قضيت", "قضينا", "قضوا", "قضاء", "يقضي", "تقضي", "نقضي", "اقض", "القاضي", "قاض", "مقضيا"),
            aliases = listOf("قضى", "يقضي", "قضاء"),
            allowAlgorithmicFallback = false
        ),
        QuranRootSpec("ملك", "الملك والملكوت", stems = listOf("ملك", "مالك", "مليك", "مملوك", "ملكوت", "يملك", "تملك", "نملك", "املك"), aliases = listOf("الملك", "مالك", "الملائكة", "ملكوت")),
        QuranRootSpec("عبد", "العبادة والعبودية", stems = listOf("عبد", "عابد", "عباد", "عبود", "معبود", "يعبد", "تعبد", "نعبد", "اعبد"), aliases = listOf("العبادة", "نعبد", "عبادي", "عبد")),
        QuranRootSpec("سبح", "التسبيح", stems = listOf("سبح", "سابح", "مسبح", "تسبيح", "سبحان", "يسبح", "تسبح", "نسبح", "سبح"), aliases = listOf("سبحان", "يسبح", "سبح", "المسبحين")),
        QuranRootSpec("حمد", "الحمد", stems = listOf("حمد", "حامد", "حميد", "محمود", "احمد", "محمد", "يحمد", "تحمد"), aliases = listOf("الحمد", "حميد", "محمد", "احمد")),
        QuranRootSpec("سجد", "السجود", stems = listOf("سجد", "ساجد", "مسجد", "مساجد", "سجود", "يسجد", "تسجد", "نسجد", "اسجد"), aliases = listOf("السجود", "اسجد", "المسجد", "الساجدين")),
        QuranRootSpec("ركع", "الركوع", stems = listOf("ركع", "راكع", "ركوع", "يركع", "تركع", "نركع", "اركع"), aliases = listOf("الركوع", "الراكعين", "اركعوا")),
        QuranRootSpec("عمل", "العمل", stems = listOf("عمل", "عامل", "اعمال", "يعمل", "تعمل", "نعمل", "اعمل"), aliases = listOf("العمل", "يعملون", "أعمال", "عاملين")),
        QuranRootSpec("سمع", "السمع", stems = listOf("سمع", "سامع", "سميع", "سماع", "اسمع", "يسمع", "تسمع", "نسمع", "استمع", "مستمع"), aliases = listOf("السميع", "يسمعون", "اسمع")),
        QuranRootSpec("بصر", "البصر والبصيرة", stems = listOf("بصر", "باصر", "بصير", "ابصار", "مبصر", "بصائر", "يبصر", "تبصر", "نبصر"), aliases = listOf("البصير", "أبصار", "بصيرة", "يبصرون")),
        QuranRootSpec("عقل", "العقل والتعقل", stems = listOf("عقل", "عاقل", "معقول", "يعقل", "تعقل", "نعقل"), aliases = listOf("تعقلون", "يعقلون", "عقلوه")),
        QuranRootSpec("فكر", "التفكر", stems = listOf("فكر", "متفكر", "يتفكر", "تتفكر", "فكرت"), aliases = listOf("يتفكرون", "تتفكرون", "التفكر")),
        QuranRootSpec("دبر", "التدبر والإدبار", stems = listOf("دبر", "مدبر", "ادبار", "يتدبر", "يدبر", "تدبر"), aliases = listOf("يتدبرون", "يدبر", "تدبر")),
        QuranRootSpec("قلب", "القلب والتقلب", stems = listOf("قلب", "قلوب", "منقلب", "يتقلب", "تقلب", "نقلب", "يقلب", "اقلب"), aliases = listOf("القلب", "القلوب", "يتقلب")),
        QuranRootSpec("نفس", "النفس", stems = listOf("نفس", "انفس", "نفوس", "تنفس", "يتنافس", "متنافس"), aliases = listOf("النفس", "أنفسهم", "نفوس")),
        QuranRootSpec("روح", "الروح والراحة", stems = listOf("روح", "رياح", "ريح", "ارواح", "ريحان", "تروح"), aliases = listOf("الروح", "رياح", "ريحان")),
        QuranRootSpec("وكل", "التوكل والوكالة", stems = listOf("وكل", "وكيل", "متوكل", "يتوكل", "توكل", "نتوكل"), aliases = listOf("التوكل", "توكلت", "وكيلا", "المتوكلين")),
        QuranRootSpec("يقن", "اليقين", stems = listOf("يقن", "موقن", "يوقن", "توقن", "يقين"), aliases = listOf("اليقين", "يوقنون", "الموقنين")),
        QuranRootSpec("خشع", "الخشوع", stems = listOf("خشع", "خاشع", "خشوع", "يخشع", "تخشع"), aliases = listOf("الخشوع", "الخاشعين", "تخشع")),
        QuranRootSpec("خلص", "الإخلاص", stems = listOf("خلص", "خالص", "مخلص", "اخلص", "يخلص", "استخلص"), aliases = listOf("مخلصين", "خالصة", "الإخلاص")),
        QuranRootSpec("شهد", "الشهادة", stems = listOf("شهد", "شاهد", "شهيد", "شهاد", "مشهود", "يشهد", "تشهد", "نشهد", "اشهد"), aliases = listOf("شهيد", "الشهادة", "اشهد")),
        QuranRootSpec("عهد", "العهد", stems = listOf("عهد", "عاهد", "عهود", "يعهد", "تعهد"), aliases = listOf("العهد", "عاهدوا", "عهدهم")),
        QuranRootSpec("وفي", "الوفاء", stems = listOf("وفي", "واف", "اوف", "يوف", "توف", "نوف", "يتوف"), exactWords = listOf("وفي", "اوفوا", "يوفون", "توفي", "يتوفي", "يتوفاكم", "توفني", "وفاهم", "موفوهم", "اوفي"), aliases = listOf("أوفوا", "الوفاء", "يتوفى"), allowAlgorithmicFallback = false),
        QuranRootSpec("صلح", "الصلاح والإصلاح", stems = listOf("صلح", "صالح", "مصلح", "اصلاح", "يصلح", "تصلح", "نصلح", "اصلح"), aliases = listOf("الصالحات", "الصالحين", "أصلح", "إصلاح")),
        QuranRootSpec("فسد", "الفساد", stems = listOf("فسد", "فاسد", "مفسد", "فساد", "يفسد", "تفسد", "افسد"), aliases = listOf("الفساد", "المفسدين", "تفسدوا")),
        QuranRootSpec("حسن", "الإحسان والحسن", stems = listOf("حسن", "احسن", "محسن", "حسني", "حسنه", "حسنات", "يحسن", "تحسن"), aliases = listOf("الإحسان", "أحسن", "المحسنين", "حسنة")),
        QuranRootSpec("سوأ", "السوء", stems = listOf("سوا", "سيئ", "اسي", "يسو", "تسؤ"), exactWords = listOf("سوء", "سوءا", "السوء", "سيئه", "سيئات", "السيئات", "اساء", "اساءوا", "ساء", "ساءت", "تسؤهم", "يسؤكم"), aliases = listOf("السوء", "السيئات", "ساءت"), allowAlgorithmicFallback = false),
        QuranRootSpec("كبر", "الكبرياء والكبر", stems = listOf("كبر", "كبير", "اكبر", "كبار", "متكبر", "مستكبر", "تكبر", "استكبر", "يكبر", "كبر"), aliases = listOf("الكبير", "أكبر", "المتكبر", "استكبر")),
        QuranRootSpec("عظم", "العظمة", stems = listOf("عظم", "عظيم", "اعظم", "عظام", "يعظم", "تعظم"), aliases = listOf("العظيم", "عظيما", "أعظم")),
        QuranRootSpec("يسر", "اليسر والتيسير", stems = listOf("يسر", "يسير", "ميسور", "ميسر", "تيسر", "يتيسر", "نيسر", "يسر"), aliases = listOf("اليسر", "يسرا", "يسيرا", "يسرنا")),
        QuranRootSpec("عسر", "العسر", stems = listOf("عسر", "عسير", "عسره", "تعاسر"), aliases = listOf("العسر", "عسرا", "عسيرا")),
        QuranRootSpec("شفي", "الشفاء", stems = listOf("شفي", "شاف", "يشف", "تشف"), exactWords = listOf("شفاء", "يشفين", "يشف", "شفا"), aliases = listOf("شفاء", "يشفين"), allowAlgorithmicFallback = false),
        QuranRootSpec("كشف", "الكشف", stems = listOf("كشف", "كاشف", "مكشوف", "يكشف", "تكشف", "نكشف", "اكشف"), aliases = listOf("كاشف", "اكشف", "يكشف")),
        QuranRootSpec("بشر", "البشارة", stems = listOf("بشر", "بشير", "مبشر", "بشري", "يبشر", "تبشر", "نبشر", "بشر"), aliases = listOf("بشرى", "بشيرا", "مبشرين", "أبشروا")),
        QuranRootSpec("نذر", "النذارة والنذر", stems = listOf("نذر", "نذير", "منذر", "انذر", "ينذر", "تنذر", "ننذر"), aliases = listOf("نذيرا", "أنذر", "المنذرين")),
        QuranRootSpec("وعد", "الوعد", stems = listOf("وعد", "واعد", "موعد", "ميعاد", "موعود", "يعد", "تعد", "نعد"), exactWords = listOf("وعد", "وعدا", "الوعد", "وعده", "وعدهم", "وعدنا", "وعدكم", "وعدت", "يعد", "يعدهم", "تعدهم", "نعدهم", "موعد", "ميعاد", "الميعاد", "موعود", "الوعيد", "وعيد"), aliases = listOf("الوعد", "الميعاد", "وعد"), allowAlgorithmicFallback = false),
        QuranRootSpec("جنن", "الجنة والجن", stems = listOf("جنن", "جنه", "جنات", "جان", "مجنون"), exactWords = listOf("جنه", "جنتان", "جنتين", "جنات", "الجنات", "الجنه", "الجن", "جن", "مجنون", "جنة"), aliases = listOf("الجنة", "جنات", "الجن"), allowAlgorithmicFallback = false),
        QuranRootSpec("نير", "النار", stems = listOf("نير", "نار"), exactWords = listOf("نار", "نارا", "النار", "ناركم", "ناره", "نارهم"), aliases = listOf("النار", "نارا"), allowAlgorithmicFallback = false),
        QuranRootSpec("سمو", "السماء والاسم", stems = listOf("سمو", "سماء", "سماوات", "سموات", "اسم", "اسماء", "مسمي"), exactWords = listOf("سماء", "السماء", "سماوات", "سموات", "السماوات", "السموات", "اسم", "اسمه", "اسماء", "الاسماء", "سمي", "مسمي"), aliases = listOf("السماء", "السماوات", "الأسماء"), allowAlgorithmicFallback = false),
        QuranRootSpec("ارض", "الأرض", stems = listOf("ارض"), exactWords = listOf("ارض", "ارضا", "الارض", "ارضه", "ارضهم", "ارضكم", "ارضي"), aliases = listOf("الأرض"), allowAlgorithmicFallback = false),
        QuranRootSpec("بحر", "البحر", stems = listOf("بحر", "بحار", "بحور", "بحرين"), aliases = listOf("البحر", "البحار")),
        QuranRootSpec("شمس", "الشمس", stems = listOf("شمس"), aliases = listOf("الشمس")),
        QuranRootSpec("قمر", "القمر", stems = listOf("قمر"), aliases = listOf("القمر")),
        QuranRootSpec("ليل", "الليل", stems = listOf("ليل"), exactWords = listOf("ليل", "ليلا", "الليل", "ليله", "ليال", "الليالي"), aliases = listOf("الليل", "ليلة", "ليال"), allowAlgorithmicFallback = false),
        QuranRootSpec("نهر", "النهار والأنهار", stems = listOf("نهار", "انهر", "انهار", "نهر"), aliases = listOf("النهار", "الأنهار", "نهر")),
        QuranRootSpec("موه", "الماء", stems = listOf("ماء", "امواه"), exactWords = listOf("ماء", "الماء"), aliases = listOf("الماء", "ماء"), allowAlgorithmicFallback = false),
        QuranRootSpec("خير", "الخير", stems = listOf("خير", "خيار", "اخيار"), aliases = listOf("الخير", "خيرا", "الأخيار")),
        QuranRootSpec("شرر", "الشر", stems = listOf("شرر", "اشرار"), exactWords = listOf("شر", "شرا", "الشر", "شره", "اشرار", "الاشرار", "شرر"), aliases = listOf("الشر", "شرا"), allowAlgorithmicFallback = false),
        QuranRootSpec("غني", "الغنى", stems = listOf("غني", "اغني", "يغني", "تغني", "استغني", "مغني"), aliases = listOf("الغني", "أغنى", "يغنيهم")),
        QuranRootSpec("فقر", "الفقر", stems = listOf("فقر", "فقير", "فقراء"), aliases = listOf("الفقر", "الفقراء", "فقير")),
        QuranRootSpec("فوز", "الفوز", stems = listOf("فوز", "فائز", "مفاز", "يفوز"), exactWords = listOf("فاز", "فازوا", "فوز", "فوزا", "الفوز", "فائزون", "الفائزون", "الفائزين", "مفازا", "مفازه", "يفز"), aliases = listOf("الفوز", "فوزا", "الفائزون"), allowAlgorithmicFallback = false),
        QuranRootSpec("خسر", "الخسران", stems = listOf("خسر", "خاسر", "خسران", "يخسر", "تخسر"), aliases = listOf("الخاسرين", "خسران")),
        QuranRootSpec("نجو", "النجاة", stems = listOf("نجو", "ناج", "منج", "ينج", "تنج", "ننج", "انج"), exactWords = listOf("نجا", "نجاه", "نجينا", "نجيناه", "نجيناهم", "ينجي", "ينجيكم", "تنجي", "ننجي", "انجنا", "ناج"), aliases = listOf("النجاة", "نجينا", "ينجي"), allowAlgorithmicFallback = false),
        QuranRootSpec("ضلل", "الضلال", stems = listOf("ضلل", "ضال", "مضل", "ضلال", "يضل", "تضل", "نضل", "اضل"), aliases = listOf("الضلال", "الضالين", "يضل")),
        QuranRootSpec("كفر", "الكفر", stems = listOf("كفر", "كافر", "كفار", "كفور", "يكفر", "تكفر", "نكفر", "كفره"), aliases = listOf("الكفر", "الكافرين", "كفروا")),
        QuranRootSpec("شرك", "الشرك", stems = listOf("شرك", "مشرك", "شريك", "شركاء", "يشرك", "تشرك", "نشرك", "اشرك"), aliases = listOf("الشرك", "المشركين", "شركاء")),
        QuranRootSpec("حسد", "الحسد", stems = listOf("حسد", "حاسد", "يحسد"), aliases = listOf("حاسد", "يحسدون")),
        QuranRootSpec("برر", "البر", stems = listOf("برر", "ابرار", "بار"), exactWords = listOf("بر", "برا", "البر", "بره", "ابرار", "الابرار", "بار", "تبروا", "تبر"), aliases = listOf("البر", "الأبرار", "برا"), allowAlgorithmicFallback = false),
        QuranRootSpec("ولد", "الوالدان والأولاد", stems = listOf("ولد", "والد", "مولود", "اولاد", "يلد", "تلد", "وليد"), aliases = listOf("الوالدين", "والديه", "أولادكم", "ولد")),
        QuranRootSpec("اهل", "الأهل", stems = listOf("اهل"), aliases = listOf("الأهل", "أهله", "أهلها")),
        QuranRootSpec("زوج", "الأزواج والزواج", stems = listOf("زوج", "ازواج", "تزوج", "يزوج"), aliases = listOf("أزواج", "زوجها", "أزواجهم")),
        QuranRootSpec("مول", "المال والأموال", stems = listOf("مال", "اموال"), exactWords = listOf("مال", "مالا", "المال", "ماله", "مالهم", "اموال", "اموالكم", "اموالهم", "الاموال"), aliases = listOf("المال", "الأموال", "أموالهم"), allowAlgorithmicFallback = false),
        QuranRootSpec("دني", "الدنيا", stems = listOf("دنيا", "ادني", "داني"), exactWords = listOf("دنيا", "الدنيا", "ادني", "دانيه"), aliases = listOf("الدنيا", "أدنى"), allowAlgorithmicFallback = false),
        QuranRootSpec("اخر", "الآخرة", stems = listOf("اخر", "اخره"), exactWords = listOf("اخر", "اخري", "الاخر", "الاخره", "اخره", "اخرون", "اخرين", "الآخره"), aliases = listOf("الآخرة", "آخر"), allowAlgorithmicFallback = false),
        QuranRootSpec("أمر", "الأمر", stems = listOf("امر", "امرون", "اماره", "يامر", "تام", "نامر"), exactWords = listOf("امر", "امرا", "الامر", "امره", "امرهم", "امركم", "امرنا", "امور", "الامور", "يامر", "يامرون", "تامرن", "امرت", "امرني", "امارة"), aliases = listOf("الأمر", "يأمرون", "الأمور"), allowAlgorithmicFallback = false),
        QuranRootSpec("نهي", "النهي", stems = listOf("نهي", "ناه", "منته", "ينه", "تنه", "ننه", "انته"), exactWords = listOf("نهي", "نهيت", "نهوا", "ينهي", "ينهون", "تنهي", "تنهون", "ننهي", "الناهون", "انتهوا", "ينتهوا", "منتهون"), aliases = listOf("ينهى", "النهي", "انتهوا"), allowAlgorithmicFallback = false),
        QuranRootSpec("وهب", "الهبة والوهاب", stems = listOf("وهب", "وهاب", "يهب", "تهب", "نهب", "هب"), exactWords = listOf("وهب", "وهبنا", "وهبت", "وهاب", "الوهاب", "يهب", "هب", "هبه"), aliases = listOf("الوهاب", "وهبنا", "هب"), allowAlgorithmicFallback = false),
        QuranRootSpec("عفو", "العفو", stems = listOf("عفو", "عاف", "عفوا", "يعف", "تعف", "اعف"), exactWords = listOf("عفا", "عفوا", "عفو", "يعف", "يعفو", "يعفون", "تعفوا", "اعف", "العافين", "عفو"), aliases = listOf("العفو", "اعف", "العافين"), allowAlgorithmicFallback = false),
        QuranRootSpec("سأل", "السؤال", stems = listOf("سال", "سائل", "مسؤول", "يسال", "تسال", "نسال"), exactWords = listOf("سال", "سالوا", "سالت", "سالوك", "سالتم", "يسال", "يسالون", "يسالونك", "تسال", "نسال", "سائل", "للسائل", "سؤال", "مسئولا", "مسؤولون"), aliases = listOf("يسألونك", "سأل", "السائل"), allowAlgorithmicFallback = false),
        QuranRootSpec("جوب", "الإجابة", stems = listOf("جوب", "اجاب", "يجيب", "تجيب", "نجيب", "مجيب", "استجاب", "يستجيب"), exactWords = listOf("اجاب", "اجابوا", "فاجاب", "يجيب", "نجيب", "مجيب", "استجاب", "استجابوا", "يستجيب", "يستجيبوا", "فليستجيبوا"), aliases = listOf("استجاب", "مجيب", "أجيب"), allowAlgorithmicFallback = false)
    )
}

internal object QuranRootLexicon {
    val popularRoots: List<Pair<String, String>> = listOf(
        "رحم" to "الرحمة",
        "علم" to "العلم",
        "صبر" to "الصبر",
        "ذكر" to "الذكر",
        "هدي" to "الهداية",
        "رزق" to "الرزق",
        "شكر" to "الشكر",
        "توب" to "التوبة"
    )

    val allSpecs: List<QuranRootSpec> by lazy {
        QuranRootLexiconPart1.entries + QuranRootLexiconPart2.entries
    }

    val specByRoot: Map<String, QuranRootSpec> by lazy {
        allSpecs.associateBy { normalizeToken(it.root) }
    }

    val stopwords: Set<String> = setOf(
        "في", "من", "الي", "علي", "عن", "ان", "ما", "لا", "لم", "لن", "قد", "هل",
        "بل", "ثم", "او", "ام", "مع", "عند", "بين", "حتي", "كل", "بعض", "غير", "سوي",
        "اي", "اذا", "اذ", "لو", "لولا", "كما", "الذي", "التي", "الذين", "اللاتي",
        "اللائي", "هو", "هي", "هم", "هن", "انت", "انتم", "انتن", "انا", "نحن",
        "ذلك", "هذا", "هذه", "اولئك", "تلك", "كان", "كانت", "كانوا", "يكون",
        "تكون", "ليس", "ليست", "ولكن", "لكن", "يا", "ايها", "ايتها", "الا",
        "فان", "وان", "بان", "لان", "كان", "وكذلك", "اولم", "افلا", "اوليس",
        "فلا", "ولا", "وما", "بما", "لما", "عما", "مما", "فيما", "كلا", "لعل"
    )

    private val prefixes = listOf("وال", "فال", "بال", "كال", "ولل", "فلل", "لل", "ال", "و", "ف", "ب", "ك", "ل", "س")
    private val suffixes = listOf(
        "تموها", "كموه", "ناهم", "ناكم", "تمان", "ونني", "ينني",
        "كم", "هم", "هن", "نا", "ها", "تم", "تن", "ون", "ين", "ان", "ات", "وا", "ه", "ك", "ي", "ت"
    )

    fun normalizeToken(raw: String): String {
        return raw
            .replace(Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640۞۩۝،؛؟.,:;()\\[\\]{}]"), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .trim()
    }

    fun resolveRootQuery(rawQuery: String): String {
        val compact = normalizeToken(rawQuery).replace(" ", "")
        if (compact.isEmpty()) return ""
        specByRoot[compact]?.let { return it.root }
        for (spec in allSpecs) {
            if (spec.aliases.any { normalizeToken(it) == compact }) return spec.root
            if (spec.exactWords.any { normalizeToken(it) == compact }) return spec.root
        }
        if (compact.length == 3) return compact
        return extractAlgorithmicRoot(compact)
    }

    fun wordMatchesRoot(normalizedWord: String, targetRootNorm: String, spec: QuranRootSpec?): Boolean {
        if (normalizedWord.length < 2 || normalizedWord in stopwords) return false
        if (spec != null) {
            val bare = stripClitics(normalizedWord)
            if (spec.exactWords.any { ex ->
                    val nex = normalizeToken(ex)
                    normalizedWord == nex || bare == nex
                }
            ) return true
            if (spec.stems.any { st ->
                    val nst = normalizeToken(st)
                    nst.length >= 3 && (bare == nst || bare.startsWith(nst) || (bare.length <= 7 && bare.contains(nst)))
                }
            ) return true
            if (!spec.allowAlgorithmicFallback) return false
        }
        return matchesAlgorithmicRoot(normalizedWord, targetRootNorm)
    }

    fun extractAlgorithmicRoot(normalizedWord: String): String {
        var w = stripClitics(normalizedWord)
        if (w.length <= 3) return w
        if (w.startsWith("است") && w.length >= 6) w = w.substring(3)
        else if (w.startsWith("مت") && w.length >= 5) w = w.substring(2)
        else if (w.startsWith("تت") && w.length >= 5) w = w.substring(2)
        else if ((w.startsWith("م") || w.startsWith("ت") || w.startsWith("ي") || w.startsWith("ن") || w.startsWith("ا")) && w.length >= 4) {
            w = w.substring(1)
        }
        val filtered = buildString {
            w.forEachIndexed { idx, c ->
                val isLikelyInfix = (c == 'ا' || c == 'و' || c == 'ي' || c == 'ت' || c == 'ن') && w.length > 3 && idx in 1..(w.length - 2)
                if (!isLikelyInfix || length + (w.length - idx) <= 3) append(c)
            }
        }
        return when {
            filtered.length == 3 -> filtered
            filtered.length > 3 -> filtered.take(3)
            w.length >= 3 -> w.take(3)
            else -> normalizedWord.take(3)
        }
    }

    private fun stripClitics(word: String): String {
        var w = word
        for (p in prefixes) {
            if (w.startsWith(p) && w.length - p.length >= 3) {
                w = w.substring(p.length)
                break
            }
        }
        for (s in suffixes) {
            if (w.endsWith(s) && w.length - s.length >= 3) {
                w = w.substring(0, w.length - s.length)
                break
            }
        }
        return w
    }

    private fun matchesAlgorithmicRoot(normalizedWord: String, targetRootNorm: String): Boolean {
        if (targetRootNorm.length < 3) return false
        val stripped = stripClitics(normalizedWord)
        if (stripped.length < 3) return false
        if (extractAlgorithmicRoot(normalizedWord) == targetRootNorm) return true
        // Ordered subsequence check on stripped stem within compact morphological window (<= 6 chars)
        if (stripped.length > 6) return false
        var rIdx = 0
        for (ch in stripped) {
            if (rIdx < targetRootNorm.length && ch == targetRootNorm[rIdx]) {
                rIdx++
            }
        }
        return rIdx == targetRootNorm.length
    }
}
