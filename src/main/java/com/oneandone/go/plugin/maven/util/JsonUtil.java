package com.oneandone.go.plugin.maven.util;

import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    public static String toJsonString(final Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.create().toJson(object);
    }

    public static <T> T fromJsonString(final String json, final Class<T> type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.create().fromJson(json, type);
    }
}
