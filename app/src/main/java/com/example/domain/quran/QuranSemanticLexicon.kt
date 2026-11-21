package com.example.domain.quran

internal data class SemanticConceptDef(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val triggers: List<String>,
    val matchStems: List<String>,
    val relatedRoots: List<String> = emptyList()
)

internal object QuranSemanticLexicon {
    val popularMeanings: List<String> = listOf(
        "السكينة والقلق",
        "بر الوالدين",
        "الرزق والبركة",
        "الصبر والفرج",
        "التوبة والمغفرة",
        "الشفاء والعافية",
        "الدعاء والاستجابة",
        "العلم والتدبر"
    )

    val concepts: List<SemanticConceptDef> = listOf(
        SemanticConceptDef(
            id = "sakinah",
            titleAr = "السكينة والطمأنينة وعلاج القلق",
            descriptionAr = "آيات انشراح الصدر وتفريج الهمّ وطمأنينة القلب بذكر الله",
            triggers = listOf("سكينه", "طمأنينه", "قلق", "حزن", "هم", "ضيق", "خوف", "اكتئاب", "توتر", "راحه", "تثبيت", "انشراح"),
            matchStems = listOf("تطمئن", "اطمئن", "سكينه", "يشرح", "اشرح", "تخف", "تحزن", "لا خوف", "يسرا", "مع العسر", "فارج", "كاشف"),
            relatedRoots = listOf("ذكر", "يسر", "كشف")
        ),
        SemanticConceptDef(
            id = "parents",
            titleAr = "برّ الوالدين وصلة الرحم والأسرة",
            descriptionAr = "الوصية بالوالدين وإحسان القول والفعل، وحقوق الزوجين والأولاد",
            triggers = listOf("بر", "والدين", "ام", "اب", "ابوي", "اسره", "رحم", "ارحام", "اولاد", "ذريه", "زواج", "عائله"),
            matchStems = listOf("بالوالدين", "والديه", "لوالدي", "لا تقل لهما اف", "واخفض لهما", "ذوي القربي", "الارحام", "من ازواجنا", "وذرياتنا", "قرره اعين", "موده ورحمه"),
            relatedRoots = listOf("ولد", "زوج", "برر")
        ),
        SemanticConceptDef(
            id = "rizq",
            titleAr = "الرزق والبركة والكسب الحلال",
            descriptionAr = "سعة رزق الله لعباده وأسباب البركة والإنفاق والتوكل في طلب المعاش",
            triggers = listOf("رزق", "بركه", "مال", "غني", "فقر", "عمل", "تجاره", "وظيفه", "دين", "معيشه", "كسب"),
            matchStems = listOf("يرزق", "ارزق", "رزقنا", "رزقا", "بغير حساب", "يبسط الرزق", "بركات", "طيبات", "طيبا", "ومما رزقناهم", "يغنيهم"),
            relatedRoots = listOf("رزق", "برك", "نفق", "غني")
        ),
        SemanticConceptDef(
            id = "sabr",
            titleAr = "الصبر والابتلاء وحسن العاقبة",
            descriptionAr = "الثبات عند الشدائد وبشارة الصابرين وعاقبة التحمل والرضا",
            triggers = listOf("صبر", "ابتلاء", "بلاء", "مصيبه", "شدة", "شدايد", "فرج", "محنه", "تحمل", "ثبات"),
            matchStems = listOf("الصابرين", "اصبر", "واصبر", "وبشر الصابرين", "ان مع العسر", "انا لله", "بلوناكم", "ونبلوكم", "عاقبه"),
            relatedRoots = listOf("صبر", "يسر", "عسر")
        ),
        SemanticConceptDef(
            id = "tawbah",
            titleAr = "التوبة والاستغفار وسعة المغفرة",
            descriptionAr = "نداء الرجوع إلى الله وعدم القنوط وتبديل السيئات حسنات",
            triggers = listOf("توبه", "مغفره", "استغفار", "ذنب", "ذنوب", "معصيه", "خطيئه", "رجوع", "ندم", "قنوط"),
            matchStems = listOf("يغفر", "غفور", "استغفر", "لا تقنطوا", "توبوا", "التواب", "يكفر", "تبديل", "عفا", "غفار"),
            relatedRoots = listOf("توب", "غفر", "عفو")
        ),
        SemanticConceptDef(
            id = "shifa",
            titleAr = "الشفاء والعافية ورفع الضر",
            descriptionAr = "آيات الشفاء الروحي والبدني ودعاء أيوب وطلب العافية",
            triggers = listOf("شفاء", "مرض", "عافيه", "صحه", "الم", "وجع", "ضر", "علاج", "رقيه"),
            matchStems = listOf("شفاء", "يشفين", "مسني الضر", "كشفنا", "رحمه للمؤمنين"),
            relatedRoots = listOf("شفي", "كشف", "رحم")
        ),
        SemanticConceptDef(
            id = "dua",
            titleAr = "الدعاء والاستجابة ومناجاة الله",
            descriptionAr = "قرب الله من الداعين وآداب الدعاء والأدعية الجامعة في القرآن",
            triggers = listOf("دعاء", "مناجاه", "استجابه", "طلب", "تضرع", "ربنا", "نداء"),
            matchStems = listOf("فاني قريب", "اجيب دعوه", "ادعوني استجب", "تضرعا", "امن يجيب", "ربنا", "رب اشرح", "لا اله الا انت"),
            relatedRoots = listOf("دعو", "جوب", "سأل")
        ),
        SemanticConceptDef(
            id = "tawakkul",
            titleAr = "التوكل واليقين وحسن الظن بالله",
            descriptionAr = "تفويض الأمر إلى الله وكفايته للمتوكلين والرضا بتدبيره",
            triggers = listOf("توكل", "يقين", "ثقه", "كفايه", "تدبير", "فوض", "حسبي"),
            matchStems = listOf("يتوكل", "المتوكلين", "توكلت", "فان حسبك", "وكيلا", "ونعم الوكيل", "موقنين"),
            relatedRoots = listOf("وكل", "يقن")
        ),
        SemanticConceptDef(
            id = "knowledge",
            titleAr = "العلم والتفكر والتدبر",
            descriptionAr = "رفعة أهل العلم ودعوة القرآن إلى التأمل في الكون والآيات",
            triggers = listOf("علم", "تفكر", "تدبر", "عقل", "حكمه", "معرفه", "دراسه", "تعلم", "علماء", "بصيره"),
            matchStems = listOf("يرفع الله الذين امنوا منكم والذين اوتوا العلم", "يتفكرون", "يتدبرون", "افلا تعقلون", "لاولي الالباب", "زدني علما", "الحكمه"),
            relatedRoots = listOf("علم", "عقل", "فكر", "دبر", "حكم")
        ),
        SemanticConceptDef(
            id = "justice",
            titleAr = "العدل والأمانة والقسط ومقاومة الظلم",
            descriptionAr = "وجوب الحكم بالعدل وأداء الأمانات وحرمة البغي والظلم",
            triggers = listOf("عدل", "قسط", "امانه", "حق", "ظلم", "حقوق", "شهاده", "ميزان", "اصلاح"),
            matchStems = listOf("بالعدل", "تؤدوا الامانات", "بالقسط", "شهداء لله", "لا يظلم", "واعدلوا", "المقسطين"),
            relatedRoots = listOf("عدل", "حق", "ظلم", "شهد", "صلح")
        ),
        SemanticConceptDef(
            id = "ethics",
            titleAr = "مكارم الأخلاق وحفظ اللسان والإحسان",
            descriptionAr = "حسن القول والتواضع والعفو وكظم الغيظ واجتناب الغيبة والسخرية",
            triggers = listOf("اخلاق", "ادب", "لسان", "احسان", "عفو", "تواضع", "غيبه", "سخريه", "كظم", "صدق", "معامله"),
            matchStems = listOf("هونا", "قولوا للناس حسنا", "الكاظمين الغيظ", "العافين عن الناس", "بالتي هي احسن", "لا يسخر", "ولا تجسسوا", "ولا يغتب", "الصادقين"),
            relatedRoots = listOf("حسن", "عفو", "صدق", "سلم")
        ),
        SemanticConceptDef(
            id = "gratitude",
            titleAr = "الشكر والحمد واستدامة النعم",
            descriptionAr = "شكر النعم وزيادتها وحمد الله في السراء والضراء",
            triggers = listOf("شكر", "حمد", "نعمه", "نعم", "فضل", "ثناء", "عرفان"),
            matchStems = listOf("لئن شكرتم لازيدنكم", "اشكروا", "الشاكرين", "الحمد لله", "نعمه الله", "الاء"),
            relatedRoots = listOf("شكر", "حمد")
        ),
        SemanticConceptDef(
            id = "guidance",
            titleAr = "الهداية والنور والصراط المستقيم",
            descriptionAr = "سؤال الله الثبات على الطريق المستقيم والخروج من الظلمات إلى النور",
            triggers = listOf("هدايه", "صراط", "طريق", "نور", "استقامه", "رشاد", "حق"),
            matchStems = listOf("الصراط المستقيم", "اهدنا", "يهدي به الله", "الي النور", "من الظلمات", "استقاموا", "رشد"),
            relatedRoots = listOf("هدي", "نور")
        ),
        SemanticConceptDef(
            id = "salah_worship",
            titleAr = "الصلاة والخشوع وقيام الليل",
            descriptionAr = "إقامة الصلاة والخشوع فيها وفضل التسبيح والتهجد بالأسحار",
            triggers = listOf("صلاه", "خشوع", "قيام", "تهجد", "سحور", "سجود", "تسبيح", "عباده"),
            matchStems = listOf("اقيموا الصلاه", "في صلاتهم خاشعون", "تنهي عن الفحشاء", "وبالاسحار", "تتجافي جنوبهم", "واسجد واقترب"),
            relatedRoots = listOf("صلو", "خشع", "سجد", "سبح")
        ),
        SemanticConceptDef(
            id = "charity",
            titleAr = "الصدقة والإنفاق والزكاة والإيثار",
            descriptionAr = "مضاعفة أجر المنفقين في سبيل الله وحق السائل والمحروم",
            triggers = listOf("صدقه", "انفاق", "زكاه", "تبرع", "احسان", "مساكين", "فقراء", "ايثار", "قرض"),
            matchStems = listOf("ينفقون", "انفقوا", "سبع سنابل", "قرضا حسنا", "الصدقات", "ويؤثرون علي انفسهم", "للسائل والمحروم"),
            relatedRoots = listOf("نفق", "صدق", "زكو")
        ),
        SemanticConceptDef(
            id = "paradise",
            titleAr = "الجنة والنعيم والرضوان",
            descriptionAr = "أوصاف الجنة وأنهارها ورضوان الله الأكبر وأهل النعيم المقيم",
            triggers = listOf("جنه", "نعيم", "فردوس", "رضوان", "خلود", "فوز", "اخره", "اجر"),
            matchStems = listOf("تجري من تحتها الانهار", "الفوز العظيم", "جنات عدن", "الفردوس", "لا خوف عليهم", "ورضوان من الله"),
            relatedRoots = listOf("جنن", "فوز", "رضي")
        ),
        SemanticConceptDef(
            id = "hereafter",
            titleAr = "اليوم الآخر والحساب والميزان",
            descriptionAr = "مشاهد البعث والحساب والميزان والاستعداد للقاء الله",
            triggers = listOf("قيامه", "حساب", "ميزان", "بعث", "نشور", "جزاء", "ساعه", "موت", "برزخ"),
            matchStems = listOf("يوم القيامه", "الموازين", "مثقال ذره", "يوم الحساب", "ذائقه الموت", "لقاء ربهم", "تنفخ"),
            relatedRoots = listOf("بعث", "موت", "حيي")
        ),
        SemanticConceptDef(
            id = "quran_virtue",
            titleAr = "القرآن الكريم وتلاوته وتدبره",
            descriptionAr = "عظمة القرآن وحفظه وبركة تلاوته والعمل بهديه",
            triggers = listOf("قران", "تلاوه", "ترتيل", "وحي", "ايات", "تنزيل", "حفظ"),
            matchStems = listOf("انا نحن نزلنا الذكر", "ورتل القران", "افلا يتدبرون", "لذكرى", "كتاب انزلناه مبارك", "يهدي للتي هي اقوم"),
            relatedRoots = listOf("قرأ", "نزل", "كتب", "دبر")
        ),
        SemanticConceptDef(
            id = "cosmos",
            titleAr = "الكون والسماوات والأرض وآيات الخلق",
            descriptionAr = "التأمل في السماوات والأرض والشمس والقمر والبحار والماء والنبات",
            triggers = listOf("كون", "سماء", "ارض", "شمس", "قمر", "نجوم", "بحر", "ماء", "مطر", "طبيعه", "خلق"),
            matchStems = listOf("السماوات والارض", "الشمس والقمر", "انزل من السماء ماء", "مرج البحرين", "اختلاف الليل والنهار"),
            relatedRoots = listOf("خلق", "سمو", "ارض", "بحر", "موه", "شمس", "قمر")
        ),
        SemanticConceptDef(
            id = "victory",
            titleAr = "النصر والتمكين والعزة",
            descriptionAr = "سنة الله في نصر المؤمنين وفتحهم وعزتهم بالحق",
            triggers = listOf("نصر", "تمكين", "فتح", "عزه", "غلبه", "تاييد", "ثبات"),
            matchStems = listOf("نصر من الله", "فتحا مبينا", "ولينصرن الله", "لله العزه", "ان تنصروا الله ينصركم"),
            relatedRoots = listOf("نصر", "فتح", "عزز")
        )
    )

    fun findMatchingConcepts(query: String): List<Pair<SemanticConceptDef, Int>> {
        val normQuery = QuranRootLexicon.normalizeToken(query)
        if (normQuery.length < 2) return emptyList()
        val queryWords = normQuery.split(Regex("\\s+")).filter { it.length >= 2 && it !in QuranRootLexicon.stopwords }

        return concepts.mapNotNull { concept ->
            val normTitle = QuranRootLexicon.normalizeToken(concept.titleAr)
            val normDesc = QuranRootLexicon.normalizeToken(concept.descriptionAr)
            var score = 0
            val matchedLabels = mutableSetOf<String>()

            if (normTitle.contains(normQuery)) {
                score += 12
                matchedLabels.add(concept.titleAr)
            }
            for (trigger in concept.triggers) {
                val nt = QuranRootLexicon.normalizeToken(trigger)
                if (normQuery == nt || normQuery.contains(nt) || nt.contains(normQuery)) {
                    score += if (normQuery == nt) 10 else 6
                    matchedLabels.add(trigger)
                }
            }
            for (qw in queryWords) {
                if (normTitle.contains(qw)) score += 5
                if (normDesc.contains(qw)) score += 2
                for (trigger in concept.triggers) {
                    val nt = QuranRootLexicon.normalizeToken(trigger)
                    if (nt == qw || nt.startsWith(qw) || qw.startsWith(nt)) {
                        score += 5
                    }
                }
                for (r in concept.relatedRoots) {
                    if (QuranRootLexicon.resolveRootQuery(qw) == QuranRootLexicon.normalizeToken(r)) {
                        score += 5
                    }
                }
            }
            if (score > 0) concept to score else null
        }.sortedByDescending { it.second }
    }
}
