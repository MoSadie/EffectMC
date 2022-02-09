package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class StopSoundHandler extends EffectRequestHandler{

    public StopSoundHandler(EffectMCCore core) {
        super(core);
        addStringProperty("sound", "", false, "Sound", "minecraft:entity.ghast.ambient");
//        addSelectionProperty("category", "", false, "Category", PlaySoundHandler.SOUND_CATEGORY.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Stop Sound";
    }

    @Override
    public String getEffectTooltip() {
        return "Stop a specific or any sound.";
    }

    @Override
    String execute() {
        // Note for the future, to re-enable category specific sound searching update the stopSound calls as well as the commented out line in the constructor.
        if (getProperty("sound").getAsString().equalsIgnoreCase("null") || getProperty("sound").getAsString().equalsIgnoreCase(" ") || getProperty("sound").getAsString().equalsIgnoreCase("")) {
            core.getExecutor().log("Stopping all sounds");
            if (core.getExecutor().stopSound(null, null))
                return "Stopping all sounds.";
            else
                return "Failed to stop all sounds.";
        } else {
            core.getExecutor().log("Stopping specific sound: " + getProperty("sound").getAsString());
            if (core.getExecutor().stopSound(getProperty("sound").getAsString(), null))
                return "Stopping specific sound: " + getProperty("sound").getAsString();
            else
                return "Failed to stop specific sound.";
        }
    }
}
