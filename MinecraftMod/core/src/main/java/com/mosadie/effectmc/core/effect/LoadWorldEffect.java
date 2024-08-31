package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class LoadWorldEffect extends Effect {

    public LoadWorldEffect() {
        super();
        getPropertyManager().addStringProperty("world", "", true, "World", "New World");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Load World";
    }

    @Override
    public String getEffectTooltip() {
        return "Load the specified world.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Loading world");
        if (core.getExecutor().loadWorld(getPropAsString(args, "world")))
            return new EffectResult("Loading world " + getPropAsString(args, "world"), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to load world, check if using the folder name of the world.", EffectResult.Result.ERROR);
    }
}
