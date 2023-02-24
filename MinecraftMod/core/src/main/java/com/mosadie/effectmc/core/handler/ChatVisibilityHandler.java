package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class ChatVisibilityHandler extends EffectRequestHandler {

    public ChatVisibilityHandler(EffectMCCore core) {
        super(core);
        addSelectionProperty("visibility", VISIBILITY.SHOW.name(), true, "Visibility", VISIBILITY.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Show/Hide Chat";
    }

    @Override
    public String getEffectSlug() {
        return "showchat";
    }

    @Override
    public String getEffectTooltip() {
        return "Show/Hide chat.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting chat visibility: " + getProperty("visibility").getAsString());
        if (core.getExecutor().setChatVisibility(VISIBILITY.fromString(getProperty("visibility").getAsString())))
            return new EffectResult("Set chat visibility: " + getProperty("visibility").getAsString(), true);
        else
            return new EffectResult("Failed to set chat visibility", false);
    }

    public enum VISIBILITY {
        SHOW,
        COMMANDS_ONLY,
        HIDE;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (VISIBILITY type : VISIBILITY.values()) {
                list.add(type.name());
            }
            return list.toArray(new String[0]);
        }

        public static VISIBILITY fromString(String name) {
            for (VISIBILITY visibility : VISIBILITY.values()) {
                if (name.equalsIgnoreCase(visibility.name())) {
                    return visibility;
                }
            }

            return VISIBILITY.SHOW;
        }
    }
}
