package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class RejoinEffect extends Effect {
    @Override
    public String getEffectName() {
        return "Rejoin";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnects and rejoins the current server/world.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        switch (core.getExecutor().getWorldState()) {
            case MULTIPLAYER:
                String server = core.getExecutor().getServerIP();
                if (core.getExecutor().joinServer(server))
                    return new EffectResult("Rejoining server...", EffectResult.Result.SUCCESS);
                else
                    return new EffectResult("Something went wrong rejoining server.", EffectResult.Result.ERROR);

            case SINGLEPLAYER:
                String world = core.getExecutor().getSPWorldName();
                if (core.getExecutor().loadWorld(world))
                    return new EffectResult("Rejoining world...", EffectResult.Result.SUCCESS);
                else
                    return new EffectResult("Something went wrong rejoining world.", EffectResult.Result.ERROR);

            default:
                return new EffectResult("Not currently in a world/server!", EffectResult.Result.ERROR);
        }
    }
}
