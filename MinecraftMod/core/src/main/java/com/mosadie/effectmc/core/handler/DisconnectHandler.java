package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;


public class DisconnectHandler extends EffectRequestHandler {


    public DisconnectHandler(EffectMCCore core) {
        super(core);
        addCommentProperty("Set color using &sect; color codes.");
        addStringProperty("title", "", true, "Title", "Title");
        addStringProperty("message", "", true, "Message", "Message");
        addCommentProperty("For a blank line, use a single space.");
        addSelectionProperty("nextscreen", NEXT_SCREEN.MAIN_MENU.toString(), true, "Next Screen", NEXT_SCREEN.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Disconnect";
    }

    @Override
    public String getEffectSlug() {
        return "triggerdisconnect";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnect from server/world and show a custom disconnect screen.";
    }

    @Override
    public String execute() {
        NEXT_SCREEN nextScreen = NEXT_SCREEN.getFromName(getProperty("nextscreen").getAsString());

        if (nextScreen == null) {
            core.getExecutor().log("Next Screen invalid");
            return "Next Screen Invalid";
        }

        core.getExecutor().log("Triggering Disconnect");
        if (core.getExecutor().triggerDisconnect(nextScreen, getProperty("title").getAsString(), getProperty("message").getAsString())) {
            return "Disconnected from world.";
        } else {
            return "Failed to disconnect.";
        }
    }

    public enum NEXT_SCREEN {
        MAIN_MENU,
        SERVER_SELECT,
        WORLD_SELECT;

        public static DisconnectHandler.NEXT_SCREEN getFromName(String name) {
            try {
                return DisconnectHandler.NEXT_SCREEN.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (NEXT_SCREEN screen : NEXT_SCREEN.values()) {
                list.add(screen.name());
            }
            return list.toArray(new String[0]);
        }
    }
}
