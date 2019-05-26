package com.oneandone.go.plugin.maven.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Json (GSON) serializer/deserializer for converting JAVA 8 time api {@link ZonedDateTime} objects.
 *
 * @see JsonDeserializer
 * @see JsonDeserializer
 */
class ZonedDateTimeConverter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

    /** Format specifier. */
    private final DateTimeFormatter dateTimeFormatter;

    ZonedDateTimeConverter(final DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(dateTimeFormatter, "dateTimeFormatter is null");
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public JsonElement serialize(ZonedDateTime zonedDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(zonedDateTime.format(dateTimeFormatter));
    }

    @Override
    public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.getAsString() == null || jsonElement.getAsString().isEmpty()) {
            return null;
        }
        //No timezone is provided in message -> need to add the default
        final LocalDateTime withoutTimezone = LocalDateTime.parse(jsonElement.getAsString(), dateTimeFormatter);
        return ZonedDateTime.of(withoutTimezone, ZoneId.systemDefault());
    }
}
