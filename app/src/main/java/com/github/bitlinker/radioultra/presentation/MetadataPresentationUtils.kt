package com.github.bitlinker.radioultra.presentation

import com.github.bitlinker.radioultra.domain.TrackMetadata
import com.squareup.picasso.Picasso

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

    fun getTitle(trackMetadata: TrackMetadata): String {
        if (trackMetadata.artist != null && trackMetadata.title != null) {
            return "$trackMetadata.artist - $trackMetadata.title"
        }
        return trackMetadata.streamTitle ?: ""
    }

    fun getSubtitle(trackMetadata: TrackMetadata): String {
        return trackMetadata.album ?: ""
    }
}