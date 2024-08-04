package com.mosadie.effectmc.core.property;

public class IntegerProperty extends EffectProperty{
    private final String placeholder;

    private final int defaultValue;

    public IntegerProperty(String id, int defaultValue, boolean required, String label, String placeholder) {
        super(PropertyType.INTEGER, id, required, label);
        this.placeholder = placeholder;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        if (input instanceof Integer) {
            return true;
        }

        try {
            Integer.parseInt(String.valueOf(input));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Integer) {
                return String.valueOf(input);
            }

            return String.valueOf(Integer.parseInt(String.valueOf(input)));
        } else {
            return null;
        }
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"number\" id=\"" + id + "\" name=\"" + id + "\" placeholder=\"" + placeholder + "\" " + (isRequired() ? "required" : "") + ">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><input id=\"" + id + "\" class=\"sdpi-item-value\" type=\"number\" accept=\"\" value=\"\" placeholder=\"\"/></div>";
    }

    @Override
    public boolean getAsBoolean(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Integer) {
                return (Integer) input != 0;
            }

            return Integer.parseInt(getAsString(input)) != 0;
        } else {
            return false;
        }
    }

    @Override
    public float getAsFloat(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Integer) {
                return (Integer) input;
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
            if (input instanceof Integer) {
                return (Integer) input;
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
