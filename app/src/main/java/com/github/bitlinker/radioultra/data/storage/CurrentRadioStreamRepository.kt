package com.github.bitlinker.radioultra.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.RadioStream
import com.github.bitlinker.radioultra.utils.OptionalWrapper
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject


private const val KEY_CURRENT_STREAM = "KEY_CURRENT_STREAM"

class CurrentRadioStreamRepository(context: Context,
                                   schedulerProvider: SchedulerProvider,
                                   moshi: Moshi) {
    private val radioStreamJsonAdapter = moshi.adapter(RadioStream::class.java)
    private val scheduler = schedulerProvider.io()
    private val sp = context.getSharedPreferences("persistent.xml", Context.MODE_PRIVATE)
    private val prefChangeSubject = PublishSubject.create<String>().toSerialized()
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> prefChangeSubject.onNext(key) }

    init {
        // Unregistering is not needed since SharedPreferences keeps only weak reference to the listener
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun RadioStream.serialize(): String {
        return radioStreamJsonAdapter.toJson(this)
    }

    private fun deserialize(value: String): RadioStream? {
        return radioStreamJsonAdapter.fromJson(value)
    }

    fun putCurrentStream(stream: RadioStream): Completable {
        return Completable.fromCallable {
            sp.edit()
                    .putString(KEY_CURRENT_STREAM, stream.serialize())
                    .apply()
        }
                .subscribeOn(scheduler)
    }

    fun getCurrentStream(): Maybe<RadioStream> {
        return Maybe.defer {
            var stream: RadioStream? = null
            val string = sp.getString(KEY_CURRENT_STREAM, null)
            if (string != null) {
                stream = deserialize(string)
            }
            return@defer if (stream != null) Maybe.just(stream) else Maybe.empty<RadioStream>()
        }
                .subscribeOn(scheduler)
    }


    fun getCurrentStreamObservable(): Observable<OptionalWrapper<RadioStream>> {
        val preferenceObservable = getCurrentStream()
                .map { OptionalWrapper.of(it) }
                .switchIfEmpty(Single.just(OptionalWrapper<RadioStream>(null)))

        return Observable.concat(
                preferenceObservable.toObservable(),
                prefChangeSubject
                        .filter { it == KEY_CURRENT_STREAM }
                        .flatMapSingle { preferenceObservable }
        )
    }
}