package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class JoinServerEffect extends Effect {

    public JoinServerEffect() {
        super();
        getPropertyManager().addStringProperty("serverip", "", true, "Server IP", "localhost:25565");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        if (args.containsKey("serverip")) {
            core.getExecutor().log("Joining Server");
            if (core.getExecutor().joinServer(getPropAsString(args, "serverip")))
                return new EffectResult("Joining Server", EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to join server.", EffectResult.Result.ERROR);
        }

        return new EffectResult("Something went wrong.", EffectResult.Result.ERROR);
    }
}
