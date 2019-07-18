package com.github.bitlinker.radioultra.presentation.streamselection

import com.github.bitlinker.radioultra.domain.RadioStream
import java.io.Serializable

class StreamSelectionArgs(
        val streams: List<RadioStream>,
        val selectedStream: RadioStream
) : Serializable