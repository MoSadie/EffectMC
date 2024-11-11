package com.mosadie.effectmc.core.effect;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkinLayerEffect extends Effect {


    public SkinLayerEffect() {
        super();
        getPropertyManager().addSelectionProperty("section", SKIN_SECTION.ALL.toString(), true, "Skin Section", SKIN_SECTION.toStringArray());
        getPropertyManager().addSelectionProperty("visibility", VISIBILITY.TOGGLE.toString(), true, "Visibility", VISIBILITY.toStringArray());
        getPropertyManager().lock();
    }

    @Override
    public String getEffectName() {
        return "Set Skin Layer";
    }

    @Override
    public String getEffectTooltip() {
        return "Sets or toggles the visibility of skin layers.";
    }

    @Override
    public EffectResult execute(EffectMCCore core, Map<String, Object> args) {
        if (!getPropertyManager().argumentCheck(args)) {
            return new EffectResult("Invalid Arguments", EffectResult.Result.ERROR);
        }

        VISIBILITY visibility = VISIBILITY.getFromName(getPropAsString(args, "visibility"));
        SKIN_SECTION section = SKIN_SECTION.getFromName(getPropAsString(args, "section"));

        if (visibility == VISIBILITY.TOGGLE) {
            if (section != null && core.getExecutor().toggleSkinLayer(section))
                return new EffectResult("Toggled " + section + " skin section", EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to toggle skin section.", EffectResult.Result.ERROR);
        } else {
            if (section != null && core.getExecutor().setSkinLayer(section, visibility == VISIBILITY.SHOW))
                return new EffectResult("Set " + section + " to " + visibility, EffectResult.Result.SUCCESS);
            else
                return new EffectResult("Failed to set skin section visibility.", EffectResult.Result.ERROR);
        }
    }

    public enum SKIN_SECTION {
        ALL,
        ALL_BODY,
        CAPE,
        JACKET,
        LEFT_SLEEVE,
        RIGHT_SLEEVE,
        LEFT_PANTS_LEG,
        RIGHT_PANTS_LEG,
        HAT;

        public static SKIN_SECTION getFromName(String name) {
            try {
                return SKIN_SECTION.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (SKIN_SECTION section : SKIN_SECTION.values()) {
                list.add(section.name());
            }
            return list.toArray(new String[0]);
        }
    }

    public enum VISIBILITY {
        SHOW,
        HIDE,
        TOGGLE;

        public static String[] toStringArray() {
            List<String> list = new ArrayList<>();
            for (VISIBILITY visibility : VISIBILITY.values()) {
                list.add(visibility.name());
            }
            return list.toArray(new String[0]);
        }

        public static VISIBILITY getFromName(String name) {
            try {
                return VISIBILITY.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}