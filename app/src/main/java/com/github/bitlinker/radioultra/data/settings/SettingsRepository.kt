package com.github.bitlinker.radioultra.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.bitlinker.radioultra.R
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.functions.Function

class SettingsRepository(private val context: Context,
                         private val scheduler: Scheduler) {
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)
    private val prefChangeSubject = PublishSubject.create<String>().toSerialized()

    private val prefKeyDownloadAlbumCovers by lazy { context.getString(R.string.pref_key_download_album_covers) }
    private val prefKeyDownloadAlbumCoversDefault by lazy { context.getString(R.string.pref_key_download_album_covers_default).toBoolean() }

    private val prefKeyUserAgentString by lazy { context.getString(R.string.pref_key_useragent_string) }
    private val prefKeyUserAgentStringDefault by lazy { context.getString(R.string.pref_key_useragent_string_default) }

    private val prefKeyPlayOnStartup by lazy { context.getString(R.string.pref_key_play_on_startup) }
    private val prefKeyPlayOnStartupDefault by lazy { context.getString(R.string.pref_key_play_on_startup_default).toBoolean() }


    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> prefChangeSubject.onNext(key) }

    init {
        // Unregistering is not needed since SharedPreferences keeps only weak reference to the listener
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun getBooleanPreferenceObservable(key: String, defaultValue: Boolean): Observable<Boolean> =
            getPreferenceObservable(key, Function { sp.getBoolean(it, defaultValue) })

    private fun getStringPreferenceObservable(key: String, defaultValue: String): Observable<String> =
            getPreferenceObservable(key, Function { sp.getString(it, defaultValue) })

    private fun <T> getPreferenceObservable(key: String, getter: Function<String, T>): Observable<T> {
        val preferenceObservable = getPreference(key, getter)
        return Observable.concat(
                preferenceObservable,
                prefChangeSubject
                        .filter { it == key }
                        .flatMap { preferenceObservable }
        )
    }

    private fun <T> getPreference(key: String, getter: Function<String, T>): Observable<T> {
        return Observable.defer {
            Observable.just(getter.apply(key))
        }.subscribeOn(scheduler)
    }

    fun isDownloadCoverObservable() = getBooleanPreferenceObservable(
            prefKeyDownloadAlbumCovers,
            prefKeyDownloadAlbumCoversDefault
    )

    fun userAgentStringObservable() = getStringPreferenceObservable(
            prefKeyUserAgentString,
            prefKeyUserAgentStringDefault
    )

    fun playOnStartupObservable() = getBooleanPreferenceObservable(
            prefKeyPlayOnStartup,
            prefKeyPlayOnStartupDefault
    )

    // TODO: more settings
    fun getBufferingTime(): Long {
        return 5000
    }
}