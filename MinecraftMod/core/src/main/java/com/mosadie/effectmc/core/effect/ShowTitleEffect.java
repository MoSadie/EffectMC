package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class ShowTitleEffect extends Effect {

    public ShowTitleEffect() {
        super();
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("title", "", true, "Title", "Hello");
        getPropertyManager().addStringProperty("subtitle", "", true, "Subtitle", "World!");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String title = getPropAsString(args, "title");
        String subtitle = getPropAsString(args, "subtitle");

        core.getExecutor().log("Showing title: " + title + " Subtitle: " + subtitle);
        if (core.getExecutor().showTitle(title, subtitle))
            return new EffectResult("Showing title: " + title + " Subtitle: " + subtitle, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to show title", EffectResult.Result.ERROR);
    }
}
