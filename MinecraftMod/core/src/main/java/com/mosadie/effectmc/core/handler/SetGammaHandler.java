package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class SetGammaHandler extends EffectRequestHandler {

    public SetGammaHandler(EffectMCCore core) {
        super(core);
        addFloatProperty("gamma", 0.5f, true, "Gamma", 0.0f, 15.0f);
    }

    @Override
    public String getEffectName() {
        return "Set Gamma";
    }

    @Override
    public String getEffectSlug() {
        return "gamma";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the brightness/gamma.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting Gamma: " + getProperty("gamma").getAsString());
        if (core.getExecutor().setGamma(getProperty("gamma").getAsDouble()))
            return new EffectResult("Set Gamma: " + getProperty("gamma").getAsString(), true);
        else
            return new EffectResult("Failed to set Gamma", false);
    }
}
