package com.github.bitlinker.radioultra.data.radiostreams

import com.github.bitlinker.radioultra.domain.RadioStream
import io.reactivex.Observable

private val hardcodedStreams = listOf<RadioStream>(
        RadioStream("hardcoded-64", "https://nashe1.hostingradio.ru:18000/ultra-64.mp3", 64),
        RadioStream("hardcoded-128", "https://nashe1.hostingradio.ru:18000/ultra-128.mp3", 128),
        RadioStream("hardcoded-256", "https://nashe1.hostingradio.ru:18000/ultra-256", 256)
)

class PredefinedRadioStreamsRepository {
    fun getStreams(): Observable<RadioStream> {
        return Observable.fromIterable(hardcodedStreams)
    }
}