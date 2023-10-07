package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class RejoinHandler extends EffectRequestHandler {
    public RejoinHandler(EffectMCCore core) {
        super(core);
    }

    @Override
    public String getEffectName() {
        return "Rejoin";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnects and rejoins the current server/world.";
    }

    @Override
    EffectResult execute() {
        switch (core.getExecutor().getWorldState()) {
            case MULTIPLAYER:
                String server = core.getExecutor().getServerIP();
                if (core.getExecutor().joinServer(server))
                    return new EffectResult("Rejoining server...", true);
                else
                    return new EffectResult("Something went wrong rejoining server.", false);

            case SINGLEPLAYER:
                String world = core.getExecutor().getSPWorldName();
                if (core.getExecutor().loadWorld(world))
                    return new EffectResult("Rejoining world...", true);
                else
                    return new EffectResult("Something went wrong rejoining world.", false);

            default:
                return new EffectResult("Not currently in a world/server!", false);
        }
    }
}
