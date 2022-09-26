package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class SetGUIScaleHandler extends EffectRequestHandler {

    public SetGUIScaleHandler(EffectMCCore core) {
        super(core);
        addIntegerProperty("scale", 1, true, "Scale", "1");
    }

    @Override
    public String getEffectName() {
        return "Set GUI Scale";
    }

    @Override
    public String getEffectSlug() {
        return "guiscale";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the GUI Scale.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting GUI Scale: " + getProperty("scale").getAsString());
        if (core.getExecutor().setGuiScale(getProperty("scale").getAsInt()))
            return new EffectResult("Set GUI Scale: " + getProperty("scale").getAsString(), true);
        else
            return new EffectResult("Failed to set GUI Scale", false);
    }
}
