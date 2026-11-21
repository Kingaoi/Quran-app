package com.example.domain.quran

import com.example.domain.model.AyahItem

object QuranSciencesResolver {

    fun resolveForAyah(ayah: AyahItem): AyahSciencesInfo {
        val key = "${ayah.surahNumber}:${ayah.ayahNumber}"
        val specificAsbab = QuranAsbabCatalog.ayahEntries[key].orEmpty()
        val surahContext = QuranAsbabEntriesPart2.surahContexts[ayah.surahNumber]
        val gharibWords = QuranGharibCatalog.extractForAyah(ayah)
        return AyahSciencesInfo(
            asbab = specificAsbab,
            surahContext = surahContext,
            gharibWords = gharibWords
        )
    }
}
