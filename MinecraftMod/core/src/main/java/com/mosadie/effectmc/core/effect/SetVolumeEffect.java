package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetVolumeEffect extends Effect {

    private final static int MAX_VOLUME = 100;
    private final static int MIN_VOLUME = 0;

    public SetVolumeEffect() {
        super();
        getPropertyManager().addIntegerProperty("volume", 100, true, "Volume", "100");
        getPropertyManager().addCommentProperty("Use whole numbers between 0 and 100.");
        getPropertyManager().addSelectionProperty("category", VOLUME_CATEGORIES.MASTER.toString(), true, "Category", VOLUME_CATEGORIES.toStringArray());
        getPropertyManager().lock();
    }
    @Override
    public String getEffectName() {
        return "Set Volume";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the volume of a specific category.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        int volume = getPropAsInt(args, "volume");
        VOLUME_CATEGORIES category = VOLUME_CATEGORIES.getFromName(getPropAsString(args, "category"));

        if (category == null) {
            return new EffectResult("Invalid category", EffectResult.Result.ERROR);
        }

        volume = Math.min(MAX_VOLUME, Math.max(MIN_VOLUME, volume));

        core.getExecutor().setVolume(category, volume);
        return new EffectResult("Set volume of " + category + " to " + volume, EffectResult.Result.SUCCESS);
    }

    public enum VOLUME_CATEGORIES {
        MASTER,
        MUSIC,
        RECORDS,
        WEATHER,
        BLOCKS,
        HOSTILE,
        NEUTRAL,
        PLAYERS,
        AMBIENT,
        VOICE;

        public static VOLUME_CATEGORIES getFromName(String name) {
            try {
                return VOLUME_CATEGORIES.valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (VOLUME_CATEGORIES category : VOLUME_CATEGORIES.values()) {
                list.add(category.name());
            }
            return list.toArray(new String[0]);
        }
    }
}
