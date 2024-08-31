package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class SetGUIScaleEffect extends Effect {

    public SetGUIScaleEffect() {
        super();
        getPropertyManager().addIntegerProperty("scale", 1, true, "Scale", "1");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set GUI Scale";
    }

    @Override
    public String getEffectId() {
        return "guiscale";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the GUI Scale.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        int scale = getPropAsInt(args, "scale");

        core.getExecutor().log("Setting GUI Scale: " + scale);
        if (core.getExecutor().setGuiScale(scale))
            return new EffectResult("Set GUI Scale: " + scale, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set GUI Scale", EffectResult.Result.ERROR);
    }
}
