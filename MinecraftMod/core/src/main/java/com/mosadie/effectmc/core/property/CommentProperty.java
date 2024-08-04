package com.mosadie.effectmc.core.property;

public class CommentProperty extends EffectProperty{

    private String comment;

    public CommentProperty(String id, String comment) {
        super(PropertyType.COMMENT, id, false, "Note");
        this.comment = comment;
    }

    @Override
    public Object getDefaultValue() {
        return comment;
    }

    @Override
    public boolean isValidInput(Object input) {
        // String.valueOf will always return a value, so this will always return true
        return true;
    }

    @Override
    public String getAsString(Object input) {
        return comment;
    }

    @Override
    public String getHTMLInput() {
        return "<p class=\"comment\" id=\"" + id + "\" name=\"" + id + "\">" + getDefaultValue() + "</p>";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><details class=\"message info\"><summary>" + getDefaultValue() + "</summary></details></div>";
    }

    // Special case for CommentProperty, the input is always ignored.

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
