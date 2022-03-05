package com.mosadie.effectmc.core.property;

public class BodyProperty extends EffectProperty{
    private String value;
    private final String placeholder;

    public BodyProperty(String id, String value, boolean required, String label, String placeholder) {
        super(PropertyType.BODY, id, required, label);
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
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"textarea\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div type=\"textarea\" class=\"sdpi-item\" id=\"label\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><span class=\"sdpi-item-value textarea\"><textarea type=\"textarea\" id=\"" + id + "\"></textarea></span></div>";
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
