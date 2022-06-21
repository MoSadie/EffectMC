package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

public class RefreshSkinHandler extends EffectRequestHandler {

    private final EffectMCCore core;

    public RefreshSkinHandler(EffectMCCore core) {
        super(core);
        addStringProperty("uuid", "", true, "Skin URL", "");
        this.core = core;
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
    EffectResult execute() {
        return new EffectResult("Not implemented", false);
//        if (getProperty("uuid") != null) {
//            try {
//                UUID uuid = UUID.fromString(getProperty("uuid").getAsString());
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
