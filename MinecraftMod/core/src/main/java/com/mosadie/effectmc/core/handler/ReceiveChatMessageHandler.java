package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ReceiveChatMessageHandler extends EffectRequestHandler {

    public ReceiveChatMessageHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("message", "", true, "Message", "Hello World!");
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
    EffectResult execute() {
        core.getExecutor().log("Receiving chat message: " + getProperty("message").getAsString());
        if (core.getExecutor().receiveChatMessage(getProperty("message").getAsString()))
            return new EffectResult("Receiving chat message: " + getProperty("message").getAsString(), true);
        else
            return new EffectResult("Failed to receive chat message", false);
    }

}
