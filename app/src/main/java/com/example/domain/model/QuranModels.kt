package com.example.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SurahInfo(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val ayahCount: Int,
    val revelationType: String,
    val isMakki: Boolean,
    val hasBasmalah: Boolean,
    val firstPage: Int,
    val lastPage: Int,
    val wordCount: Int = 0,
    val letterCount: Int = 0,
    val openingAyahText: String = ""
)

@Immutable
data class AyahItem(
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val text: String,
    val imlaiText: String,
    val marker: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val hasSajdah: Boolean
)

@Immutable
data class JuzInfo(
    val number: Int,
    val nameAr: String,
    val startSurahNumber: Int,
    val startSurahNameAr: String,
    val startAyahNumber: Int,
    val startPage: Int
)

@Immutable
data class JuzSurahSlice(
    val juzNumber: Int,
    val surah: SurahInfo,
    val startAyah: Int,
    val startPage: Int,
    val isContinuationFromPreviousJuz: Boolean
)

@Immutable
data class HizbQuarterInfo(
    val rubNumber: Int,
    val juzNumber: Int,
    val hizbNumber: Int,
    val quarterInHizb: Int, // 0 = Hizb start, 1 = 1/4, 2 = 1/2, 3 = 3/4
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val openingText: String
) {
    val badgeLabelAr: String
        get() = when (quarterInHizb) {
            0 -> hizbNumber.toArabicIndicDigits()
            1 -> "١/٤".localizeDigits()
            2 -> "١/٢".localizeDigits()
            else -> "٣/٤".localizeDigits()
        }
}

@Immutable
data class SearchHit(
    val surahNumber: Int,
    val surahNameAr: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val ayahText: String,
    val matchedWord: String
)

@Immutable
data class ReciterOption(
    val id: String,
    val nameAr: String,
    val styleAr: String,
    val everyAyahFolder: String,
    val photoUrl: String = "",
    val fallbackPhotoUrl: String = "",
    val riwayah: com.example.data.local.RiwayahEdition = com.example.data.local.RiwayahEdition.HAFS,
    val categoryAr: String = "المدرسة العامة",
    val bioAr: String = "",
    val isSurahLevelAudio: Boolean = false,
    val directBaseUrl: String = "",
    val preferMonogramAvatar: Boolean = false
) {
    fun ayahAudioUrl(surah: Int, ayah: Int): String {
        val sss = surah.toString().padStart(3, '0')
        if (isSurahLevelAudio && directBaseUrl.isNotBlank()) {
            return "${directBaseUrl.trimEnd('/')}/$sss.mp3"
        }
        val aaa = ayah.toString().padStart(3, '0')
        if (directBaseUrl.isNotBlank()) {
            return "${directBaseUrl.trimEnd('/')}/$sss$aaa.mp3"
        }
        return "https://everyayah.com/data/$everyAyahFolder/$sss$aaa.mp3"
    }
}

object QuranReciters {
    val all: List<ReciterOption> = listOf(
        ReciterOption(
            id = "Abdul_Basit_Murattal_192kbps",
            nameAr = "عبد الباسط عبد الصمد",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Abdul_Basit_Murattal_192kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Abdul_Basit_Abd_us-Samad.jpg?width=480",
            categoryAr = "المدرسة المصرية",
            bioAr = "أحد أعلام التلاوة في العالم الإسلامي، يجمع بين الخشوع وحسن الأداء وإتقان المقامات."
        ),
        ReciterOption(
            id = "MaherAlMuaiqly128kbps",
            nameAr = "ماهر المعيقلي",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "MaherAlMuaiqly128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Maher_Al_Mueaqly.png?width=480",
            fallbackPhotoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Professor_Maher_Al-Mu%27aiqly.jpg?width=480",
            categoryAr = "أئمة الحرم المكي",
            bioAr = "إمام وخطيب المسجد الحرام بمكة، يتميز بنبرة صافية متزنة وتلاوة ميسرة للتدبر."
        ),
        ReciterOption(
            id = "Alafasy_128kbps",
            nameAr = "مشاري راشد العفاسي",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Alafasy_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/%D0%9C%D0%B8%D1%88%D0%B0%D1%80%D0%B8_%D0%A0%D0%B0%D1%88%D0%B8%D0%B4.jpg?width=480",
            categoryAr = "أئمة الخليج",
            bioAr = "قارئ وإمام المسجد الكبير بالكويت، معروف بعذوبة الصوت والتنقل اللطيف بين المقامات."
        ),
        ReciterOption(
            id = "Husary_128kbps",
            nameAr = "محمود خليل الحصري",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Husary_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Hussary.jpg?width=480",
            categoryAr = "المدرسة المصرية",
            bioAr = "شيخ عموم المقارئ المصرية، مرجع رئيسي في ضبط أحكام التجويد ومخارج الحروف."
        ),
        ReciterOption(
            id = "Minshawy_Murattal_128kbps",
            nameAr = "محمد صديق المنشاوي",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Minshawy_Murattal_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Minshawy.jpg?width=480",
            fallbackPhotoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Elminshwey.jpg?width=480",
            categoryAr = "المدرسة المصرية",
            bioAr = "لقب بالصوت الخاشع الباكي، تلاوته محبوبة لعمقها الروحي وخشوعها المؤثر."
        ),
        ReciterOption(
            id = "Abdurrahmaan_As-Sudais_192kbps",
            nameAr = "عبد الرحمن السديس",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Abdurrahmaan_As-Sudais_192kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Abdul-Rahman_Al-Sudais_(Cropped%2C_2011).jpg?width=480",
            fallbackPhotoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Shaikh_Abdur_Rahman_Abdul_Aziz_al_Sudais_in_New_Delhi_on_March_24%2C_2011.jpg?width=480",
            categoryAr = "أئمة الحرم المكي",
            bioAr = "إمام وخطيب المسجد الحرام، يتميز بصوت جهوري مؤثر يبعث على السكينة والتأمل."
        ),
        ReciterOption(
            id = "Hudhaify_128kbps",
            nameAr = "علي بن عبد الرحمن الحذيفي",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Hudhaify_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Huthaify.jpg?width=480",
            categoryAr = "أئمة المسجد النبوي",
            bioAr = "شيخ أئمة المسجد النبوي الشريف، يتميز بترتيل وقور رصين وضبط متقن للقراءة."
        ),
        ReciterOption(
            id = "Ghamadi_40kbps",
            nameAr = "سعد الغامدي",
            styleAr = "حفص عن عاصم • مرتل",
            everyAyahFolder = "Ghamadi_40kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Saad_al_Ghamdi.jpg?width=480",
            categoryAr = "أئمة المسجد النبوي",
            bioAr = "قارئ وإمام سابق بالمسجد النبوي، تلاوته هادئة ومناسبة للحفظ والمراجعة اليومية."
        ),
        ReciterOption(
            id = "warsh_Abdul_Basit_128kbps",
            nameAr = "عبد الباسط عبد الصمد (ورش)",
            styleAr = "ورش عن نافع • مرتل",
            everyAyahFolder = "warsh/warsh_Abdul_Basit_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Abdul_Basit_Abd_us-Samad.jpg?width=480",
            riwayah = com.example.data.local.RiwayahEdition.WARSH,
            categoryAr = "قرّاء رواية ورش",
            bioAr = "ختمة مرتلة متقنة للشيخ عبد الباسط برواية ورش عن نافع من طريق الأزرق."
        ),
        ReciterOption(
            id = "warsh_ibrahim_aldosary_128kbps",
            nameAr = "إبراهيم الدوسري (ورش)",
            styleAr = "ورش عن نافع • آية بآية",
            everyAyahFolder = "warsh/warsh_ibrahim_aldosary_128kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Huthaify.jpg?width=480",
            riwayah = com.example.data.local.RiwayahEdition.WARSH,
            categoryAr = "قرّاء رواية ورش",
            bioAr = "تلاوة محققة متزنة برواية ورش عن نافع، واضحة المخارج ومناسبة للمراجعة.",
            preferMonogramAvatar = true
        ),
        ReciterOption(
            id = "warsh_yassin_al_jazaery_64kbps",
            nameAr = "ياسين الجزائري (ورش)",
            styleAr = "ورش عن نافع • آية بآية",
            everyAyahFolder = "warsh/warsh_yassin_al_jazaery_64kbps",
            photoUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Hussary.jpg?width=480",
            riwayah = com.example.data.local.RiwayahEdition.WARSH,
            categoryAr = "المدرسة المغاربية",
            bioAr = "قارئ جزائري متقن برواية ورش عن نافع، يتميز بترتيل مريح وأداء مغاربي أصيل.",
            preferMonogramAvatar = true
        )
    )

    @Volatile
    private var dynamicCatalog: List<ReciterOption> = emptyList()

    fun registerDynamicReciters(extra: List<ReciterOption>) {
        dynamicCatalog = extra.distinctBy { it.id }
    }

    fun combinedCatalog(): List<ReciterOption> =
        if (dynamicCatalog.isEmpty()) all else (all + dynamicCatalog).distinctBy { it.id }

    fun forRiwayah(edition: com.example.data.local.RiwayahEdition): List<ReciterOption> =
        combinedCatalog().filter { it.riwayah == edition }

    fun byId(id: String): ReciterOption =
        combinedCatalog().firstOrNull { it.id == id } ?: all.first()
}

fun Int.toArabicIndicDigits(): String = AppDigitConfig.formatInt(this)
