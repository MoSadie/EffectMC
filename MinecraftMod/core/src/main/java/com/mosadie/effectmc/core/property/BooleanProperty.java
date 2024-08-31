package com.mosadie.effectmc.core.property;

public class BooleanProperty extends EffectProperty{
    private final String trueLabel;
    private final String falseLabel;

    private boolean defaultValue;

    public BooleanProperty(String id, boolean defaultValue, boolean required, String label, String trueLabel, String falseLabel) {
        super(PropertyType.BOOLEAN, id, required, label);
        this.defaultValue = defaultValue;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        // Check if the input is a boolean or a string that can be directly parsed as a boolean
        if (input instanceof Boolean) {
            return true;
        }

        if (input instanceof String) {
            String str = (String) input;
            return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
        }

        return false;
    }

    @Override
    public String getAsString(Object input) {
        if (isValidInput(input)) {
            // Parse the input as a boolean
            return String.valueOf(Boolean.parseBoolean(String.valueOf(input)));
        } else {
            return null;
        }
    }

    @Override
    public boolean getAsBoolean(Object input) {
        if (isValidInput(input)) {
            // Parse the input as a boolean
            return Boolean.parseBoolean(String.valueOf(input));
        } else {
            return false;
        }
    }

    @Override
    public float getAsFloat(Object input) {
        return getAsBoolean(input) ? 1 : 0;
    }

    @Override
    public int getAsInt(Object input) {
        return getAsBoolean(input) ? 1 : 0;
    }

    @Override
    public double getAsDouble(Object input) {
        return getAsBoolean(input) ? 1 : 0;
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><select id=\"" + id + "\" name=\"" + id + "\"><option value=\"true\" " + (defaultValue ? "selected" : "") + ">" + trueLabel + "</option><option value=\"false\" " + (!defaultValue ? "selected" : "") + ">" + falseLabel + "</option></select>";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\" id=\"select_single\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><select class=\"sdpi-item-value select\" id=\"" + id + "\"><option value=\"false\">" + falseLabel + "</option><option value=\"true\">" + trueLabel + "</option></select></div>";
    }
}
