package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class ReceiveChatMessageEffect extends Effect {

    public ReceiveChatMessageEffect() {
        super();
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("message", "", true, "Message", "Hello World!");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Receive Chat";
    }

    @Override
    public String getEffectTooltip() {
        return "Displays a message in in-game chat.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Receiving chat message: " + getPropAsString(args, "message"));
        if (core.getExecutor().receiveChatMessage(getPropAsString(args, "message")))
            return new EffectResult("Receiving chat message: " + getPropAsString(args, "message"), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to receive chat message", EffectResult.Result.ERROR);
    }

}
