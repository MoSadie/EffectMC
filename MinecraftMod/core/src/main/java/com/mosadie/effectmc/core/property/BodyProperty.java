package com.mosadie.effectmc.core.property;

public class BodyProperty extends EffectProperty{
    private final String defaultValue;
    private final String placeholder;

    public BodyProperty(String id, String defaultValue, boolean required, String label, String placeholder) {
        super(PropertyType.BODY, id, required, label);
        this.placeholder = placeholder;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        // String.valueOf will always return a value, so this will always return true
        return true;
    }

    @Override
    public String getAsString(Object input) {
        return String.valueOf(input);
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"textarea\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div type=\"textarea\" class=\"sdpi-item\" id=\"label\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><span class=\"sdpi-item-value textarea\"><textarea type=\"textarea\" id=\"" + id + "\"></textarea></span></div>";
    }

    @Override
    public boolean getAsBoolean(Object input) {
        return Boolean.parseBoolean(getAsString(input));
    }

    @Override
    public float getAsFloat(Object input) {
        try {
            return Float.parseFloat(getAsString(input));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int getAsInt(Object input) {
        try {
            return Integer.parseInt(getAsString(input));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public double getAsDouble(Object input) {
        try {
            return Double.parseDouble(getAsString(input));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
