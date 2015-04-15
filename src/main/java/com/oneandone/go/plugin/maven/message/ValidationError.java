package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class ValidationError {

    @Expose
    private String key;

    @Expose
    @Getter private String message;

    public ValidationError(final String key, final String message) {
        this.key = key;
        this.message = message;
    }

}
