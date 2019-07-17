package com.github.bitlinker.libhostingradio.impl;

import com.github.bitlinker.libhostingradio.dto.ConfigDto;
import com.github.bitlinker.libhostingradio.dto.MetaDto;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface HostingRadioRetrofitApi {
    @GET
    Single<ConfigDto> getConfig(@Url String baseUrl);

    @GET
    Single<MetaDto> getMeta(@Url String baseUrl);
}
