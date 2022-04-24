package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SetSkinHandler extends EffectRequestHandler {

    private final EffectMCCore core;

    public SetSkinHandler(EffectMCCore core) {
        super(core);
        addStringProperty("url", "", true, "Skin URL", "");
        addSelectionProperty("skinType", SKIN_TYPE.CLASSIC.getValue(), true, "Skin Type", SKIN_TYPE.toStringArray());
        addCommentProperty("NOTE: This does not refresh your skin in-game, rejoin the server to refresh your skin.");
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
            try {
                URL skinUrl = new URL(getProperty("url").getAsString());

                SKIN_TYPE skinType = SKIN_TYPE.getFromName(getProperty("skinType").getAsString());

                core.getExecutor().log("Attempting to update & refresh skin.");
                if (core.getExecutor().setSkin(skinUrl, skinType))
                    return "Updated and locally refreshed skin.";
                else
                    return "Failed update and refresh skin.";
            } catch (MalformedURLException e) {
                core.getExecutor().log("Malformed url! Aborting effect.");
                e.printStackTrace();
                return "Malformed url!";
            }
    }

    public enum SKIN_TYPE {
        SLIM("slim"),
        CLASSIC("classic");

        private final String value;

        SKIN_TYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SKIN_TYPE getFromName(String name) {
            try {
                return SKIN_TYPE.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (SKIN_TYPE skinType : SKIN_TYPE.values()) {
                list.add(skinType.name());
            }
            return list.toArray(new String[0]);
        }
    }

}
