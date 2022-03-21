package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ShowToastHandler extends EffectRequestHandler {

    public ShowToastHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("title", "", true, "Title", "Hello");
        addStringProperty("subtitle", "", true, "Subtitle", "World!");
        addCommentProperty("For a blank subtitle, use a single space.");
    }

    @Override
    public String getEffectName() {
        return "Show Toast";
    }

    @Override
    public String getEffectTooltip() {
        return "Show a toast on screen with a custom message.";
    }

    @Override
    String execute() {
        core.getExecutor().log("Showing toast with title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString());
        if (core.getExecutor().showToast(getProperty("title").getAsString(), getProperty("subtitle").getAsString()))
            return "Showing toast with title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString();
        else
            return "Failed to show toast.";

    }
}
