package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class ReceiveChatMessageHandler extends EffectRequestHandler {

    public ReceiveChatMessageHandler(EffectMCCore core) {
        super(core);
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
    String execute() {
        core.getExecutor().log("Receiving chat message: " + getProperty("message").getAsString());
        if (core.getExecutor().receiveChatMessage(getProperty("message").getAsString()))
            return "Receiving chat message: " + getProperty("message").getAsString();
        else
            return "Failed to receive chat message";
    }

}
