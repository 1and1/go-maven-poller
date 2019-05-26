package com.oneandone.go.plugin.maven.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Representation of configuration properties. */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PackageMaterialProperties {

    /** The property map. */
    private Map<String, PackageMaterialProperty> propertyMap = new LinkedHashMap<>();

    /**
     * Constructs a new instance of configuration properties.
     *
     * @param propertyMap the property map
     */
    public PackageMaterialProperties(final Map<String, PackageMaterialProperty> propertyMap) {
        this.propertyMap = propertyMap;
    }

    /**
     * Add a new property.
     *
     * @param key the property key
     * @param packageMaterialProperty the property definition
     */
    public void addPackageMaterialProperty(final String key, final PackageMaterialProperty packageMaterialProperty) {
        propertyMap.put(key, packageMaterialProperty);
    }

    /**
     * Returns the property definition for the specified {@code key}.
     *
     * @param key the key
     * @return the property definition or {@code null}
     */
    private PackageMaterialProperty getProperty(final String key) {
        return propertyMap.get(key);
    }

    /**
     * Returns {@code true} if a property definition for the specified {@code key} is defined, otherwise {@code false}.
     *
     * @param key the key
     * @return {@code true} if a property definition for the specified {@code key} is defined, otherwise {@code false}
     */
    private boolean hasKey(final String key) {
        return propertyMap.keySet().contains(key);
    }

    /**
     * Returns a collection of all property keys.
     *
     * @return collection of all property keys
     */
    public Collection<String> keys() {
        return propertyMap.keySet();
    }

    /**
     * Returns the property map.
     *
     * @return the property map
     */
    public Map<String, PackageMaterialProperty> getPropertyMap() {
        return propertyMap;
    }

    /**
     * Returns an {@link Optional} for the value of the property for the specified {@code key}.
     *
     * @param key the key
     * @return {@link Optional} for the value of the property for the specified {@code key}
     */
    public Optional<String> getValue(final String key) {
        if (hasKey(key) && getProperty(key).getValue() != null && !getProperty(key).getValue().trim().isEmpty()) {
            return Optional.of(getProperty(key).getValue());
        } else {
            return Optional.empty();
        }
    }
}
