package com.oneandone.go.plugin.maven.message;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/** Representation of a validation result. */
public class ValidationResultMessage {

    /**
     * The list of validation errors.
     *
     * @return the list of validation errors
     */
    @Getter private final List<ValidationError> validationErrors = new ArrayList<>();

    /**
     * Adds the specified error to the list of errors.
     *
     * @param validationError the validation error to add
     */
    public void addError(final ValidationError validationError) {
        validationErrors.add(validationError);
    }

    /**
     * Adds the list of errors to the list of errors.
     *
     * @param validationErrors the validations errors to add
     */
    public void addErrors(final List<ValidationError> validationErrors) {
        this.validationErrors.addAll(validationErrors);
    }

    /**
     * Returns {@code true} if any errors exist, otherwise {@code false}.
     *
     * @return {@code true} if any errors exist, otherwise {@code false}
     */
    public boolean failure() {
        return !validationErrors.isEmpty();
    }

    /**
     * Returns {@code true} if no errors exists, otherwise {@code false}.
     *
     * @return {@code true} if no errors exists, otherwise {@code false}
     */
    public Boolean success() {
        return !failure();
    }
}
