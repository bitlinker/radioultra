package com.github.bitlinker.libhostingradio;

import com.github.bitlinker.libhostingradio.dto.MetaTrackDto;
import com.github.bitlinker.libhostingradio.impl.DateMoshiAdapter;
import com.github.bitlinker.libhostingradio.dto.ConfigDto;
import com.github.bitlinker.libhostingradio.dto.ConfigStreamDto;
import com.github.bitlinker.libhostingradio.dto.MetaDto;
import com.github.bitlinker.libhostingradio.impl.HostingRadioRetrofitApi;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class HostingRadioApi {
    public static class Config {
        public static final HttpUrl BASE_URL_ULTRA = HttpUrl.parse("https://metaultrafm.hostingradio.ru/");

        private long configCacheTimeMs = 86400000L; // 24 hrs
        private HttpUrl baseUrl = BASE_URL_ULTRA;
        private Scheduler scheduler = Schedulers.io();

        public HttpUrl getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(HttpUrl baseUrl) {
            this.baseUrl = baseUrl;
        }

        public long getConfigCacheTimeMs() {
            return configCacheTimeMs;
        }

        public void setConfigCacheTimeMs(long configCacheTimeMs) {
            this.configCacheTimeMs = configCacheTimeMs;
        }

        public Scheduler getScheduler() {
            return scheduler;
        }

        public void setScheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
        }
    }

    public static final String STREAM_KEY_HIGH = "highStream";
    public static final String STREAM_KEY_LOW = "lowStream";

    private final Config config;
    private final HostingRadioRetrofitApi api;

    private @Nullable
    ConfigDto configDtoCache;
    private long lastConfigDtoCache = 0L;

    public HostingRadioApi(Config config) {
        this.config = config;

        Moshi moshi = new Moshi.Builder()
                .add(new DateMoshiAdapter())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fake-base-url.com/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        api = retrofit.create(HostingRadioRetrofitApi.class);
    }

    public Single<ConfigDto> getConfig(boolean forceInvalidateCache) {
        return Single.defer(() -> {
            if (!forceInvalidateCache) {
                synchronized (this) {
                    if (configDtoCache != null && System.currentTimeMillis() - lastConfigDtoCache < config.getConfigCacheTimeMs()) {
                        return Single.just(configDtoCache);
                    }
                }
            }
            return getConfigFromNetwork()
                    .doOnSuccess(configDto -> {
                        synchronized (this) {
                            configDtoCache = configDto;
                            lastConfigDtoCache = System.currentTimeMillis();
                        }
                    });
        });
    }

    private ConfigDto fixConfigDto(ConfigDto dto) throws IOException {
        dto.feedbackUrl = fixRelativeUrl(dto.feedbackUrl);
        dto.topChartUrl = fixRelativeUrl(dto.topChartUrl);
        dto.serverTimeUrl = fixRelativeUrl(dto.serverTimeUrl);
        for (ConfigStreamDto value : dto.streams.values()) {
            value.streamUrl = fixRelativeUrl(value.streamUrl);
            value.idUrl = fixRelativeUrl(value.idUrl);
            value.metaUrl = fixRelativeUrl(value.metaUrl);
            value.likeUrl = fixRelativeUrl(value.likeUrl);
            value.dislikeUrl = fixRelativeUrl(value.dislikeUrl);
            value.likeStateUrl = fixRelativeUrl(value.likeStateUrl);
            value.utmReferer = fixRelativeUrl(value.utmReferer);
        }
        return dto;
    }

    private MetaDto fixMetaDto(MetaDto dto) throws IOException {
        fixMetaTrackDto(dto);
        if (dto.prev_tracks != null) {
            for (MetaTrackDto prev_track : dto.prev_tracks) {
                fixMetaTrackDto(prev_track);
            }
        }
        return dto;
    }

    private void fixMetaTrackDto(MetaTrackDto dto) throws IOException {
        dto.cover = fixRelativeUrl(dto.cover);
        dto.itunes_url = fixRelativeUrl(dto.itunes_url);
        dto.google_url = fixRelativeUrl(dto.google_url);
        dto.youtube_url = fixRelativeUrl(dto.youtube_url);
    }

    private Single<ConfigDto> getConfigFromNetwork() {
        return api.getConfig(config.getBaseUrl() + "mobile/config.json")
                .map(this::fixConfigDto)
                .subscribeOn(config.scheduler);
    }

    public Single<MetaDto> getMeta(String streamKey) {
        return getConfig(false)
                .flatMap(configDto -> {
                    ConfigStreamDto configStreamDto = configDto.streams.get(streamKey);
                    if (configStreamDto == null) {
                        throw new IOException("Stream not found: " + streamKey);
                    }
                    return api.getMeta(configStreamDto.metaUrl)
                            .map(this::fixMetaDto)
                            .subscribeOn(config.scheduler);
                });
    }

    private @Nullable
    String fixRelativeUrl(@Nullable String src) throws IOException {
        if (src == null || src.isEmpty()) return src;
        final HttpUrl resolved = config.getBaseUrl().resolve(src);
        if (resolved == null) throw new IOException("Wrong url: " + src);
        return resolved.toString();
    }
}
