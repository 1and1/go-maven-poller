package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/** Representation of a configuration property definition. */
public class PackageMaterialProperty {

    /**
     * The default value for {@code this} property.
     *
     * @return the default value for {@code this} property
     */
    @Expose @SerializedName("default-value")
    @Getter private String defaultValue;

    /**
     * The value for {@code this} property.
     *
     * @return the value for {@code this} property
     */
    @Expose
    @Getter private String value;

    /**
     * Flag indicating whether the content of {@code this} property value should be displayed hidden.
     *
     * @return {@code true} if value should be hidden, otherwise {@code false}
     */
    @Expose
    @Getter private Boolean secure;

    /**
     * Flag indicating whether {@code this} property is part of the configuration identifier.
     *
     * @return {@code true} if {@code this} property is part of the identity, otherwise {@code false}
     */
    @Expose @SerializedName("part-of-identity")
    @Getter private Boolean partOfIdentity;

    /**
     * FLag indicating whether a value for {@code this} property is required
     *
     * @return {@code true} if value is required, otherwise {@code false}
     */
    @Getter private Boolean required;

    /**
     * The display name for {@code this} property.
     *
     * @return the display name for {@code this} property
     */
    @Expose @SerializedName("display-name")
    @Getter private String displayName;

    /**
     * The display order for {@code this} property.
     *
     * @return the display order for {@code this} property
     */
    @Expose @SerializedName("display-order")
    @Getter private String displayOrder;

    /**
     * Constructs a property.
     * <p />
     * All {@code boolean} properties are set to {@code false} as default value.
     */
    public PackageMaterialProperty() {
        this.secure = false;
        this.partOfIdentity = false;
        this.required = false;
    }

    /**
     * Sets whether value should be hidden.
     * <p />
     * Default is {@code false}.
     *
     * @param secure {@code true} if value should be hidden, otherwise {@code false}
     * @return {@code this} property
     */
    public PackageMaterialProperty withSecure(final boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Sets whether property is part of identity.
     * <p />
     * Default is {@code false}.
     *
     * @param partOfIdentity {@code true} if {@code this} property is part of the identity, otherwise {@code false}
     * @return {@code this} property
     */
    public PackageMaterialProperty withPartOfIdentity(final boolean partOfIdentity) {
        this.partOfIdentity = partOfIdentity;
        return this;
    }

    /**
     * Sets whether a value is required.
     * <p />
     * Default is {@code false}.
     *
     * @param required {@code true} if value is required, otherwise {@code false}
     * @return {@code this} property
     */
    public PackageMaterialProperty withRequired(final boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the display name to set
     * @return {@code this} property
     */
    public PackageMaterialProperty withDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Sets the display order.
     *
     * @param displayOrder the display order to set
     * @return {@code this} property
     */
    public PackageMaterialProperty withDisplayOrder(final int displayOrder) {
        this.displayOrder = Integer.toString(displayOrder);
        return this;
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue the default value to set
     * @return {@code this} property
     */
    public PackageMaterialProperty withDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     * @return {@code this} property
     */
    public PackageMaterialProperty withValue(final String value) {
        this.value = value;
        return this;
    }
}
