package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;

import java.util.Map;

public class EffectHandler {
    private final EffectMCCore core;
    private final Map<String, Effect> effects;

    private boolean exportFlag = false;
    private boolean trustFlag = false;

    public EffectHandler(EffectMCCore core, Map<String, Effect> effects) {
        this.core = core;
        this.effects = effects;
    }

    public void setExportFlag() {
        exportFlag = true;
    }

    public void setTrustFlag() {
        trustFlag = true;
    }

    public Effect.EffectResult handleRequest(Device device, EffectRequest request) {
        // Check for null device or request
        if (device == null) {
            return new Effect.EffectResult("Device is null", Effect.EffectResult.Result.ERROR);
        } else if (request == null) {
            return new Effect.EffectResult("Request is null", Effect.EffectResult.Result.ERROR);
        }

        // Get the effect from the map
        Effect effect = effects.get(request.getEffectId());
        if (effect == null) {
            return new Effect.EffectResult("Effect not found", Effect.EffectResult.Result.UNKNOWN);
        }

        // If the trust flag, begin the device trust process
        if (trustFlag) {
            trustFlag = false;
            // Prompt to trust the device
            core.getExecutor().showTrustPrompt(device);
            return new Effect.EffectResult("Showing trust prompt", Effect.EffectResult.Result.SKIPPED);
        }

        // Validate device is trusted
        if (!core.checkTrust(device)) {
            core.getExecutor().log("Device is not trusted");
            return new Effect.EffectResult("Device is not trusted", Effect.EffectResult.Result.UNAUTHORIZED);
        }

        // Validate request contains all required properties
        if (!effect.getPropertyManager().argumentCheck(request.getArgs())) {
            core.getExecutor().log("A required property is missing or invalid.");
            return new Effect.EffectResult("A required property is missing or invalid.", Effect.EffectResult.Result.ERROR);
        }

        // If there is an export flag set, export the effect request as a json string in the log
        if (exportFlag) {
            exportFlag = false;
            core.getExecutor().log("Exported Effect Request: " + core.toJson(request));
        }

        // Execute the effect
        return effect.execute(core, request.getArgs());
    }
}
