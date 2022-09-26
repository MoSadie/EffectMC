package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ShowTitleHandler extends EffectRequestHandler {

    public ShowTitleHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("title", "", true, "Title", "Hello");
        addStringProperty("subtitle", "", true, "Subtitle", "World!");
        addCommentProperty("For a blank title/subtitle, use a single space.");
    }

    @Override
    public String getEffectName() {
        return "Show Title";
    }

    @Override
    public String getEffectTooltip() {
        return "Show a title/subtitle as if using the /title command.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Showing title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString());
        if (core.getExecutor().showTitle(getProperty("title").getAsString(), getProperty("subtitle").getAsString()))
            return new EffectResult("Showing title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString(), true);
        else
            return new EffectResult("Failed to show title", false);
    }
}
