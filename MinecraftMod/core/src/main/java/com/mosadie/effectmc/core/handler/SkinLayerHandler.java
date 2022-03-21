package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;

import java.util.ArrayList;
import java.util.List;

public class SkinLayerHandler extends EffectRequestHandler {


    public SkinLayerHandler(EffectMCCore core) {
        super(core);
        addSelectionProperty("section", SKIN_SECTION.ALL.toString(), true, "Skin Section", SKIN_SECTION.toStringArray());
        addSelectionProperty("visibility", VISIBILITY.TOGGLE.toString(), true, "Visibility", VISIBILITY.toStringArray());
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
    String execute() {
        if (getProperty("visibility").getAsString().equalsIgnoreCase(VISIBILITY.TOGGLE.name())) {
            if (core.getExecutor().toggleSkinLayer(SKIN_SECTION.getFromName(getProperty("section").getAsString())))
                return "Toggled " + SKIN_SECTION.getFromName(getProperty("section").getAsString()) + " skin section";
            else
                return "Failed to toggle skin section.";
        } else {
            if (core.getExecutor().setSkinLayer(SKIN_SECTION.getFromName(getProperty("section").getAsString()), getProperty("visibility").getAsString().equalsIgnoreCase(VISIBILITY.SHOW.name())))
                return "Set " + SKIN_SECTION.getFromName(getProperty("section").getAsString()) + " to " + getProperty("visibility").getAsString();
            else
                return "Failed to set skin section visibility.";
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
    }
}