package com.github.bitlinker.radioultra.domain

import java.io.Serializable

data class TrackMetadata(
        val id: String,
        val title: String?,
        val artist: String?,
        val album: String?,
        val streamTitle: String?,
        val coverLink: String?,
        val googleLink: String?,
        val itunesLink: String?,
        val youtubeLink: String?
) : Serializable
