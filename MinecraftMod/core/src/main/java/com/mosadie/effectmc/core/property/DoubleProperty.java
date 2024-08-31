package com.mosadie.effectmc.core.property;

public class DoubleProperty extends EffectProperty{

    private final double defaultValue;
    public DoubleProperty(String id, double defaultValue, boolean required, String label) {
        super(PropertyType.DOUBLE, id, required, label);
        this.defaultValue = defaultValue;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        if (input instanceof Double) {
            return true;
        }

        try {
            Double.parseDouble(String.valueOf(input));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Double) {
                return String.valueOf(input);
            }

            return String.valueOf(Double.parseDouble(String.valueOf(input)));
        } else {
            return null;
        }
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"number\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + getAsString(getDefaultValue()) + "\" step=\"0.001\">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><input id=\"" + id + "\" class=\"sdpi-item-value\" type=\"number\" accept=\"\" value=\"\" placeholder=\"\"/></div>";
    }

    @Override
    public boolean getAsBoolean(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Double) {
                return (Double) input != 0;
            }

            return Double.parseDouble(getAsString(input)) != 0;
        } else {
            return false;
        }
    }

    @Override
    public float getAsFloat(Object input) {
        if (isValidInput(input)) {
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
