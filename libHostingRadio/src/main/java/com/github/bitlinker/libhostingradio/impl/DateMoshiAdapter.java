package com.github.bitlinker.libhostingradio.impl;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateMoshiAdapter {
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @ToJson
    synchronized String dateToJson(Date d) {
        return DATE_FORMAT.format(d);
    }

    @FromJson
    synchronized Date dateToJson(String s) throws ParseException {
        return DATE_FORMAT.parse(s);
    }
}
