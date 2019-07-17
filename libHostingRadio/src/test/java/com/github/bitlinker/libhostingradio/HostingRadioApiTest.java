package com.github.bitlinker.libhostingradio;

import com.github.bitlinker.libhostingradio.dto.ConfigDto;
import com.github.bitlinker.libhostingradio.dto.MetaDto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HostingRadioApiTest {
    private HostingRadioApi api;

    @Before
    public void setup() {
        HostingRadioApi.Config config = new HostingRadioApi.Config();
        api = new HostingRadioApi(config);
    }

    @Test
    public void getConfigTest() {
        ConfigDto configDto = api.getConfig(true).blockingGet();
        assertNotNull(configDto.streams);
        assertEquals(
                "https://metaultrafm.hostingradio.ru/current.json",
                configDto.streams.get(HostingRadioApi.STREAM_KEY_HIGH).metaUrl
        );
    }

    @Test
    public void getMetaTest() {
        MetaDto metaDto = api.getMeta(HostingRadioApi.STREAM_KEY_HIGH).blockingGet();
        assertNotNull(metaDto.metadata);
    }
}