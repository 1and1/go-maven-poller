package com.oneandone.go.plugin.maven.message;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessage {

    @Getter private List<ValidationError> validationErrors = new ArrayList<ValidationError>();

    public void addError(final ValidationError validationError) {
        validationErrors.add(validationError);
    }

    public void addErrors(final List<ValidationError> validationErrors) {
        this.validationErrors.addAll(validationErrors);
    }

    public boolean failure() {
        return !validationErrors.isEmpty();
    }

    public Boolean success() {
        return !failure();
    }
}
