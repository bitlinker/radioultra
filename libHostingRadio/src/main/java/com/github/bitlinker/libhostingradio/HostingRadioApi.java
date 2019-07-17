package com.github.bitlinker.libhostingradio;

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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class HostingRadioApi {
    public static class Config {
        public static final String CONFIG_URL_ULTRA = "https://metaultrafm.hostingradio.ru/mobile/config.json";

        private long configCacheTimeMs = 86400000L; // 24 hrs
        private String configUrl = CONFIG_URL_ULTRA;
        private Scheduler scheduler = Schedulers.io();

        public String getConfigUrl() {
            return configUrl;
        }

        public void setConfigUrl(String configUrl) {
            this.configUrl = configUrl;
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

    private Single<ConfigDto> getConfigFromNetwork() {
        return api.getConfig(config.getConfigUrl())
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
                            .subscribeOn(config.scheduler);
                });
    }
}
