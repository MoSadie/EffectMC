package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class ShowItemToastEffect extends Effect {

    public ShowItemToastEffect() {
        super();
        getPropertyManager().addCommentProperty("You can get item data by holding an item and running /effectmc exportitem");
        getPropertyManager().addStringProperty("item", "", true, "Item Data", "{}");
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("title", "", true, "Title", "Hello");
        getPropertyManager().addStringProperty("subtitle", "", true, "Subtitle", "World!");
        getPropertyManager().addCommentProperty("For a blank subtitle, use a single space.");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String title = getPropAsString(args, "title");
        String subtitle = getPropAsString(args, "subtitle");
        String item = getPropAsString(args, "item");

        core.getExecutor().log("Showing item toast with data: " + item + " title: " + title + " Subtitle: " + subtitle);
        if (core.getExecutor().showItemToast(item, title, subtitle))
            return new EffectResult("Showing item toast with data: " + item + " title: " + title + " Subtitle: " + subtitle, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to show toast.", EffectResult.Result.ERROR);

    }
}
