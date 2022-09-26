package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class SetGameModeHandler extends EffectRequestHandler {

    public SetGameModeHandler(EffectMCCore core) {
        super(core);
        addSelectionProperty("gamemode", GAME_MODE.SURVIVAL.name(), true, "Game Mode", GAME_MODE.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Set Game Mode";
    }

    @Override
    public String getEffectSlug() {
        return "gamemode";
    }

    @Override
    public String getEffectTooltip() {
        return "Set your game mode.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting game mode: " + getProperty("gamemode").getAsString());
        if (core.getExecutor().sendChatMessage("/gamemode " + getProperty("gamemode").getAsString().toLowerCase()))
            return new EffectResult("Set game mode: " + getProperty("gamemode").getAsString(), true);
        else
            return new EffectResult("Failed to set game mode", false);
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
