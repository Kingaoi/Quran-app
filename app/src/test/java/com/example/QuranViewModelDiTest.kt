package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.di.QuranSystemBridge
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class QuranViewModelDiTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun container_sharesSingletonsAndFactoryCreatesViewModel() {
        val app = ApplicationProvider.getApplicationContext<QuranApplication>()
        val container = app.container

        assertSame(container.quranRepository, app.quranRepository)
        assertSame(container.mushafRasterCache, app.mushafRasterCache)
        assertSame(container.audioPlayer, app.audioPlayer)

        val vm = QuranViewModel.factory(container).create(QuranViewModel::class.java)
        assertSame(container.mushafOfflineStorage, vm.offlineStorage)
    }

    @Test
    fun constructorInjection_usesProvidedSystemBridgeAndDelegates() = runTest(testDispatcher) {
        val app = ApplicationProvider.getApplicationContext<QuranApplication>()
        var shortcutSyncCalls = 0
        var lastSyncedPage = -1
        var widgetNotifyCount = 0

        val fakeBridge = object : QuranSystemBridge {
            override suspend fun warmMushafCropMap() {}
            override fun syncDynamicShortcuts(page: Int, surahName: String?, useWesternDigits: Boolean) {
                shortcutSyncCalls++
                lastSyncedPage = page
            }
            override fun prefetchMushafPages(models: List<Any>) {}
            override fun notifyWidgetsChanged() {
                widgetNotifyCount++
            }
        }

        val deps = app.container.quranViewModelDependencies(ownsAudioPlayer = false).copy(
            systemBridge = fakeBridge
        )
        val vm = QuranViewModel(deps = deps, ioDispatcher = testDispatcher, autoStart = false)

        vm.onPageScrolling(42)
        assertEquals(42, vm.currentPage.value)

        vm.onPageSettled(128)
        advanceUntilIdle()
        assertEquals(128, vm.currentPage.value)
        assertEquals(128, lastSyncedPage)
        assertTrue(shortcutSyncCalls >= 1)

        vm.setUseWesternDigits(true)
        advanceUntilIdle()
        assertEquals(1, widgetNotifyCount)
    }
}
