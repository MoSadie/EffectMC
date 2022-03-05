package com.mosadie.effectmc.core.property;

public class DoubleProperty extends EffectProperty{
    private double value;

    public DoubleProperty(String id, double value, boolean required, String label) {
        super(PropertyType.DOUBLE, id, required, label);
        this.value = value;
    }

    @Override
    public boolean setValue(Object newValue) {
        try {
            value = Double.parseDouble(String.valueOf(newValue));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString() {
        return Double.toString(value);
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"number\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + getAsString() + "\" step=\"0.001\">";
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
        return Float.parseFloat(getAsString());
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
        return value;
    }
}
