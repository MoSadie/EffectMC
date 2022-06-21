package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class StopSoundHandler extends EffectRequestHandler{

    public StopSoundHandler(EffectMCCore core) {
        super(core);
        addStringProperty("sound", "minecraft:entity.ghast.ambient", false, "Sound", "Sound ID or 'all' to stop all sounds");
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
    EffectResult execute() {
        // Note for the future, to re-enable category specific sound searching update the stopSound calls as well as the commented out line in the constructor.
        if (getProperty("sound").getAsString().equalsIgnoreCase("null") || getProperty("sound").getAsString().equalsIgnoreCase("all") || getProperty("sound").getAsString().equalsIgnoreCase("")) {
            core.getExecutor().log("Stopping all sounds");
            if (core.getExecutor().stopSound(null, null))
                return new EffectResult("Stopping all sounds.", true);
            else
                return new EffectResult("Failed to stop all sounds.", false);
        } else {
            core.getExecutor().log("Stopping specific sound: " + getProperty("sound").getAsString());
            if (core.getExecutor().stopSound(getProperty("sound").getAsString(), null))
                return new EffectResult("Stopping specific sound: " + getProperty("sound").getAsString(), true);
            else
                return new EffectResult("Failed to stop specific sound.", false);
        }
    }
}
