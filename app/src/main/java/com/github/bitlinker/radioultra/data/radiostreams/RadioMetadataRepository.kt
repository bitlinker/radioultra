package com.github.bitlinker.radioultra.data.radiostreams

import com.github.bitlinker.libhostingradio.HostingRadioApi
import com.github.bitlinker.libhostingradio.dto.ConfigStreamDto
import com.github.bitlinker.libhostingradio.dto.MetaTrackDto
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.HistoryItem
import com.github.bitlinker.radioultra.domain.RadioStream
import com.github.bitlinker.radioultra.domain.TrackMetadata
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.regex.Pattern

// TODO: setting
private const val CONFIG_CACHE_TIMEOUT = 86400000L

class RadioMetadataRepository(private val schedulerProvider: SchedulerProvider) {
    // TODO: move to some repo?
    private val hostingRadioApi: HostingRadioApi

    // TODO: cache here instead of lib itself
    //, cache streams

    init {
        val config = HostingRadioApi.Config()
        config.scheduler = schedulerProvider.io()
        config.configUrl = HostingRadioApi.Config.CONFIG_URL_ULTRA
        config.configCacheTimeMs = CONFIG_CACHE_TIMEOUT
        hostingRadioApi = HostingRadioApi(config)
    }

    fun getHistory(): Observable<HistoryItem> {
        // TODO: sort if needed
        return hostingRadioApi.getMeta(HostingRadioApi.STREAM_KEY_HIGH)
                .flatMapObservable {
                    if (it.prev_tracks?.isEmpty() ?: true) {
                        return@flatMapObservable Observable.empty<MetaTrackDto>()
                    } else {
                        return@flatMapObservable Observable.fromIterable(it.prev_tracks)
                    }
                }
                .map {
                    HistoryItem(
                            it.timeline,
                            TrackMetadata(
                                    it.uniqueid ?: UUID.randomUUID().toString(),
                                    it.title,
                                    it.artist,
                                    null,
                                    null,
                                    it.cover,
                                    it.google_url,
                                    it.itunes_url,
                                    it.youtube_url
                            )
                    )
                }
                .toSortedList(kotlin.Comparator { o1, o2 ->
                    ((o2.date?.time ?: 0L) - (o1.date?.time ?: 0L)).toInt()
                })
                .toObservable()
                .concatMapIterable { it }

    }

    fun getCurrentTrack(): Single<TrackMetadata> {
        return hostingRadioApi.getMeta(HostingRadioApi.STREAM_KEY_HIGH)
                .map {
                    TrackMetadata(
                            it.uniqueid ?: UUID.randomUUID().toString(),
                            it.title,
                            it.artist,
                            it.album,
                            it.metadata,
                            it.cover,
                            it.google_url,
                            it.itunes_url,
                            it.youtube_url
                    )
                }
    }


    fun getSteams(): Observable<RadioStream> {
        return hostingRadioApi.getConfig(false)
                .subscribeOn(Schedulers.io()) // TODO: in lib!
                .flatMapObservable {
                    Observable.fromIterable(it.streams.entries)
                            .map {
                                RadioStream(
                                        it.key,
                                        it.value.streamUrl,
                                        extractBitrateHeuristics(it.value)
                                )
                            }
                }
    }

    private val BITRATE_PATTERN = Pattern.compile(".+ultra-([0-9]+).*")
    private fun extractBitrateHeuristics(stream: ConfigStreamDto): Int {

        val matcher = BITRATE_PATTERN.matcher(stream.streamUrl)
        if (matcher.matches()) {
            return matcher.group(1).toInt()
        }
        return -1;
    }

// TODO: not working...
//    private fun getMetaInfo(url: String): Int {
//        val mex = MediaExtractor()
//        mex.setDataSource(url)
//        val trackFormat = mex.getTrackFormat(0)
//        val bitrate = trackFormat.getInteger(MediaFormat.KEY_BIT_RATE)
//        return bitrate
//    }
}