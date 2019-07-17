package com.github.bitlinker.radioultra.domain

data class PlayerStatus(val state: State, val throwable: Throwable? = null) {
    enum class State() {
        STOPPED,
        STOPPED_ERROR,
        BUFFERING,
        PLAYING;

        fun isPlaying(): Boolean {
            return this == PLAYING || this == BUFFERING
        }
    }
}

