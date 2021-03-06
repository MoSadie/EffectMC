package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class SendChatMessageHandler extends EffectRequestHandler {

    public SendChatMessageHandler(EffectMCCore core) {
        super(core);
        addStringProperty("message", "", true, "Message", "Hello World!");
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
    EffectResult execute() {
        core.getExecutor().log("Sending chat message: " + getProperty("message").getAsString());
        if (core.getExecutor().sendChatMessage(getProperty("message").getAsString()))
            return new EffectResult("Sending chat message " + getProperty("message").getAsString(), true);
        else
            return new EffectResult("Failed to send chat message.", false);
    }
}
