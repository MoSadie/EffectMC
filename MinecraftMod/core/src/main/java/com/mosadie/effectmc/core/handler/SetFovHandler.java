package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class SetFovHandler extends EffectRequestHandler {

    public SetFovHandler(EffectMCCore core) {
        super(core);
        addIntegerProperty("fov", 30, true, "FOV", "60");
    }

    @Override
    public String getEffectName() {
        return "Set FOV";
    }

    @Override
    public String getEffectSlug() {
        return "fov";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the field of view.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting FOV: " + getProperty("fov").getAsString());
        if (core.getExecutor().setFOV(getProperty("fov").getAsInt()))
            return new EffectResult("Set FOV: " + getProperty("fov").getAsString(), true);
        else
            return new EffectResult("Failed to set FOV", false);
    }
}
