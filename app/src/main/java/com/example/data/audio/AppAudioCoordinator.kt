package com.example.data.audio

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ConcurrentHashMap

enum class AppAudioOwner {
    QURAN,
    ADHKAR,
    ADHAN_PREVIEW,
    RECITER_PREVIEW,
    ADHAN_SERVICE
}

object AppAudioCoordinator {
    private val stopCallbacks = ConcurrentHashMap<AppAudioOwner, () -> Unit>()
    private val mainHandler: Handler? by lazy {
        runCatching { Handler(Looper.getMainLooper()) }.getOrNull()
    }

    fun register(owner: AppAudioOwner, onPauseOrStop: () -> Unit) {
        stopCallbacks[owner] = onPauseOrStop
    }

    fun unregister(owner: AppAudioOwner) {
        stopCallbacks.remove(owner)
    }

    fun claimFocus(owner: AppAudioOwner) {
        stopCallbacks.forEach { (otherOwner, callback) ->
            if (otherOwner != owner) {
                val mainLooper = runCatching { Looper.getMainLooper() }.getOrNull()
                val onMain = mainLooper == null || Looper.myLooper() == mainLooper
                if (onMain) {
                    runCatching { callback() }
                } else {
                    mainHandler?.post { runCatching { callback() } } ?: runCatching { callback() }
                }
            }
        }
    }

    fun clearForTest() {
        stopCallbacks.clear()
    }
}
