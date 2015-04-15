package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class PackageMaterialProperty {

    @Expose @SerializedName("default-value")
    @Getter private String defaultValue;

    @Expose
    @Getter private String value;

    @Expose
    @Getter private Boolean secure;

    @Expose @SerializedName("part-of-identity")
    @Getter private Boolean partOfIdentity;

    @Getter private Boolean required;

    @Expose @SerializedName("display-name")
    @Getter private String displayName;

    @Expose @SerializedName("display-order")
    @Getter private String displayOrder;

    public PackageMaterialProperty() {
        this.secure = false;
        this.partOfIdentity = false;
        this.required = false;
    }

    public PackageMaterialProperty withSecure(final boolean secure) {
        this.secure = secure;
        return this;
    }

    public PackageMaterialProperty withPartOfIdentity(final boolean partOfIdentity) {
        this.partOfIdentity = partOfIdentity;
        return this;
    }

    public PackageMaterialProperty withRequired(final boolean required) {
        this.required = required;
        return this;
    }

    public PackageMaterialProperty withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PackageMaterialProperty withDisplayOrder(final int displayOrder) {
        this.displayOrder = Integer.toString(displayOrder);
        return this;
    }

    public PackageMaterialProperty withDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public PackageMaterialProperty withValue(final String value) {
        this.value = value;
        return this;
    }
}
