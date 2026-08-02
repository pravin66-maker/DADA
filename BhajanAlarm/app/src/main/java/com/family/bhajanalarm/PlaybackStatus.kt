package com.family.bhajanalarm

enum class PlaybackState { PLAYING, PAUSED, STOPPED }

/**
 * Simple singleton event bus (app runs single-process, so this is safe and avoids
 * pulling in extra libraries just to pass play/pause/stop state to the UI).
 */
object PlaybackStatus {
    var state: PlaybackState = PlaybackState.STOPPED
    var songName: String = ""

    private val listeners = mutableListOf<(PlaybackState, String) -> Unit>()

    fun addListener(listener: (PlaybackState, String) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (PlaybackState, String) -> Unit) {
        listeners.remove(listener)
    }

    fun update(newState: PlaybackState, name: String) {
        state = newState
        songName = name
        listeners.forEach { it(newState, name) }
    }
}
