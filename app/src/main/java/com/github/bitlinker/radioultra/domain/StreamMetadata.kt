package com.github.bitlinker.radioultra.domain

data class StreamMetadata(val title: String?) {
    companion object {
        val EMPTY = StreamMetadata(null)
    }
}