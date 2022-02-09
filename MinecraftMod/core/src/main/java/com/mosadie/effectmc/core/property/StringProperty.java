package com.mosadie.effectmc.core.property;

public class StringProperty extends EffectProperty{
    private String value;
    private final String placeholder;

    public StringProperty(String id, String value, boolean required, String label, String placeholder) {
        super(PropertyType.STRING, id, required, label);
        this.value = value;
        this.placeholder = placeholder;
    }

    @Override
    public boolean setValue(Object newValue) {
        this.value = String.valueOf(newValue);
        return true;
    }

    @Override
    public String getAsString() {
        return value;
    }

    @Override
    public String getHTMLInput() {
        return "<span class=\"label\">" + getLabel() + "</span><input type=\"text\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><input id=\"" + id + "\" class=\"sdpi-item-value\" type=\"text\" value=\"\" placeholder=\"" + placeholder + "\"/></div>";
    }

    @Override
    public boolean getAsBoolean() {
        return Boolean.parseBoolean(getAsString());
    }

    @Override
    public float getAsFloat() {
        try {
            return Float.parseFloat(getAsString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int getAsInt() {
        try {
            return Integer.parseInt(getAsString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public double getAsDouble() {
        try {
            return Double.parseDouble(getAsString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
