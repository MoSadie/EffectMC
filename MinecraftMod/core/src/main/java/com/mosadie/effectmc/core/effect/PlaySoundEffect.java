package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaySoundEffect extends Effect {
    public PlaySoundEffect() {
        getPropertyManager().addStringProperty("sound", "minecraft:entity.ghast.ambient", true, "Sound", "minecraft:entity.ghast.ambient");
        getPropertyManager().addSelectionProperty("category", SOUND_CATEGORY.MASTER.toString(), true, "Category", SOUND_CATEGORY.toStringArray());
        getPropertyManager().addFloatProperty("volume", 1.0f, true, "Volume", 0.0f, 1.0f);
        getPropertyManager().addFloatProperty("pitch", 1.0f, true, "Pitch", 0.0f, 2.0f);
        getPropertyManager().addBooleanProperty("repeat", false, true, "Repeat", "Enabled", "Disabled");
        getPropertyManager().addIntegerProperty("repeatDelay", 0, false, "Repeat Delay", "0");
        getPropertyManager().addSelectionProperty("attenuationType", ATTENUATION_TYPE.NONE.toString(), true, "Attenuation Type", ATTENUATION_TYPE.toStringArray());
        getPropertyManager().addIntegerProperty("x", 0, true, "X", "0");
        getPropertyManager().addIntegerProperty("y", 0, true, "Y", "0");
        getPropertyManager().addIntegerProperty("z", 0, true, "Z", "0");
        getPropertyManager().addBooleanProperty("relative", false, true, "Relative", "Enabled", "Disabled");
        getPropertyManager().addBooleanProperty("global", false, true, "Global", "Enabled", "Disabled");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Play Sound";
    }

    @Override
    public String getEffectTooltip() {
        return "Play any sound.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        core.getExecutor().log("Play sound: " + getPropAsString(args, "sound"));
        if (core.getExecutor().playSound(getPropAsString(args, "sound"),
                getPropAsString(args, "category"),
                getPropAsFloat(args, "volume"),
                getPropAsFloat(args, "pitch"),
                getPropAsBoolean(args, "repeat"),
                getPropAsInt(args, "repeatDelay"),
                getPropAsString(args, "attenuationType"),
                getPropAsDouble(args, "x"),
                getPropAsDouble(args, "y"),
                getPropAsDouble(args, "z"),
                getPropAsBoolean(args,"relative"),
                getPropAsBoolean(args,"global")))
            return new EffectResult("Played sound " + getPropAsString(args, "sound"), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to play sound.", EffectResult.Result.ERROR);
    }

    public enum SOUND_CATEGORY {
        MASTER,
        MUSIC,
        RECORD,
        WEATHER,
        BLOCK,
        HOSTILE,
        NEUTRAL,
        PLAYER,
        AMBIENT,
        VOICE;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (SOUND_CATEGORY category : SOUND_CATEGORY.values()) {
                list.add(category.name());
            }
            return list.toArray(new String[0]);
        }
    }

    public enum ATTENUATION_TYPE {
        NONE,
        LINEAR;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (ATTENUATION_TYPE type : ATTENUATION_TYPE.values()) {
                list.add(type.name());
            }
            return list.toArray(new String[0]);
        }
    }
}