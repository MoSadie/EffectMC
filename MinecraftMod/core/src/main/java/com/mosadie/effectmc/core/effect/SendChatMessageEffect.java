package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class SendChatMessageEffect extends Effect {

    public SendChatMessageEffect() {
        super();
        getPropertyManager().addStringProperty("message", "", true, "Message", "Hello World!");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Send Chat";
    }

    @Override
    public String getEffectTooltip() {
        return "Send a chat message or run a command.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String message = getPropAsString(args, "message");

        if (message.toLowerCase().startsWith("/effectmc ")) {
            return new EffectResult("Cannot trigger EffectMC commands.", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Sending chat message: " + message);
        if (core.getExecutor().sendChatMessage(message))
            return new EffectResult("Sending chat message " + message, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to send chat message.", EffectResult.Result.ERROR);
    }
}
