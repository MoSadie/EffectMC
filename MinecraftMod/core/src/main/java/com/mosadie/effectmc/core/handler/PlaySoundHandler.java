package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class PlaySoundHandler extends EffectRequestHandler {
    public PlaySoundHandler(EffectMCCore core) {
        super(core);
        addStringProperty("sound", "minecraft:entity.ghast.ambient", true, "Sound", "minecraft:entity.ghast.ambient");
        addSelectionProperty("category", SOUND_CATEGORY.MASTER.toString(), true, "Category", SOUND_CATEGORY.toStringArray());
        addFloatProperty("volume", 1.0f, true, "Volume", 0.0f, 1.0f);
        addFloatProperty("pitch", 1.0f, true, "Pitch", 0.0f, 2.0f);
        addBooleanProperty("repeat", false, true, "Repeat", "Enabled", "Disabled");
        addIntegerProperty("repeatDelay", 0, false, "Repeat Delay", "0");
        addSelectionProperty("attenuationType", ATTENUATION_TYPE.NONE.toString(), true, "Attenuation Type", ATTENUATION_TYPE.toStringArray());
        addIntegerProperty("x", 0, true, "X", "0");
        addIntegerProperty("y", 0, true, "Y", "0");
        addIntegerProperty("z", 0, true, "Z", "0");
        addBooleanProperty("relative", false, true, "Relative", "Enabled", "Disabled");
        addBooleanProperty("global", false, true, "Global", "Enabled", "Disabled");
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
    EffectResult execute() {
        core.getExecutor().log("Play sound: " + getProperty("sound").getAsString());
        if (core.getExecutor().playSound(getProperty("sound").getAsString(),
                getProperty("category").getAsString(), getProperty("volume").getAsFloat(),
                getProperty("pitch").getAsFloat(),
                getProperty("repeat").getAsBoolean(),
                getProperty("repeatDelay").getAsInt(),
                getProperty("attenuationType").getAsString(),
                getProperty("x").getAsDouble(),
                getProperty("y").getAsDouble(),
                getProperty("z").getAsDouble(),
                getProperty("relative").getAsBoolean(),
                getProperty("global").getAsBoolean()))
            return new EffectResult("Played sound " + getProperty("sound").getAsString(), true);
        else
            return new EffectResult("Failed to play sound.", false);
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