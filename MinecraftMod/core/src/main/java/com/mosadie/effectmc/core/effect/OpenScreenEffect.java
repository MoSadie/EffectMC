package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OpenScreenEffect extends Effect {


    public OpenScreenEffect() {
        super();
        getPropertyManager().addSelectionProperty("screen", SCREEN.MAIN_MENU.toString(), true, "Screen", SCREEN.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Open Screen";
    }

    @Override
    public String getEffectId() {
        return "openscreen";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnect from server/world and show a set screen.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        SCREEN screen = SCREEN.getFromName(getPropAsString(args, "screen"));

        if (screen == null) {
            core.getExecutor().log("Screen invalid");
            return new EffectResult("Screen Invalid", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Opening Screen");
        if (core.getExecutor().openScreen(screen)) {
            return new EffectResult("Opened Screen.", EffectResult.Result.SUCCESS);
        } else {
            return new EffectResult("Failed to open screen.", EffectResult.Result.ERROR);
        }
    }

    public enum SCREEN {
        MAIN_MENU,
        SERVER_SELECT,
        SERVER_DIRECT_CONNECT,
        WORLD_SELECT,
        WORLD_CREATE;

        public static SCREEN getFromName(String name) {
            try {
                return OpenScreenEffect.SCREEN.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (SCREEN screen : SCREEN.values()) {
                list.add(screen.name());
            }
            return list.toArray(new String[0]);
        }
    }
}
