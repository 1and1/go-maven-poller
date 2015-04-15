package com.oneandone.go.plugin.maven.message;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PackageMaterialProperties {

    private Map<String, PackageMaterialProperty> propertyMap = new LinkedHashMap<String, PackageMaterialProperty>();

    public PackageMaterialProperties(Map<String, PackageMaterialProperty> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public void addPackageMaterialProperty(String key, PackageMaterialProperty packageMaterialProperty) {
        propertyMap.put(key, packageMaterialProperty);
    }

    public PackageMaterialProperty getProperty(String key) {
        return propertyMap.get(key);
    }

    public boolean hasKey(String key) {
        return propertyMap.keySet().contains(key);
    }

    public Collection<String> keys() {
        return propertyMap.keySet();
    }

    public Map<String, PackageMaterialProperty> getPropertyMap() {
        return propertyMap;
    }

    public Optional<String> getValue(final String key) {
        if (hasKey(key) && getProperty(key).getValue() != null && !getProperty(key).getValue().trim().isEmpty()) {
            return Optional.of(getProperty(key).getValue());
        } else {
            return Optional.absent();
        }
    }
}
