package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class LoadWorldHandler extends EffectRequestHandler {

    public LoadWorldHandler(EffectMCCore core) {
        super(core);
        addStringProperty("world", "", true, "World", "New World");
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
    String execute() {
        core.getExecutor().log("Loading world");
        if (core.getExecutor().loadWorld(getProperty("world").getAsString()))
            return "Loading world " + getProperty("world").getAsString();
        else
            return "Failed to load world, check if using the folder name of the world.";
    }
}
