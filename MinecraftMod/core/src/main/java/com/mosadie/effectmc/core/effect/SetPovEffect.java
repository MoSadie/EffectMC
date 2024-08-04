package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetPovEffect extends Effect {

    public SetPovEffect() {
        super();
        getPropertyManager().addSelectionProperty("pov", POV.FIRST_PERSON.name(), true, "POV", POV.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set POV";
    }

    @Override
    public String getEffectId() {
        return "pov";
    }

    @Override
    public String getEffectTooltip() {
        return "Set the point of view.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        POV pov = POV.fromString(getPropAsString(args, "pov"));

        core.getExecutor().log("Setting POV: " + pov.name());
        if (core.getExecutor().setPOV(pov))
            return new EffectResult("Set POV: " + pov.name(), EffectResult.Result.SUCCESS);
        else
            return new EffectResult("Failed to set POV", EffectResult.Result.ERROR);
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
