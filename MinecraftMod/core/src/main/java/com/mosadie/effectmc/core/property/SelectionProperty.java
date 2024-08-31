package com.mosadie.effectmc.core.property;

import java.util.Arrays;
import java.util.List;

public class SelectionProperty extends EffectProperty{
    private final List<String> options;

    private String defaultValue;

    public SelectionProperty(String id, String defaultValue, boolean required, String label, String... options) {
        super(PropertyType.SELECTION, id, required, label);
        this.options = Arrays.asList(options);

        if (!this.options.contains(defaultValue)) {
            throw new IllegalArgumentException("Default value must be one of the options");
        }

        this.defaultValue = defaultValue;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        return options.contains(String.valueOf(input));
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
        String result = "<label for=\""+ id + "\">" + getLabel() + "</label><select id=\"" + id + "\" name=\"" + id + "\" " + (isRequired() ? "required" : "") + ">";
        for (String option : options) {
            result += "<option value=\"" + option + "\"" + (option.equalsIgnoreCase(defaultValue) ? "selected" : "") + ">" + option + "</option>";
        }
        result += "</select>";
        return result;
    }

    @Override
    public String getSDHTMLInput() {
        String result = "<div class=\"sdpi-item\" id=\"select_single\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><select class=\"sdpi-item-value select\" id=\"" + id + "\">";
        for (String option : options) {
            result += "<option value=\"" + option + "\">" + option + "</option>";
        }
        result += "</select></div>";

        return result;
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
