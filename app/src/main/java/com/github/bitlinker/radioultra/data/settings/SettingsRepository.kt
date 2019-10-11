package com.github.bitlinker.radioultra.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class SettingsRepository(private val context: Context,
                         private val schedulerProvider: SchedulerProvider) {
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefChangeSubject = PublishSubject.create<String>().toSerialized()

    private val prefKeyDownloadAlbumCovers by lazy { context.getString(R.string.pref_key_download_album_covers) }
    private val prefKeyUserAgentString by lazy { context.getString(R.string.pref_key_useragent_string) }
    private val prefKeyPlayOnStartup by lazy { context.getString(R.string.pref_key_play_on_startup) }
    private val prefKeyStoppedMetadataUpdateInterval by lazy { context.getString(R.string.pref_key_stopped_metadata_update_interval) }
    private val prefKeyBufferingTime by lazy { context.getString(R.string.pref_key_buffering_time) }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> prefChangeSubject.onNext(key) }

    init {
        PreferenceManager.setDefaultValues(context, R.xml.settings, false)

        // Unregistering is not needed since SharedPreferences keeps only weak reference to the listener
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun getBooleanPreferenceObservable(key: String): Observable<Boolean> =
            getPreferenceObservable(key, Function { sp.getBoolean(it, false) })

    private fun getStringPreferenceObservable(key: String): Observable<String> =
            getPreferenceObservable(key, Function { sp.getString(it, "") })

    private fun <T> getPreferenceObservable(key: String, getter: Function<String, T>): Observable<T> {
        val preferenceObservable = getPreference(key, getter)
        return Observable.concat(
                preferenceObservable.toObservable(),
                prefChangeSubject
                        .filter { it == key }
                        .flatMapSingle { preferenceObservable }
        )
    }

    private fun <T> getPreference(key: String, getter: Function<String, T>): Single<T> {
        return Single.defer {
            Single.just(getter.apply(key))
        }.subscribeOn(schedulerProvider.io())
    }

    fun isDownloadCoverObservable() = getBooleanPreferenceObservable(prefKeyDownloadAlbumCovers)

    fun userAgentStringObservable() = getStringPreferenceObservable(prefKeyUserAgentString)

    fun playOnStartupObservable() = getBooleanPreferenceObservable(prefKeyPlayOnStartup)

    fun stoppedMetadataUpdateInterval(): Observable<Long> = getStringPreferenceObservable(prefKeyStoppedMetadataUpdateInterval)
            .map { it.toLong() }

    fun bufferingTime(): Observable<Long> = getStringPreferenceObservable(prefKeyBufferingTime)
            .map { it.toLong() }
}