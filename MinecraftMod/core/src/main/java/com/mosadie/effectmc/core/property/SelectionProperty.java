package com.mosadie.effectmc.core.property;

import java.util.Arrays;
import java.util.List;

public class SelectionProperty extends EffectProperty{
    private final List<String> options;
    private String selected;

    public SelectionProperty(String id, String selected, boolean required, String label, String... options) {
        super(PropertyType.SELECTION, id, required, label);
        this.options = Arrays.asList(options);

        if (this.options.contains(selected)) {
            this.selected = selected;
        } else {
            this.selected = "";
        }
    }

    @Override
    public boolean setValue(Object newValue) {
        String newValueString = String.valueOf(newValue);
        if (!options.contains(newValueString)) {
            return false;
        }
        this.selected = newValueString;
        return true;
    }

    @Override
    public String getAsString() {
        return selected;
    }

    @Override
    public String getHTMLInput() {
        String result = "<span class=\"label\">" + getLabel() + "</span><select id=\"" + id + "\" name=\"" + id + "\" " + (isRequired() ? "required" : "") + ">";
        for (String option : options) {
            result += "<option value=\"" + option + "\"" + (option == selected ? "selected" : "") + ">" + option + "</option>";
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
