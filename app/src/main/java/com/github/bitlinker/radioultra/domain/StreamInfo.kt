package com.github.bitlinker.radioultra.domain

data class StreamInfo(val bitrate: Int?, val channels: Int?, val sampleRate: Int?) {
    companion object {
        val EMPTY = StreamInfo(null, null, null)
    }
}