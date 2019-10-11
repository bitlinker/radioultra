package com.github.bitlinker.libhostingradio.dto;

import java.util.List;

public class MetaDto extends MetaTrackDto {
    public String id;
    public String metadata;
    public String album;
    public List<MetaTrackDto> prev_tracks;
}
