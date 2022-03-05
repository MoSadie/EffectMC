package com.mosadie.effectmc.core.property;

public class CommentProperty extends EffectProperty{
    private String value;

    public CommentProperty(String id, String value) {
        super(PropertyType.COMMENT, id, false, "Note");
        this.value = value;
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
        return "<p class=\"comment\" id=\"" + id + "\" name=\"" + id + "\">" + getAsString() + "</p>";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><details class=\"message info\"><summary>" + getAsString() + "</summary></details></div>";
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
