package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class ShowActionMessageEffect extends Effect {


    public ShowActionMessageEffect() {
        super();
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("message", "", true, "Message", "Hello World!");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String message = getPropAsString(args, "message");

        core.getExecutor().log("Showing action bar message: " + message);
        if (core.getExecutor().showActionMessage(message))
            return new EffectResult("Showing action bar message: " + message, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to show action bar message.", EffectResult.Result.ERROR);
    }
}
