package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ShowActionMessageHandler extends EffectRequestHandler {


    public ShowActionMessageHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("message", "", true, "Message", "Hello World!");
    }

    @Override
    public String getEffectName() {
        return "Show Action Message";
    }

    @Override
    public String getEffectTooltip() {
        return "Show a message on the action bar.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Showing action bar message: " + getProperty("message").getAsString());
        if (core.getExecutor().showActionMessage(getProperty("message").getAsString()))
            return new EffectResult("Showing action bar message: " + getProperty("message").getAsString(), true);
        else
            return new EffectResult("Failed to show action bar message.", false);
    }
}
