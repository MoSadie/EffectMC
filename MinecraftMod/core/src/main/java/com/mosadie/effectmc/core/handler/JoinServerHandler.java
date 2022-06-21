package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class JoinServerHandler extends EffectRequestHandler {

    public JoinServerHandler(EffectMCCore core) {
        super(core);
        addStringProperty("serverip", "", true, "Server IP", "localhost:25565");
    }

    @Override
    public String getEffectName() {
        return "Join Server";
    }

    @Override
    public String getEffectTooltip() {
        return "Automatically attempts to join the provided Minecraft server.";
    }

    @Override
    EffectResult execute() {
        if (getProperty("serverip") != null) {
            core.getExecutor().log("Joining Server");
            if (core.getExecutor().joinServer(getProperty("serverip").getAsString()))
                return new EffectResult("Joining Server", true);
            else
                return new EffectResult("Failed to join server.", false);
        }

        return new EffectResult("Something went wrong.", false);
    }
}
