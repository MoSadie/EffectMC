package com.mosadie.effectmc.core.property;

public class FloatProperty extends EffectProperty{
    private float value;
    private final float min;
    private final float max;

    public FloatProperty(String id, float value, boolean required, String label, float min, float max) {
        super(PropertyType.FLOAT, id, required, label);
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean setValue(Object newValue) {
        try {
            float tmp = Float.parseFloat(String.valueOf(newValue));

            if (tmp > max || tmp < min)
                return false;

            value = tmp;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getAsString() {
        return Float.toString(value);
    }

    @Override
    public String getHTMLInput() {
        return "<span class=\"label\">" + getLabel() + "</span><input type=\"range\" id=\"" + id + "\" name=\"" + id + "\" value=\"" + getAsString() + "\" min=\"" + min + "\" max=\"" + max + "\" step=\"0.01\">";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div type=\"range\" class=\"sdpi-item\" id=\"" + id +"\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><div class=\"sdpi-item-value\"><span class=\"clickable\" value=\"" + min +"\">" + min + "</span><input type=\"range\" min=\"" + min + "\" max=\"" + max + "\" step =\"0.01\" value=" + value + "list=\"volume-numbers\"><datalist id=\"" + id + "-numbers\"><option>" + value + "</option></datalist><span class=\"clickable\" value=\"" + max + "\">" + max +"</span></div></div>";
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
