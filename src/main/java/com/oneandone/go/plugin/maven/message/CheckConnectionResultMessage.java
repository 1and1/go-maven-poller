package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class CheckConnectionResultMessage {

    public enum STATUS {SUCCESS, FAILURE}

    @Expose
    private STATUS status;

    @Expose
    @Getter private List<String> messages;

    public CheckConnectionResultMessage(final STATUS status, final String... messages) {
        this.status = status;
        this.messages = Arrays.asList(messages);
    }

    public boolean success() {
        return STATUS.SUCCESS.equals(status);
    }
}
