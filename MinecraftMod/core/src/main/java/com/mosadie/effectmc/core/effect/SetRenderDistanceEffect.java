package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class SetRenderDistanceEffect extends Effect {

    public SetRenderDistanceEffect() {
        super();
        getPropertyManager().addIntegerProperty("chunks", 12, true, "Chunks", "12");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set Render Distance";
    }

    @Override
    public String getEffectId() {
        return "renderdistance";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the Render Distance.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        int chunks = getPropAsInt(args, "chunks");

        core.getExecutor().log("Setting Render Distance: " + chunks);
        if (core.getExecutor().setRenderDistance(chunks))
            return new EffectResult("Set Render Distance: " + chunks, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set Render Distance", EffectResult.Result.ERROR);
    }
}
