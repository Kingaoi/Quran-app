package com.example.data.audio

internal object AudioPlaylistPlayerHelper {

    fun buildSession(
        playlist: AudioPlaylist,
        startTrackIndex: Int,
        surahMetaLookup: (Int) -> Pair<String, Int>
    ): PlaylistPlaybackSession? {
        if (playlist.tracks.isEmpty()) return null
        val sanitized = playlist.tracks.map { track ->
            val (_, ayahCount) = surahMetaLookup(track.surahNumber)
            val maxAyah = ayahCount.coerceAtLeast(1)
            val s = track.startAyah.coerceIn(1, maxAyah)
            val e = track.endAyah.coerceIn(s, maxAyah)
            track.copy(startAyah = s, endAyah = e)
        }
        val clampedStart = startTrackIndex.coerceIn(0, sanitized.lastIndex)
        val orderedTracks = if (playlist.shuffleEnabled && sanitized.size > 1) {
            val anchor = sanitized[clampedStart]
            val rest = sanitized.filterIndexed { index, _ -> index != clampedStart }.shuffled()
            listOf(anchor) + rest
        } else {
            sanitized
        }
        return PlaylistPlaybackSession(
            playlistId = playlist.id,
            playlistTitleAr = playlist.titleAr,
            tracks = orderedTracks,
            currentTrackIndex = if (playlist.shuffleEnabled) 0 else clampedStart,
            repeatPlaylist = playlist.repeatPlaylist,
            shuffleEnabled = playlist.shuffleEnabled
        )
    }

    sealed interface StepOutcome {
        data class PlayAyah(val surah: Int, val ayah: Int, val updatedSession: PlaylistPlaybackSession) : StepOutcome
        data object StopPlayback : StepOutcome
    }

    fun onAyahCompleted(
        state: AudioPlaybackState,
        session: PlaylistPlaybackSession
    ): StepOutcome {
        val track = session.currentTrack ?: return StepOutcome.StopPlayback
        if (state.reciter.isSurahLevelAudio) {
            return advanceToNextTrack(session)
        }
        if (state.currentSurah == track.surahNumber && state.currentAyah < track.endAyah) {
            return StepOutcome.PlayAyah(track.surahNumber, state.currentAyah + 1, session)
        }
        return advanceToNextTrack(session)
    }

    fun onManualNextAyah(
        state: AudioPlaybackState,
        session: PlaylistPlaybackSession
    ): StepOutcome? {
        val track = session.currentTrack ?: return null
        if (!state.reciter.isSurahLevelAudio &&
            state.currentSurah == track.surahNumber &&
            state.currentAyah < track.endAyah
        ) {
            return StepOutcome.PlayAyah(track.surahNumber, state.currentAyah + 1, session)
        }
        val next = advanceToNextTrack(session)
        return if (next is StepOutcome.PlayAyah) next else null
    }

    fun onManualPreviousAyah(
        state: AudioPlaybackState,
        session: PlaylistPlaybackSession
    ): StepOutcome? {
        val track = session.currentTrack ?: return null
        if (!state.reciter.isSurahLevelAudio &&
            state.currentSurah == track.surahNumber &&
            state.currentAyah > track.startAyah
        ) {
            return StepOutcome.PlayAyah(track.surahNumber, state.currentAyah - 1, session)
        }
        return jumpToPreviousTrack(session)
    }

    fun advanceToNextTrack(session: PlaylistPlaybackSession): StepOutcome {
        if (session.tracks.isEmpty()) return StepOutcome.StopPlayback
        val nextIndex = session.currentTrackIndex + 1
        val targetIndex = when {
            nextIndex < session.tracks.size -> nextIndex
            session.repeatPlaylist -> 0
            else -> return StepOutcome.StopPlayback
        }
        val nextTrack = session.tracks[targetIndex]
        return StepOutcome.PlayAyah(
            surah = nextTrack.surahNumber,
            ayah = nextTrack.startAyah,
            updatedSession = session.copy(currentTrackIndex = targetIndex)
        )
    }

    fun jumpToPreviousTrack(session: PlaylistPlaybackSession): StepOutcome.PlayAyah? {
        if (session.tracks.isEmpty()) return null
        val prevIndex = when {
            session.currentTrackIndex > 0 -> session.currentTrackIndex - 1
            session.repeatPlaylist -> session.tracks.lastIndex
            else -> 0
        }
        val prevTrack = session.tracks[prevIndex]
        return StepOutcome.PlayAyah(
            surah = prevTrack.surahNumber,
            ayah = prevTrack.startAyah,
            updatedSession = session.copy(currentTrackIndex = prevIndex)
        )
    }
}
