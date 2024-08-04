package com.mosadie.effectmc.core.property;

public class FloatProperty extends EffectProperty{
    private final float min;
    private final float max;

    private final float defaultValue;

    public FloatProperty(String id, float defaultValue, boolean required, String label, float min, float max) {
        super(PropertyType.FLOAT, id, required, label);
        this.min = min;
        this.max = max;
        this.defaultValue = Math.max(min, Math.min(max, defaultValue));
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isValidInput(Object input) {
        if (input instanceof Float) {
            float tmp = (Float) input;
            return tmp <= max && tmp >= min;
        }

        try {
            float tmp = Float.parseFloat(String.valueOf(input));

            return tmp <= max && tmp >= min;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Float) {
                return String.valueOf(input);
            }

            return String.valueOf(Float.parseFloat(String.valueOf(input)));
        } else {
            return null;
        }
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><input type=\"number\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + getAsString(getDefaultValue()) + "\" min=\"" + min + "\" max=\"" + max + "\" step=\"0.01\">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div type=\"range\" class=\"sdpi-item\" id=\"" + id +"\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><div class=\"sdpi-item-value\"><span class=\"clickable\" value=\"" + min +"\">" + min + "</span><input type=\"range\" min=\"" + min + "\" max=\"" + max + "\" step =\"0.01\" value=" + getDefaultValue() + "list=\"volume-numbers\"><datalist id=\"" + id + "-numbers\"><option>" + getDefaultValue() + "</option></datalist><span class=\"clickable\" value=\"" + max + "\">" + max +"</span></div></div>";
    }

    @Override
    public boolean getAsBoolean(Object input) {
        if (isValidInput(input)) {
            if (input instanceof Float) {
                return (Float) input != 0;
            }

            return Float.parseFloat(getAsString(input)) != 0;
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
            if (input instanceof Float) {
                return Math.round((Float) input);
            }

            try {
                return Math.round(Float.parseFloat(getAsString(input)));
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
}
