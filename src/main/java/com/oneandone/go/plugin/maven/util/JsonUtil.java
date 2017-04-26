package com.oneandone.go.plugin.maven.util;

import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Json serializer and deserializer utility class. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    private static final DateTimeFormatter dateTomeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /**
     * This method serializes the specified object into its equivalent Json representation.
     *
     * @param object the object for which Json representation is to be created
     * @return Json representation of {@code object}.
     */
    public static String toJsonString(final Object object) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter(dateTomeFormatter));
        return gsonBuilder.create().toJson(object);
    }

    /**
     * This method deserializes the specified Json into an object of the specified class.
     *
     * @param json the string from which the object is to be deserialized
     * @param type the class of {@code T}
     * @param <T> the type of the desired object
     * @return an object of type {@code T} from the Json string or {@code null} if {@code json} is {@code null}.
     * @throws com.google.gson.JsonSyntaxException if json is not a valid representation for an object of type {@code T}
     */
    public static <T> T fromJsonString(final String json, final Class<T> type) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeConverter(dateTomeFormatter));
        return gsonBuilder.create().fromJson(json, type);
    }
}
