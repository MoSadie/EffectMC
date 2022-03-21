package com.mosadie.effectmc.core.property;

public class BooleanProperty extends EffectProperty{
    private boolean value;
    private final String trueLabel;
    private final String falseLabel;

    public BooleanProperty(String id, boolean value, boolean required, String label, String trueLabel, String falseLabel) {
        super(PropertyType.BOOLEAN, id, required, label);
        this.value = value;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public boolean setValue(Object newValue) {
        this.value = Boolean.parseBoolean(String.valueOf(newValue));
        return true;
    }

    @Override
    public String getAsString() {
        return Boolean.toString(value);
    }

    @Override
    public boolean getAsBoolean() {
        return value;
    }

    @Override
    public float getAsFloat() {
        return value ? 1 : 0;
    }

    @Override
    public int getAsInt() {
        return value ? 1 : 0;
    }

    @Override
    public double getAsDouble() {
        return value ? 1 : 0;
    }

    @Override
    public String getHTMLInput() {
        return "<label for=\""+ id + "\">" + getLabel() + "</label><select id=\"" + id + "\" name=\"" + id + "\"><option value=\"true\" " + (value ? "selected" : "") + ">" + trueLabel + "</option><option value=\"false\" " + (!value ? "selected" : "") + ">" + falseLabel + "</option></select>";
    }

    @Override
    public String getSDHTMLInput() {
        return "<div class=\"sdpi-item\" id=\"select_single\"><div class=\"sdpi-item-label\">" + getLabel() + "</div><select class=\"sdpi-item-value select\" id=\"" + id + "\"><option value=\"false\">" + falseLabel + "</option><option value=\"true\">" + trueLabel + "</option></select></div>";
    }
}
