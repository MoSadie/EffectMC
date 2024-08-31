package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class SetFovEffect extends Effect {

    public SetFovEffect() {
        super();
        getPropertyManager().addIntegerProperty("fov", 60, true, "FOV", "60");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set FOV";
    }

    @Override
    public String getEffectId() {
        return "fov";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the field of view.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        int fov = getPropAsInt(args, "fov");
        core.getExecutor().log("Setting FOV: " + fov);
        if (core.getExecutor().setFOV(fov))
            return new EffectResult("Set FOV: " + fov, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set FOV", EffectResult.Result.ERROR);
    }
}
