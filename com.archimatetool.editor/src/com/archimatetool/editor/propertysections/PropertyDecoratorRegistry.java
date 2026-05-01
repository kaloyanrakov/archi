package com.archimatetool.editor.propertysections;

import java.util.ArrayList;
import java.util.List;

public class PropertyDecoratorRegistry {

    private static final List<IPropertyDecorator> decorators = new ArrayList<>();

    static {
        decorators.add(new LevelingPropertyDecorator());
        // To add more: decorators.add(new YourNewDecorator());
    }

    public static IPropertyDecorator getDecorator(String propertyKey) {
        return decorators.stream()
            .filter(d -> d.getPropertyKey().equals(propertyKey))
            .findFirst()
            .orElse(null);
    }

    public static String[] getRestrictedValues(String propertyKey) {
        IPropertyDecorator d = getDecorator(propertyKey);
        return d != null ? d.getRestrictedValues() : null;
    }

    public static List<IPropertyDecorator> getAllDecorators() {
        return decorators;
    }
}