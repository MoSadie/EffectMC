package com.mosadie.effectmc.core.property;

public class IntegerProperty extends EffectProperty{
    private int value;
    private final String placeholder;

    public IntegerProperty(String id, int value, boolean required, String label, String placeholder) {
        super(PropertyType.INTEGER, id, required, label);
        this.value = value;
        this.placeholder = placeholder;
    }

    @Override
    public boolean setValue(Object newValue) {
        try {
            this.value = Integer.parseInt(String.valueOf(newValue));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString() {
        return Integer.toString(value);
    }

    @Override
    public String getHTMLInput() {
        return "<span class=\"label\">" + getLabel() + "</span><input type=\"number\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><input id=\"" + id + "\" class=\"sdpi-item-value\" type=\"number\" accept=\"\" value=\"\" placeholder=\"\"/></div>";
    }

    @Override
    public boolean getAsBoolean() {
        return Boolean.parseBoolean(getAsString());
    }

    @Override
    public float getAsFloat() {
        return value;
    }

    @Override
    public int getAsInt() {
        return value;
    }

    @Override
    public double getAsDouble() {
        return value;
    }
}
