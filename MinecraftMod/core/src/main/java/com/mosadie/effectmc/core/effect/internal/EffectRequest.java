package com.mosadie.effectmc.core.effect.internal;

import java.util.Map;

public class EffectRequest {
    private final String effectId;
    private final Map<String, Object> args;

    public EffectRequest(String effectId, Map<String, Object> args) {
        this.effectId = effectId;
        this.args = args;
    }

    public String getEffectId() {
        return effectId;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
}
