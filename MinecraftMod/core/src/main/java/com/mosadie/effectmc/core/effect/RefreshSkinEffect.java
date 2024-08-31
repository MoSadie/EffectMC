package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.Map;

public class RefreshSkinEffect extends Effect {

    public RefreshSkinEffect() {
        super();
        getPropertyManager().addStringProperty("uuid", "", true, "Skin URL", "");
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Refresh Skin";
    }

    @Override
    public String getEffectTooltip() {
        return "Refreshes the specified player's skin.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        return new EffectResult("Not implemented", EffectResult.Result.ERROR);
//        if (args.containsKey("uuid")) {
//            try {
//                UUID uuid = UUID.fromString(getPropAsString(args, "uuid"));
//
//                core.getExecutor().log("Attempting to refresh skin.");
//                if (core.getExecutor().refreshSkin(uuid))
//                    return "Refreshed skin.";
//                else
//                    return "Failed to refresh skin.";
//            } catch (IllegalArgumentException e) {
//                core.getExecutor().log("Malformed UUID! Aborting effect.");
//                e.printStackTrace();
//                return "Malformed UUID!";
//            }
//        }
//
//        return "Missing UUID";
    }

}
