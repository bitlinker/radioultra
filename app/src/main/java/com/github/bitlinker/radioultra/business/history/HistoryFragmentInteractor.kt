package com.github.bitlinker.radioultra.business.history

import com.github.bitlinker.radioultra.data.radiostreams.RadioMetadataRepository
import com.github.bitlinker.radioultra.domain.HistoryItem
import io.reactivex.Observable

class HistoryFragmentInteractor(private val radioMetadataRepository: RadioMetadataRepository) {
    fun getHistory(): Observable<HistoryItem> {
        return radioMetadataRepository.getHistory()
    }
}