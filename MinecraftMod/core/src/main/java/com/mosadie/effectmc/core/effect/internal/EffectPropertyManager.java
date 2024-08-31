package com.mosadie.effectmc.core.effect.internal;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.property.*;

import java.util.*;

public class EffectPropertyManager {
    private final Map<String, EffectProperty> properties;
    private final List<EffectProperty> propertiesList;

    private boolean locked;

    public EffectPropertyManager() {
        this.properties = new HashMap<>();
        this.propertiesList = new ArrayList<>();
        this.locked = false;
    }

    public void addProperty(String propKey, EffectProperty property) {
        if (locked) {
            throw new IllegalStateException("Property manager is locked");
        }
        if (properties.containsKey(propKey)) {
            throw new IllegalArgumentException("Property key already exists: " + propKey);
        }

        properties.put(propKey, property);
        propertiesList.add(property);
    }

    public void lock() {
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }

    public EffectProperty getProperty(String propKey) {
        return properties.getOrDefault(propKey, null);
    }

    public Map<String, EffectProperty> getProperties() {
        return properties;
    }

    public List<EffectProperty> getPropertiesList() {
        return propertiesList;
    }

    public boolean argumentCheck(Map<String, Object> args) {
        return argumentCheck(args, false);
    }

    public boolean argumentCheck(Map<String, Object> args, boolean strict) {
        for (EffectProperty property : propertiesList) {
            boolean isRequired = property.isRequired() || strict;
            if (isRequired && !args.containsKey(property.getId())) {
                System.out.println("Missing required property: " + property.getId());
                return false;
            }

            // Actually validate the input
            if (args.containsKey(property.getId())) {
                if (!property.isValidInput(args.get(property.getId()))) {
                    System.out.println("Invalid input for property: " + property.getId());
                    return false;
                }
            }
        }
        return true;
    }

    public void addStringProperty(String id, String defaultValue, boolean required, String label, String placeholder) {
        addProperty(id, new StringProperty(id, defaultValue, required, label, placeholder));
    }

    public void addBooleanProperty(String id, boolean defaultValue, boolean required, String label, String trueLabel, String falseLabel) {
        addProperty(id, new BooleanProperty(id, defaultValue, required, label, trueLabel, falseLabel));
    }

    public void addFloatProperty(String id, float defaultValue, boolean required, String label, float min, float max) {
        addProperty(id, new FloatProperty(id, defaultValue, required, label, min, max));
    }

    public void addSelectionProperty(String id, String defaultValue, boolean required, String label, String... options) {
        addProperty(id, new SelectionProperty(id, defaultValue, required, label, options));
    }

    public void addIntegerProperty(String id, int defaultValue, boolean required, String label, String placeholder) {
        addProperty(id, new IntegerProperty(id, defaultValue, required, label, placeholder));
    }

    public void addBodyProperty(String id, String defaultValue, boolean required, String label, String placeholder) {
        addProperty(id, new BodyProperty(id, defaultValue, required, label, placeholder));
    }

    public void addCommentProperty(String comment) {
        String id = UUID.randomUUID().toString();
        addProperty(id, new CommentProperty(id, comment));
    }
}
