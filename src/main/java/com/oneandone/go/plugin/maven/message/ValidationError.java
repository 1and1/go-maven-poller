package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Representation of a validation error. */
@EqualsAndHashCode
public class ValidationError {

    /** The error key. */
    @Expose
    private String key;

    /**
     * The error message.
     *
     * @return the error message
     */
    @Expose
    @Getter private String message;

    /**
     * Constructs a validation error.
     *
     * @param key the error key
     * @param message the error message
     */
    public ValidationError(final String key, final String message) {
        this.key = key;
        this.message = message;
    }

}
