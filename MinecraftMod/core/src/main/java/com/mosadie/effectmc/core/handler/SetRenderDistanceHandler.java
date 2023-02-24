package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class SetRenderDistanceHandler extends EffectRequestHandler {

    public SetRenderDistanceHandler(EffectMCCore core) {
        super(core);
        addIntegerProperty("chunks", 12, true, "Chunks", "12");
    }

    @Override
    public String getEffectName() {
        return "Set Render Distance";
    }

    @Override
    public String getEffectSlug() {
        return "renderdistance";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the Render Distance.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting Render Distance: " + getProperty("chunks").getAsString());
        if (core.getExecutor().setRenderDistance(getProperty("chunks").getAsInt()))
            return new EffectResult("Set Render Distance: " + getProperty("chunks").getAsString(), true);
        else
            return new EffectResult("Failed to set Render Distance", false);
    }
}
