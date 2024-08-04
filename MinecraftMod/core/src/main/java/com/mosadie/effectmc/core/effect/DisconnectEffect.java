package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DisconnectEffect extends Effect {


    public DisconnectEffect() {
        super();
        getPropertyManager().addCommentProperty("Set color using &sect; color codes.");
        getPropertyManager().addStringProperty("title", "", true, "Title", "Title");
        getPropertyManager().addStringProperty("message", "", true, "Message", "Message");
        getPropertyManager().addCommentProperty("For a blank line, use a single space.");
        getPropertyManager().addSelectionProperty("nextscreen", NEXT_SCREEN.MAIN_MENU.name(), true, "Next Screen", NEXT_SCREEN.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Disconnect";
    }

    @Override
    public String getEffectId() {
        return "triggerdisconnect";
    }

    @Override
    public String getEffectTooltip() {
        return "Disconnect from server/world and show a custom disconnect screen.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        NEXT_SCREEN nextScreen = NEXT_SCREEN.getFromName(getPropAsString(args, "nextscreen"));

        if (nextScreen == null) {
            core.getExecutor().log("Next Screen invalid");
            return new EffectResult("Next Screen Invalid", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Triggering Disconnect");
        if (core.getExecutor().triggerDisconnect(nextScreen, getPropAsString(args, "title"), getPropAsString(args, "message"))) {
            return new EffectResult("Disconnected from world.", EffectResult.Result.SUCCESS);
        } else {
            return new EffectResult("Failed to disconnect.", EffectResult.Result.ERROR);
        }
    }

    public enum NEXT_SCREEN {
        MAIN_MENU,
        SERVER_SELECT,
        WORLD_SELECT;

        public static DisconnectEffect.NEXT_SCREEN getFromName(String name) {
            try {
                return DisconnectEffect.NEXT_SCREEN.valueOf(name.toUpperCase());
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
