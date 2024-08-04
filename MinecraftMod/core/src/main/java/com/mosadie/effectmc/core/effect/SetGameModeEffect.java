package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetGameModeEffect extends Effect {

    public SetGameModeEffect() {
        super();
        getPropertyManager().addSelectionProperty("gamemode", GAME_MODE.SURVIVAL.name(), true, "Game Mode", GAME_MODE.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set Game Mode";
    }

    @Override
    public String getEffectId() {
        return "gamemode";
    }

    @Override
    public String getEffectTooltip() {
        return "Set your game mode.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        String gamemode = getPropAsString(args, "gamemode");
        core.getExecutor().log("Setting game mode: " + gamemode);
        if (core.getExecutor().sendChatMessage("/gamemode " + gamemode.toLowerCase()))
            return new EffectResult("Set game mode: " + gamemode, EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set game mode", EffectResult.Result.ERROR);
    }

    public enum GAME_MODE {
        SURVIVAL,
        CREATIVE,
        ADVENTURE,
        SPECTATOR;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (GAME_MODE type : GAME_MODE.values()) {
                list.add(type.name());
            }
            return list.toArray(new String[0]);
        }

        public static GAME_MODE fromString(String name) {
            for (GAME_MODE gameMode : GAME_MODE.values()) {
                if (name.equalsIgnoreCase(gameMode.name())) {
                    return gameMode;
                }
            }

            return GAME_MODE.SURVIVAL;
        }
    }
}
