package com.github.bitlinker.radioultra.data.imaging

import android.content.Context
import com.squareup.picasso.Picasso

class PicassoConfigurator {

    // TODO: init picasso
//    OkHttpClient okHttpClient = new OkHttpClient();
//
//    okHttpClient.setCache(new Cache(getCacheDir(), 100 * 1024 * 1024)); //100 MB cache, use Integer.MAX_VALUE if it is too low
//    OkHttpDownloader downloader = new OkHttpDownloaderDiskCacheFirst(okHttpClient);
//
//    Picasso.Builder builder = new Picasso.Builder(this);
//
//    builder.downloader(downloader);
//
//    Picasso built = builder.build();
//
//    Picasso.setSingletonInstance(built);

    companion object {
        fun configure(context: Context, isDebug: Boolean) {
            val builder = Picasso.Builder(context);
            //builder.downloader(downloader);
            builder.indicatorsEnabled(isDebug)
            builder.loggingEnabled(isDebug)
            Picasso.setSingletonInstance(builder.build());
        }
    }
}