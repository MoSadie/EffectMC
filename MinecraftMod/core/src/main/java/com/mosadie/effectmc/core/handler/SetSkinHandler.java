package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class PressInputHandler extends EffectRequestHandler {

    private final EffectMCCore core;

    public PressInputHandler(EffectMCCore core) {
        super(core);
        addStringProperty("key", "", true, "Key", "");
        addIntegerProperty("holdtime", 0, true, "Hold Duration", "100");
        this.core = core;
    }

    @Override
    public String getEffectName() {
        return "Press Input";
    }

    @Override
    public String getEffectTooltip() {
        return "Press any control.";
    }

    @Override
    String execute() {
        if (getProperty("key") != null) {
            core.getExecutor().log("Attempting to press key");
            if (core.getExecutor().pressInput(getProperty("key").getAsString(), getProperty("holdtime").getAsInt()))
                return "Pressed key";
            else
                return "Failed to press key.";
        }

        return "Something went wrong.";
    }

}
