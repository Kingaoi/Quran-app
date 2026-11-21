package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import coil.imageLoader
import coil.size.Size
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.data.repository.QuranRepository
import com.example.ui.components.MushafCenterCropTransformation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("القرآن الكريم", appName)
  }

  @Test
  fun `quranApplication provides shared ImageLoader with disk and memory cache`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val loader = context.imageLoader
    assertNotNull(loader.memoryCache)
    assertNotNull(loader.diskCache)
  }

  @Test
  fun `mushafOfflineStorage stores counts and deletes cached pages`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = MushafOfflineStorage(context)
    storage.deleteEdition(RiwayahEdition.HAFS)
    assertEquals(0, storage.countDownloadedPages(RiwayahEdition.HAFS))
    assertNull(storage.getCachedPageFile(RiwayahEdition.HAFS, 1))

    val pageFile = storage.pageFile(RiwayahEdition.HAFS, 1)
    pageFile.parentFile?.mkdirs()
    pageFile.writeText("<svg xmlns='http://www.w3.org/2000/svg' width='520' height='840'>" + " ".repeat(200) + "</svg>")

    assertEquals(1, storage.countDownloadedPages(RiwayahEdition.HAFS))
    assertNotNull(storage.getCachedPageFile(RiwayahEdition.HAFS, 1))
    assertNotNull(storage.getCachedPageFileFast(RiwayahEdition.HAFS, 1))
    assertNull(storage.getCachedPageFileFast(RiwayahEdition.HAFS, 2))

    storage.deleteEdition(RiwayahEdition.HAFS)
    assertEquals(0, storage.countDownloadedPages(RiwayahEdition.HAFS))
    assertNull(storage.getCachedPageFileFast(RiwayahEdition.HAFS, 1))
  }

  @Test
  fun `mushafRasterCache indexes existing PNGs and invalidates cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val rasterDir = java.io.File(context.cacheDir, "mushaf_raster/hafs-kfqc_1080x1800")
    rasterDir.mkdirs()
    val pagePng = java.io.File(rasterDir, "005.png")
    pagePng.writeBytes(ByteArray(1024) { 1 })

    val cache = MushafRasterCache(context)
    assertNull(cache.getCachedFileIfReady("hafs-kfqc", 5, 1080, 1800))
    cache.warmIndex()
    assertNotNull(cache.getCachedFileIfReady("hafs-kfqc", 5, 1080, 1800))
    assertNull(cache.getCachedFileIfReady("hafs-kfqc", 6, 1080, 1800))

    cache.invalidatePage("hafs-kfqc", 5, 1080, 1800)
    assertNull(cache.getCachedFileIfReady("hafs-kfqc", 5, 1080, 1800))

    val coloredDir = java.io.File(context.cacheDir, "mushaf_raster/hafs-kfqc_em_b_d_1080x1800")
    coloredDir.mkdirs()
    java.io.File(coloredDir, "005.png").writeBytes(ByteArray(1024) { 2 })
    val coloredCache = MushafRasterCache(context)
    coloredCache.warmIndex()
    assertNotNull(coloredCache.getCachedFileIfReady("hafs-kfqc", 5, 1080, 1800, colorKey = "em_b_d"))
    assertNull(coloredCache.getCachedFileIfReady("hafs-kfqc", 5, 1080, 1800, colorKey = "em_b_n"))

    val inverted = com.example.ui.components.MushafSvgMarkerRecolorer.invertHexForNightFilter("#6CCB9B")
    assertEquals("#87203f", inverted)
  }

  @Test
  fun `quranRepository loads 114 surahs 30 ajza and 240 hizb quarters`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuranRepository(context)
    repo.ensureLoaded()

    assertEquals(114, repo.getSurahs().size)
    assertEquals(30, repo.getAjza().size)

    val quartersByJuz = repo.getHizbQuartersByJuz()
    val badAjza = (1..30).mapNotNull { juz ->
      val list = quartersByJuz[juz].orEmpty()
      if (list.size != 8) "Juz $juz=${list.size} (${list.map { "${it.surahNumber}:${it.ayahNumber}" }})" else null
    }
    assertEquals("Expected 8 quarters in every Juz, mismatches: $badAjza", emptyList<String>(), badAjza)
    assertEquals(240, quartersByJuz.values.sumOf { it.size })

    val slicesByJuz = repo.getJuzSurahSlices()
    assertEquals(30, slicesByJuz.size)
    assertTrue(slicesByJuz.values.all { it.isNotEmpty() })
  }

  @Test
  fun `mushaf crop transformation centers left-skewed and right-skewed pages symmetrically`() = runTest {
    val transformation = MushafCenterCropTransformation(
      pageKey = "test_page_symmetry",
      horizontalPadRatio = 0.03f,
      verticalPadRatio = 0.02f
    )

    // Simulate an odd page hugging the left edge (left margin 4px, right margin 66px)
    val leftSkewed = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888)
    val paint = Paint().apply { color = Color.BLACK }
    Canvas(leftSkewed).drawRect(4f, 20f, 334f, 570f, paint)

    val centeredFromLeft = transformation.transform(leftSkewed, Size.ORIGINAL)
    assertBalancedMargins(centeredFromLeft)

    // Simulate an even page hugging the right edge (left margin 66px, right margin 4px)
    val rightSkewed = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888)
    Canvas(rightSkewed).drawRect(66f, 20f, 396f, 570f, paint)

    val centeredFromRight = transformation.transform(rightSkewed, Size.ORIGINAL)
    assertBalancedMargins(centeredFromRight)
  }

  private fun assertBalancedMargins(bitmap: Bitmap) {
    val midY = bitmap.height / 2
    var minX = bitmap.width
    var maxX = -1
    for (x in 0 until bitmap.width) {
      if (Color.alpha(bitmap.getPixel(x, midY)) > 128) {
        if (x < minX) minX = x
        if (x > maxX) maxX = x
      }
    }
    val leftMargin = minX
    val rightMargin = (bitmap.width - 1) - maxX
    assertTrue("Expected positive balanced left margin, got $leftMargin", leftMargin >= 6)
    assertTrue(
      "Expected symmetrical left ($leftMargin) and right ($rightMargin) margins",
      abs(leftMargin - rightMargin) <= 1
    )
  }

  @Test
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  fun `mushafColoredSvgDecoder renders bundled Madinah surah banner via AndroidSVG`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val template = com.example.ui.components.MushafSvgSurahBannerInjector.loadBannerTemplate(context)
    assertTrue("Expected bundled surah_header_madinah.txt asset", template.contains("q_sh_m"))

    val sampleSvg = """
      <svg xmlns="http://www.w3.org/2000/svg" xmlns:ayah="https://quranpedia.net" version="1.1" viewBox="0 0 345 550">
        <g transform="matrix(1.3333 0 0 -1.3333 -115 640)">
          <g id="ayah_markers"><g class="ayah-mark"><g class="ayah-mark-ornament"><path d="M10 100 H20 V110 H10 Z" fill="#231f20"/></g><g ayah:x="15" ayah:y="105"><path d="M12 102 H18 V108 H12 Z" fill="#231f20"/></g></g></g>
          <g id="content">
            <g transform="translate(200 462)">
              <path d="M0 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z" fill="#231f20"/>
            </g>
            <g transform="translate(200 352)">
              <path d="M0 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z" fill="#231f20"/>
            </g>
          </g>
        </g>
        <path class="ayahPolygon" fill-opacity="0" surah="112" ayah="1" d="M 0 74.25 L 341.2 74.25 L 341.2 112.75 L 0 112.75 Z" />
        <path class="ayahPolygon" fill-opacity="0" surah="112" ayah="4" d="M 0 112.75 L 341.2 112.75 L 341.2 148.25 L 0 148.25 Z" />
        <path class="ayahPolygon" fill-opacity="0" surah="113" ayah="1" d="M 0 218.5 L 341.2 218.5 L 341.2 257.25 L 0 257.25 Z" />
      </svg>
    """.trimIndent()

    val buffer = okio.Buffer().writeUtf8(sampleSvg)
    val options = coil.request.Options(
      context = context,
      size = coil.size.Size(345, 550),
      tags = coil.request.Tags.from(
        mapOf(
          com.example.ui.components.MushafSvgColorTag::class.java to
            com.example.ui.components.MushafSvgColorTag(
              ornamentHex = "#1B6B46",
              numberHex = "#1B6B46",
              surahBannerHex = "#1B6B46"
            )
        )
      )
    )
    val decoder = com.example.ui.components.MushafColoredSvgDecoder(
      source = coil.decode.ImageSource(buffer, context),
      options = options,
      colorTag = com.example.ui.components.MushafSvgColorTag("#1B6B46", "#1B6B46", "#1B6B46")
    )
    val result = decoder.decode()
    val drawable = result.drawable
    assertNotNull(drawable)
    assertTrue("Expected positive drawable width", drawable.intrinsicWidth > 0)
    assertTrue("Expected positive drawable height", drawable.intrinsicHeight > 0)

    val placements = com.example.ui.components.MushafSvgSurahBannerInjector.computeBannerPlacements(sampleSvg)
    assertEquals("Expected top and mid surah placements, got $placements", 2, placements.size)

    val bmp = Bitmap.createBitmap(345, 550, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, 345, 550)
    drawable.draw(canvas)
    var topLeftWing = 0
    var topRightWing = 0
    var midGreenPixels = 0
    var basmalahOverlapPixels = 0
    for (y in 0 until 550) {
      for (x in 0 until 345) {
        val px = bmp.getPixel(x, y)
        if (Color.alpha(px) > 10 && Color.green(px) > Color.red(px) + 15) {
          if (y in 0..38) {
            if (x in 5..80) topLeftWing++
            if (x in 260..336) topRightWing++
          }
          if (y in 145..185 && x in 5..336) midGreenPixels++
          if (y in 188..216 && x in 5..336) basmalahOverlapPixels++
        }
      }
    }
    assertTrue("Expected top-left banner wing pixels, got $topLeftWing", topLeftWing > 50)
    assertTrue("Expected top-right banner wing pixels, got $topRightWing", topRightWing > 50)
    assertTrue("Expected mid-page banner pixels on Surah name line, got $midGreenPixels", midGreenPixels > 100)
    assertEquals("Banner must not overlap Basmalah Y-band", 0, basmalahOverlapPixels)
  }

  @Test
  fun `wirdPlanCalculator builds exact 30-day Khatmah and Juz 9 boundaries from QuranRepository`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuranRepository(context)
    repo.ensureLoaded()

    val plan = com.example.domain.wird.WirdPlanCalculator.buildPlan(
      config = com.example.domain.wird.KhatmahPlanConfig(
        isActive = true,
        startJuz = 1,
        quartersPerDay = 8,
        completedWirdsCount = 8, // Juz 9 (index 8)
        startEpochDay = 100L
      ),
      quarters = repo.getAllHizbQuarters(),
      allAyahs = repo.getAllAyahs(),
      ajza = repo.getAjza(),
      todayEpochDay = 110L
    )
    assertNotNull(plan)
    assertEquals(30, plan!!.totalWirds)
    val juz9Wird = plan.currentWird!!
    assertEquals(7, juz9Wird.startSurahNumber)
    assertEquals(88, juz9Wird.startAyahNumber)
    assertEquals(162, juz9Wird.startPage)
    assertEquals(8, juz9Wird.endSurahNumber)
    assertEquals(40, juz9Wird.endAyahNumber)
    assertEquals(181, juz9Wird.endPage)
    val lastWird = plan.allWirds.last()
    assertEquals(114, lastWird.endSurahNumber)
    assertEquals(6, lastWird.endAyahNumber)
    assertEquals(604, lastWird.endPage)
  }
}
