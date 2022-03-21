package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.net.MalformedURLException;
import java.net.URL;

public class SetSkinHandler extends EffectRequestHandler {

    private final EffectMCCore core;

    public SetSkinHandler(EffectMCCore core) {
        super(core);
        addStringProperty("url", "", true, "Skin URL", "");
        this.core = core;
    }

    @Override
    public String getEffectName() {
        return "Set Skin";
    }

    @Override
    public String getEffectTooltip() {
        return "Update your skin and locally refresh it.";
    }

    @Override
    String execute() {
        if (getProperty("url") != null) {
            try {
                URL skinUrl = new URL(getProperty("url").getAsString());

                core.getExecutor().log("Attempting to update & refresh skin.");
                if (core.getExecutor().setSkin(skinUrl))
                    return "Updated and locally refreshed skin.";
                else
                    return "Failed update and refresh skin.";
            } catch (MalformedURLException e) {
                core.getExecutor().log("Malformed url! Aborting effect.");
                e.printStackTrace();
                return "Malformed url!";
            }
        }

        return "Missing URL";
    }

}
