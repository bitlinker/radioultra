package com.github.bitlinker.radioultra.presentation

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.bitlinker.radioultra.R
import com.github.bitlinker.radioultra.domain.StreamInfo
import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("setTextOrHide")
fun setTextOrHide(tv: TextView, value: String?) {
    if (TextUtils.isEmpty(value)) {
        tv.visibility = View.GONE
    } else {
        tv.visibility = View.VISIBLE
        tv.text = value
    }
}

@BindingAdapter("hideIfEmpty")
fun hideIfEmpty(v: View, value: String?) {
    if (TextUtils.isEmpty(value)) {
        v.visibility = View.GONE
    } else {
        v.visibility = View.VISIBLE
    }
}

// TODO: inject picasso here
@BindingAdapter("coverImageUrl")
fun loadImage(view: ImageView, url: String?) {
    val picasso = Picasso.get()

    if (url == null) {
        picasso.load(R.drawable.ultralogo).into(view)
    } else {
        picasso
                .load(url)
                .placeholder(R.drawable.ultralogo)
                .error(R.drawable.ultralogo)
                .into(view)
    }
}

private val DATE_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
@BindingAdapter("historyItemDate")
fun loadImage(view: TextView, date: Date?) {
    view.text = if (date != null) DATE_FORMAT.format(date) else ""
}

fun getTitle(trackMetadata: TrackMetadata): String {
    if (trackMetadata.artist != null && trackMetadata.title != null) {
        return "$trackMetadata.artist - $trackMetadata.title"
    }
    return trackMetadata.streamTitle ?: ""
}

fun getTrackOrStreamTitle(trackMetadata: TrackMetadata): String? =
        if (trackMetadata.title != null) trackMetadata.title
        else trackMetadata.streamTitle

fun getSubtitle(trackMetadata: TrackMetadata): String {
    return trackMetadata.album ?: ""
}

class MetadataPresentationUtils(private val picasso: Picasso) {
    // TODO

//    fun getCoverImage(metadata: TrackMetadata): Bitmap {
//        Single.create {
//            // TODO: observable?
//            if (metadata.coverLink == null) {
//                picasso.load(R.drawable.ultralogo).fetch(object: Callback {
//                    override fun onSuccess() {
//
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                    override fun onError(e: Exception?) {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                })
//                }
//            } else {
//                return picasso
//                        .load(metadata.coverLink)
//                        .error(R.drawable.ultralogo)
//                        .get()
//            }
//        }
//
    //}


    @BindingAdapter("streamInfoText")
    fun setStreamInfoText(tv: TextView, streamInfo: StreamInfo?) {
        if (streamInfo == null || streamInfo == StreamInfo.EMPTY) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            val sb = StringBuilder()
            if (streamInfo.bitrate != null) {
                sb.append(tv.context.getString(R.string.fragment_player_streaminfo_bitrate, streamInfo.bitrate / 1000))
                sb.append(' ')
            }
            if (streamInfo.sampleRate != null) {
                sb.append(tv.context.getString(R.string.fragment_player_streaminfo_samplerate, streamInfo.sampleRate / 1000F))
                sb.append(' ')
            }
            if (streamInfo.channels != null) {
                when (streamInfo.channels) {
                    1 -> sb.append(tv.context.getString(R.string.fragment_player_streaminfo_mono))
                    2 -> sb.append(tv.context.getString(R.string.fragment_player_streaminfo_stereo))
                    else -> {
                    }
                }
                sb.append(' ')
            }
            tv.text = tv.context.getString(R.string.fragment_player_streaminfo_stream, sb.toString())
        }
    }
}