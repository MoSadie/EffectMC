package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetSkinEffect extends Effect {

    public SetSkinEffect() {
        super();
        getPropertyManager().addStringProperty("url", "", true, "Skin URL", "");
        getPropertyManager().addSelectionProperty("skinType", SKIN_TYPE.CLASSIC.name(), true, "Skin Type", SKIN_TYPE.toStringArray());
        getPropertyManager().addCommentProperty("NOTE: This does not refresh your skin in-game, rejoin the server to refresh your skin.");
        getPropertyManager().lock();
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
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        try {
                URL skinUrl = new URL(getPropAsString(args, "url"));

                SKIN_TYPE skinType = SKIN_TYPE.getFromName(getPropAsString(args, "skinType"));

                core.getExecutor().log("Attempting to update skin.");
                if (core.getExecutor().setSkin(skinUrl, skinType))
                    return new EffectResult("Updated skin.", EffectResult.Result.SUCCESS);
                else
                    return new EffectResult("Failed update and refresh skin.", EffectResult.Result.ERROR);
            } catch (MalformedURLException e) {
                core.getExecutor().log("Malformed url! Aborting effect.");
                e.printStackTrace();
                return new EffectResult("Malformed url!", EffectResult.Result.ERROR);
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
