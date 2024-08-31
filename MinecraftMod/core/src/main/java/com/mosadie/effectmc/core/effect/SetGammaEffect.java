package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class SetGammaEffect extends Effect {

    public SetGammaEffect() {
        super();
        getPropertyManager().addFloatProperty("gamma", 0.5f, true, "Gamma", 0.0f, 15.0f);
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set Gamma";
    }

    @Override
    public String getEffectId() {
        return "gamma";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the brightness/gamma.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        double gamma = getPropAsDouble(args, "gamma");

        core.getExecutor().log("Setting Gamma: " + gamma);
        if (core.getExecutor().setGamma(gamma))
            return new EffectResult("Set Gamma: " + gamma, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set Gamma", EffectResult.Result.ERROR);
    }
}
