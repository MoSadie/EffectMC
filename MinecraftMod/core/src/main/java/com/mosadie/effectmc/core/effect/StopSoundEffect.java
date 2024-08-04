package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class StopSoundEffect extends Effect {

    public StopSoundEffect() {
        super();
        getPropertyManager().addStringProperty("sound", "minecraft:entity.ghast.ambient", false, "Sound", "Sound ID or 'all' to stop all sounds");
//        getPropertyManager().addSelectionProperty("category", "", false, "Category", PlaySoundHandler.SOUND_CATEGORY.toStringArray());
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        // Note for the future, to re-enable category specific sound searching update the stopSound calls as well as the commented out line in the constructor.

        String sound = getPropAsString(args, "sound");

        if (sound.equalsIgnoreCase("null") || sound.equalsIgnoreCase("all") || sound.equalsIgnoreCase("")) {
            core.getExecutor().log("Stopping all sounds");
            if (core.getExecutor().stopSound(null, null))
                return new EffectResult("Stopping all sounds.", EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to stop all sounds.", EffectResult.Result.ERROR);
        } else {
            core.getExecutor().log("Stopping specific sound: " + sound);
            if (core.getExecutor().stopSound(sound, null))
                return new EffectResult("Stopping specific sound: " + sound, EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to stop specific sound.", EffectResult.Result.ERROR);
        }
    }
}
