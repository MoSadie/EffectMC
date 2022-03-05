package com.mosadie.effectmc.core.property;

public abstract class EffectProperty {
    final PropertyType TYPE;
    final String id;
    final boolean required;
    final String label;

    EffectProperty(PropertyType type, String id, boolean required, String label) {
        this.TYPE = type;
        this.id = id;
        this.required = required;
        this.label = label;
    }

    public PropertyType getPropType() {
        return TYPE;
    }

    public abstract boolean setValue(Object newValue);

    public boolean isRequired() {
        return required;
    }

    public String getLabel() {
        return label;
    }

    public abstract String getAsString();
    public abstract String getHTMLInput();
    public abstract String getSDHTMLInput();

    public abstract boolean getAsBoolean();
    public abstract float getAsFloat();
    public abstract int getAsInt();
    public abstract double getAsDouble();

    public enum PropertyType {
        STRING,
        SELECTION,
        INTEGER,
        BOOLEAN,
        FLOAT,
        DOUBLE,
        BODY,
        COMMENT
    }
}
