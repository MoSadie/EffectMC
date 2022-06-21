package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class NarrateHandler extends EffectRequestHandler {

    public NarrateHandler(EffectMCCore core) {
        super(core);
        addStringProperty("message", "", true, "Message", "Hello world!");
        addBooleanProperty("interrupt", false, false, "Interrupt Narrator", "Enabled", "Disabled");
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
    EffectResult execute() {
        core.getExecutor().log("Narrating message: " + getProperty("message").getAsString());
        if (core.getExecutor().narrate(getProperty("message").getAsString(), getProperty("interrupt").getAsBoolean()))
            return new EffectResult("Narrating message: " + getProperty("message").getAsString(), true);
        else
            return new EffectResult("Failed to narrate message.", false);
    }
}
