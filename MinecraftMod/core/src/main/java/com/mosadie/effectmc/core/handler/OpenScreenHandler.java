package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;


public class OpenScreenHandler extends EffectRequestHandler {


    public OpenScreenHandler(EffectMCCore core) {
        super(core);
        addSelectionProperty("screen", SCREEN.MAIN_MENU.toString(), true, "Screen", SCREEN.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Open Screen";
    }

    @Override
    public String getEffectSlug() {
        return "openscreen";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnect from server/world and show a set screen.";
    }

    @Override
    public EffectResult execute() {
        SCREEN screen = SCREEN.getFromName(getProperty("screen").getAsString());

        if (screen == null) {
            core.getExecutor().log("Screen invalid");
            return new EffectResult("Screen Invalid", false);
        }

        core.getExecutor().log("Opening Screen");
        if (core.getExecutor().openScreen(screen)) {
            return new EffectResult("Opened Screen.", true);
        } else {
            return new EffectResult("Failed to open screen.", false);
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
                return OpenScreenHandler.SCREEN.valueOf(name.toUpperCase());
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
