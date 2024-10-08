package com.mosadie.effectmc.core.property;

public abstract class EffectProperty {
    final PropertyType TYPE;
    final String id;
    final boolean required;
    final String label;

    final String sdPropType;

    EffectProperty(PropertyType type, String id, boolean required, String label) {
        this.TYPE = type;
        this.id = id;
        this.required = required;
        this.label = label;

        switch (type) {
            case FLOAT:
            case DOUBLE:
            case INTEGER:
                this.sdPropType = "number";
                break;

            case BOOLEAN:
                this.sdPropType = "boolean";
                break;

            case BODY:
            case STRING:
            case SELECTION:
            case COMMENT:
            default:
                this.sdPropType = "string";
                break;
        }
    }

    public String getId() {
        return id;
    }

    public PropertyType getPropType() {
        return TYPE;
    }

    public abstract boolean isValidInput(Object input);

    public boolean isRequired() {
        return required;
    }

    public String getLabel() {
        return label;
    }

    public abstract Object getDefaultValue();

    public abstract String getAsString(Object input);
    public abstract String getHTMLInput();

    public abstract String getSDHTMLInput();

    public abstract boolean getAsBoolean(Object input);
    public abstract float getAsFloat(Object input);
    public abstract int getAsInt(Object input);
    public abstract double getAsDouble(Object input);

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
