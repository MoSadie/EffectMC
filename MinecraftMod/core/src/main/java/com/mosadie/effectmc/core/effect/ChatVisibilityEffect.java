package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatVisibilityEffect extends Effect {

    public ChatVisibilityEffect() {
        super();
        getPropertyManager().addSelectionProperty("visibility", VISIBILITY.SHOW.name(), true, "Visibility", VISIBILITY.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Show/Hide Chat";
    }

    @Override
    public String getEffectId() {
        return "showchat";
    }

    @Override
    public String getEffectTooltip() {
        return "Show/Hide chat.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }
        core.getExecutor().log("Setting chat visibility: " + getPropAsString(args, "visibility"));
        if (core.getExecutor().setChatVisibility(VISIBILITY.fromString(getPropAsString(args, "visibility"))))
            return new EffectResult("Set chat visibility: " + getPropAsString(args, "visibility"), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set chat visibility", EffectResult.Result.ERROR);
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
