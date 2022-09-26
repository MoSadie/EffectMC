package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class SetPovHandler extends EffectRequestHandler {

    public SetPovHandler(EffectMCCore core) {
        super(core);
        addSelectionProperty("pov", POV.FIRST_PERSON.name(), true, "POV", POV.toStringArray());
    }

    @Override
    public String getEffectName() {
        return "Set POV";
    }

    @Override
    public String getEffectSlug() {
        return "pov";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the point of view.";
    }

    @Override
    EffectResult execute() {
        core.getExecutor().log("Setting POV: " + getProperty("pov").getAsString());
        if (core.getExecutor().setPOV(POV.fromString(getProperty("pov").getAsString())))
            return new EffectResult("Set POV: " + getProperty("pov").getAsString(), true);
        else
            return new EffectResult("Failed to set POV", false);
    }

    public enum POV {
        FIRST_PERSON,
        THIRD_PERSON_BACK,
        THIRD_PERSON_FRONT;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (POV type : POV.values()) {
                list.add(type.name());
            }
            return list.toArray(new String[0]);
        }

        public static POV fromString(String name) {
            for (POV pov : POV.values()) {
                if (name.equalsIgnoreCase(pov.name())) {
                    return pov;
                }
            }

            return POV.FIRST_PERSON;
        }
    }
}
