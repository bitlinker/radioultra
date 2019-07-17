package com.github.bitlinker.radioultra.data.storage

import android.content.Context
import com.github.bitlinker.radioultra.data.schedulers.SchedulerProvider
import com.github.bitlinker.radioultra.domain.RadioStream
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Maybe
import java.util.concurrent.Callable


private const val KEY_CURRENT_STREAM = "KEY_CURRENT_STREAM"

class PersistentStorage(context: Context,
                        schedulerProvider: SchedulerProvider,
                        moshi: Moshi) {
    private val radioStreamJsonAdapter = moshi.adapter(RadioStream::class.java)
    private val scheduler = schedulerProvider.io()
    private val sp = context.getSharedPreferences("persistent.xml", Context.MODE_PRIVATE)

    private fun RadioStream.serialize(): String {
        return radioStreamJsonAdapter.toJson(this)
    }

    private fun deserialize(value: String): RadioStream? {
        return radioStreamJsonAdapter.fromJson(value)
    }

    fun putCurrentStream(stream: RadioStream): Completable {
        return Completable.fromCallable(Callable {
            sp.edit()
                    .putString(KEY_CURRENT_STREAM, stream.serialize())
                    .apply()
        })
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

}