package com.example.ui.util

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

data class DisplayRefreshConfig(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float
)

/**
 * Selects the highest refresh-rate display mode matching the active physical resolution.
 * Falls back to the globally highest refresh-rate mode if no resolution match exists.
 */
fun selectHighestRefreshMode(
    activeMode: DisplayRefreshConfig?,
    supportedModes: List<DisplayRefreshConfig>
): DisplayRefreshConfig? {
    if (supportedModes.isEmpty()) return activeMode
    val sameResolution = if (activeMode != null) {
        supportedModes.filter {
            it.width == activeMode.width && it.height == activeMode.height
        }
    } else {
        emptyList()
    }
    val pool = sameResolution.ifEmpty { supportedModes }
    return pool.maxWithOrNull(
        compareBy<DisplayRefreshConfig> { it.refreshRate }
            .thenBy { it.modeId == activeMode?.modeId }
    )
}

object HighRefreshRateHelper {

    fun applyHighestRefreshRate(activity: Activity): Float? {
        val display = resolveDisplay(activity) ?: return null
        val active = display.mode?.toConfig()
        val supported = display.supportedModes.map { it.toConfig() }
        val bestMode = selectHighestRefreshMode(active, supported) ?: return null

        val window = activity.window ?: return bestMode.refreshRate
        val attrs = window.attributes
        var updated = false
        if (attrs.preferredDisplayModeId != bestMode.modeId) {
            attrs.preferredDisplayModeId = bestMode.modeId
            updated = true
        }
        if (attrs.preferredRefreshRate != bestMode.refreshRate) {
            attrs.preferredRefreshRate = bestMode.refreshRate
            updated = true
        }
        if (updated) {
            window.attributes = attrs
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.isFrameRatePowerSavingsBalanced = false
            window.setFrameRateBoostOnTouchEnabled(true)
            window.decorView.requestedFrameRate = if (bestMode.refreshRate > 60f) {
                bestMode.refreshRate
            } else {
                View.REQUESTED_FRAME_RATE_CATEGORY_HIGH
            }
        }
        return bestMode.refreshRate
    }

    fun observeAndLock(activity: Activity, lifecycleOwner: LifecycleOwner) {
        val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                val activeId = resolveDisplay(activity)?.displayId
                if (activeId == null || activeId == displayId) {
                    applyHighestRefreshRate(activity)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                applyHighestRefreshRate(activity)
            }

            override fun onCreate(owner: LifecycleOwner) {
                displayManager?.registerDisplayListener(
                    listener,
                    Handler(Looper.getMainLooper())
                )
                applyHighestRefreshRate(activity)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                displayManager?.unregisterDisplayListener(listener)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun resolveDisplay(activity: Activity): Display? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { activity.display }.getOrNull()?.let { return it }
        }
        val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.let { return it }
        @Suppress("DEPRECATION")
        return runCatching { activity.windowManager?.defaultDisplay }.getOrNull()
    }

    private fun Display.Mode.toConfig(): DisplayRefreshConfig = DisplayRefreshConfig(
        modeId = modeId,
        width = physicalWidth,
        height = physicalHeight,
        refreshRate = refreshRate
    )
}
