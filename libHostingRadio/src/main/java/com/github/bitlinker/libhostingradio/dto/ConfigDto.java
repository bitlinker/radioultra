package com.github.bitlinker.libhostingradio.dto;

import java.util.Map;

public class ConfigDto {
    public String feedbackUrl;
    public String topChartUrl;
    public String serverTimeUrl;
    public Map<String, ConfigStreamDto> streams;
    //public ConfigStreamsDto streams;
}
