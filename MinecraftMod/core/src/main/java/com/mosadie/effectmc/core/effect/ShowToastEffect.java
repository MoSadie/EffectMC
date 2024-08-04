package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class ShowToastEffect extends Effect {

    public ShowToastEffect() {
        super();
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("title", "", true, "Title", "Hello");
        getPropertyManager().addStringProperty("subtitle", "", true, "Subtitle", "World!");
        getPropertyManager().addCommentProperty("For a blank subtitle, use a single space.");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String title = getPropAsString(args, "title");
        String subtitle = getPropAsString(args, "subtitle");

        core.getExecutor().log("Showing toast with title: " + title + " Subtitle: " + subtitle);
        if (core.getExecutor().showToast(title, subtitle))
            return new EffectResult("Showing toast with title: " + title + " Subtitle: " + subtitle, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to show toast.", EffectResult.Result.ERROR);

    }
}
