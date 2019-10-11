package com.github.bitlinker.radioultra.business.ui

import com.github.bitlinker.radioultra.business.common.StreamSelectionInteractor
import com.github.bitlinker.radioultra.domain.RadioStream

class StreamSelectionViewInteractor(private val streamSelectionInteractor: StreamSelectionInteractor) {
    fun setCurStream(stream: RadioStream) =
            streamSelectionInteractor.setCurStream(stream)
}