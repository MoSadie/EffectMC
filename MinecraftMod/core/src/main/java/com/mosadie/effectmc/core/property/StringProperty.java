package com.mosadie.effectmc.core.property;

public class StringProperty extends EffectProperty{
    private final String placeholder;

    private final String defaultValue;

    public StringProperty(String id, String defaultValue, boolean required, String label, String placeholder) {
        super(PropertyType.STRING, id, required, label);
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
        if (isValidInput(input)) {
            return String.valueOf(input);
        } else {
            return null;
        }
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"text\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><input id=\"" + id + "\" class=\"sdpi-item-value\" type=\"text\" value=\"\" placeholder=\"" + placeholder + "\"/></div>";
    }

    @Override
    public boolean getAsBoolean(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Boolean) {
                return (Boolean) input;
            }

            return Boolean.parseBoolean(getAsString(input));
        } else {
            return false;
        }
    }

    @Override
    public float getAsFloat(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Float) {
                return (Float) input;
            }

            try {
                return Float.parseFloat(getAsString(input));
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public int getAsInt(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Integer) {
                return (Integer) input;
            }
            try {
                return Integer.parseInt(getAsString(input));
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public double getAsDouble(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Double) {
                return (Double) input;
            }

            try {
                return Double.parseDouble(getAsString(input));
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
