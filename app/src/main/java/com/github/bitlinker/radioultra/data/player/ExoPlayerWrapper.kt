package com.github.bitlinker.radioultra.data.player

import android.content.Context
import android.net.Uri
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.Closeable

class ExoPlayerWrapper(context: Context,
                       schedulerProvider: SchedulerProvider) : Closeable {
    private val player: SimpleExoPlayer
    private val dataSourceFactory: DataSource.Factory
    private val extractorsFactory: ExtractorsFactory
    private val mediaSourceFactory: ProgressiveMediaSource.Factory
    private val renderersFactory: RenderersFactory

    private val scheduler: Scheduler = schedulerProvider.ui()

    // TODO: set useragent, loaccontrol recreates player!

    // TODO: defaults
    private val streamMetadata = BehaviorSubject.create<StreamMetadata>().toSerialized()
    private val streamInfo = BehaviorSubject.create<StreamInfo>().toSerialized()
    private val playerStatus = BehaviorSubject.createDefault(PlayerStatus(PlayerStatus.State.STOPPED)).toSerialized()

    init {
        val userAgent = "foobar2000/1.3.16"
        // TODO: useragent can be updated with recreating factory only...
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory(userAgent, null)
        dataSourceFactory = DefaultDataSourceFactory(context, null, defaultHttpDataSourceFactory)
        extractorsFactory = DefaultExtractorsFactory()
        mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
        renderersFactory = DefaultRenderersFactory(context)

        val trackSelector = DefaultTrackSelector()

        // TODO: customize load control...
        val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                )
                .createDefaultLoadControl()


        player = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector, loadControl);

        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                val playbackError = player.playbackError
                Timber.d("state changed: playWhenReady $playWhenReady, state: $playbackState, error: $playbackError")
                parseStatus(playWhenReady, playbackState, playbackError)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Timber.d("isLoading: %s", isLoading)
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.d(error, "Player error:")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                Timber.d("Tracks changed: $trackSelections")
                parseAudioFromatFromTrackSelections(trackSelections)
            }
        })

        player.addMetadataOutput { metadata ->
            Timber.d("Player metadata: $metadata")
            for (i in 0..metadata.length()) {
                val entry = metadata.get(0)
                when (entry) {
                    is IcyInfo -> streamMetadata.onNext(StreamMetadata(entry.title))
                }
            }
        }
    }

    fun setBufferTime(time: Long): Completable {
        // TODO
        return Completable.complete()
    }

    fun setUserAgentString(value: String): Completable {
        // TODO
        return Completable.complete()
    }

    private fun recreatePlayer() {
        // TODO: subj
        val b = BehaviorSubject.create<Boolean>()
        b.value
    }

    private fun parseStatus(playWhenReady: Boolean, playbackState: Int, playbackError: ExoPlaybackException?) {
        val status: PlayerStatus
        if (playbackError != null) {
            status = PlayerStatus(PlayerStatus.State.STOPPED_ERROR, mapError(playbackError))
        } else {
            status = when (playbackState) {
                Player.STATE_IDLE -> PlayerStatus(PlayerStatus.State.STOPPED)
                Player.STATE_BUFFERING -> PlayerStatus(PlayerStatus.State.BUFFERING)
                Player.STATE_READY -> {
                    if (playWhenReady) PlayerStatus(PlayerStatus.State.PLAYING)
                    else PlayerStatus(PlayerStatus.State.STOPPED)
                }
                Player.STATE_ENDED -> PlayerStatus(PlayerStatus.State.STOPPED)
                else -> PlayerStatus(PlayerStatus.State.STOPPED, IllegalStateException("Unknown player state: $playbackState"))
            }
        }
        playerStatus.onNext(status)
        if (status.state == PlayerStatus.State.STOPPED || status.state == PlayerStatus.State.STOPPED_ERROR) {
            streamMetadata.onNext(StreamMetadata.EMPTY)
            streamInfo.onNext(StreamInfo.EMPTY)
        }
    }

    private fun parseAudioFromatFromTrackSelections(trackSelections: TrackSelectionArray?) {
        val selectedFormat = trackSelections?.get(C.TRACK_TYPE_AUDIO)?.selectedFormat
        val radioStreamInfo =
                if (selectedFormat != null) {
                    StreamInfo(
                            if (selectedFormat.bitrate != Format.NO_VALUE) selectedFormat.bitrate else null,
                            if (selectedFormat.channelCount != Format.NO_VALUE) selectedFormat.channelCount else null,
                            if (selectedFormat.sampleRate != Format.NO_VALUE) selectedFormat.sampleRate else null
                    )
                } else {
                    StreamInfo.EMPTY
                }
        streamInfo.onNext(radioStreamInfo)
    }

    fun getStreamMetadata() = streamMetadata as Observable<StreamMetadata>

    fun getPlayerStatus() = playerStatus as Observable<PlayerStatus>

    fun getStreamInfo() = streamInfo as Observable<StreamInfo>

    fun play(stream: RadioStream): Completable {
        return Completable.fromCallable {
            val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(stream.url))
            player.prepare(mediaSource)
            player.setPlayWhenReady(true)
        }.subscribeOn(scheduler)
    }

    private fun mapError(throwable: Throwable): Throwable {
        if (throwable is HttpDataSource.HttpDataSourceException) {
            // TODO
        } else if (throwable is HttpDataSource.InvalidResponseCodeException) {
            //throwable.responseCode
        }
        //default: generic player exception
        // Http stream error: com.google.android.exoplayer2.upstream.HttpDataSource$InvalidResponseCodeException: Response code: 404
        // Timeout error (no internet): com.google.android.exoplayer2.upstream.HttpDataSource$HttpDataSourceException: Unable to connect
        return throwable
    }

    fun stop(): Completable {
        return Completable.fromCallable {
            player.setPlayWhenReady(false)
            player.stop()
            playerStatus.onNext(PlayerStatus(PlayerStatus.State.STOPPED))
            streamInfo.onNext(StreamInfo.EMPTY)
        }
                .subscribeOn(scheduler)
    }

    override fun close() {
        player.release()
    }
}