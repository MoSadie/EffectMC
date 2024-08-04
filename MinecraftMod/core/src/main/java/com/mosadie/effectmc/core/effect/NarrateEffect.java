package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class NarrateEffect extends Effect {

    public NarrateEffect() {
        super();
        getPropertyManager().addStringProperty("message", "", true, "Message", "Hello world!");
        getPropertyManager().addBooleanProperty("interrupt", false, false, "Interrupt Narrator", "Enabled", "Disabled");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Narrate";
    }

    @Override
    public String getEffectTooltip() {
        return "Say text using the in-game narrator.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }
        core.getExecutor().log("Narrating message: " + args.get("message"));
        if (core.getExecutor().narrate(getPropAsString(args, "message"), getPropAsBoolean(args, "interrupt")))
            return new EffectResult("Narrating message: " + getPropAsString(args, "message"), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to narrate message.", EffectResult.Result.ERROR);
    }
}
