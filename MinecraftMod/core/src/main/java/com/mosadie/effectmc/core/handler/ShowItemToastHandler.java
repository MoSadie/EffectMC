package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ShowItemToastHandler extends EffectRequestHandler {

    public ShowItemToastHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("You can get item data by holding an item and running /effectmc exportitem");
        addStringProperty("item", "", true, "Item Data", "{}");
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("title", "", true, "Title", "Hello");
        addStringProperty("subtitle", "", true, "Subtitle", "World!");
        addCommentProperty("For a blank subtitle, use a single space.");
    }

    @Override
    public String getEffectName() {
        return "Show Item Toast";
    }

    @Override
    public String getEffectTooltip() {
        return "Show a toast on screen with a custom message and item.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Showing item toast with data: " + getProperty("item").getAsString() + " title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString());
        if (core.getExecutor().showItemToast(getProperty("item").getAsString(), getProperty("title").getAsString(), getProperty("subtitle").getAsString()))
            return new EffectResult("Showing item toast with data: " + getProperty("item").getAsString() + " title: " + getProperty("title").getAsString() + " Subtitle: " + getProperty("subtitle").getAsString(), true);
        else
            return new EffectResult("Failed to show toast.", false);

    }
}
