package com.github.bitlinker.radioultra.domain

import java.io.Serializable

data class RadioStream(val id: String, val url: String, val bitrate: Int): Serializable