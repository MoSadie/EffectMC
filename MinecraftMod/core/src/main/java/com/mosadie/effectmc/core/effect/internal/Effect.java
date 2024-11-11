package com.mosadie.effectmc.core.effect.internal;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.*;

public abstract class Effect {
    public Effect() {
        propertyManager = new EffectPropertyManager();
    }
    public abstract String getEffectName();
    public String getEffectId() {
        return getEffectName().replaceAll(" ", "").toLowerCase();
    }
    public abstract String getEffectTooltip();

    private final EffectPropertyManager propertyManager;
    public EffectPropertyManager getPropertyManager() {
        return propertyManager;
    }
    public abstract EffectResult execute(EffectMCCore core, Map<String, Object> args);

    public static class EffectResult {
        public final String message;
        public enum Result {
            SUCCESS,
            SKIPPED,
            UNAUTHORIZED,
            ERROR,
            UNSUPPORTED,
            UNKNOWN
        }

        public final Result result;

        public EffectResult(String message, Result result) {
            this.message = message;
            this.result = result;
        }

        public boolean isSuccess() {
            return result == Result.SUCCESS;
        }

        public boolean isError() {
            return result == Result.ERROR || result == Result.UNAUTHORIZED || result == Result.SKIPPED || result == Result.UNKNOWN || result == Result.UNSUPPORTED;
        }
    }

    protected String getPropAsString(Map<String, Object> args, String key) {
        if (args.containsKey(key))
            return getPropertyManager().getProperty(key).getAsString(args.get(key));
        else
            return String.valueOf(getPropertyManager().getProperty(key).getDefaultValue());
    }

    protected boolean getPropAsBoolean(Map<String, Object> args, String key) {
        if (args.containsKey(key))
            return getPropertyManager().getProperty(key).getAsBoolean(args.get(key));
        else
            return (boolean) getPropertyManager().getProperty(key).getDefaultValue();
    }

    protected int getPropAsInt(Map<String, Object> args, String key) {
        if (args.containsKey(key))
            return getPropertyManager().getProperty(key).getAsInt(args.get(key));
        else
            return (int) getPropertyManager().getProperty(key).getDefaultValue();
    }

    protected float getPropAsFloat(Map<String, Object> args, String key) {
        if (args.containsKey(key))
            return getPropertyManager().getProperty(key).getAsFloat(args.get(key));
        else
            return (float) getPropertyManager().getProperty(key).getDefaultValue();
    }

    protected double getPropAsDouble(Map<String, Object> args, String key) {
        if (args.containsKey(key))
            return getPropertyManager().getProperty(key).getAsDouble(args.get(key));
        else
            return (double) getPropertyManager().getProperty(key).getDefaultValue();
    }
}
