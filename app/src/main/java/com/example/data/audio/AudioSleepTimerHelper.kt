package com.example.data.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class AudioSleepTimerHelper(
    private val scope: CoroutineScope,
    private val onTickRemainingSec: (Int?) -> Unit,
    private val onTimerExpired: () -> Unit
) {
    private var timerJob: Job? = null

    fun startMinutes(minutes: Int) {
        cancel()
        if (minutes <= 0) return
        var remaining = minutes * 60
        onTickRemainingSec(remaining)
        timerJob = scope.launch {
            while (isActive && remaining > 0) {
                delay(1_000L)
                remaining -= 1
                if (remaining <= 0) {
                    onTickRemainingSec(null)
                    onTimerExpired()
                    break
                } else {
                    onTickRemainingSec(remaining)
                }
            }
        }
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        onTickRemainingSec(null)
    }
}
