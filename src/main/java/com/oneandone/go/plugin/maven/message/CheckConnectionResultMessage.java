package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/** The result of a connection check. */
public class CheckConnectionResultMessage {

    /** Enumeration of response status. */
    public enum STATUS {
        /** State representing a succeeded connection check. */
        SUCCESS,

        /** State representing a failed connection check. */
        FAILURE
    }

    /** The connection check status */
    @Expose
    private STATUS status;

    /**
     * The list of messages associated with the connection check.
     *
     * @return list of messages associated with the connection check
     */
    @Expose
    @Getter private List<String> messages;

    /**
     * Constructs a connection check result.
     *
     * @param status the check status
     * @param messages list of connection check messages
     */
    public CheckConnectionResultMessage(final STATUS status, final String... messages) {
        this.status = status;
        this.messages = Arrays.asList(messages);
    }

    /**
     * Returns {@code true} if the connection check succeeded, otherwise {@code false}.
     *
     * @return {@code true} if the connection check succeeded, otherwise {@code false}
     */
    public boolean success() {
        return STATUS.SUCCESS.equals(status);
    }
}
