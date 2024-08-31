package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.WorldState;

public enum DeviceType {
    SERVER,
    WORLD,
    OTHER;

    public static DeviceType fromWorldState(WorldState state) {
        switch (state) {
            case MULTIPLAYER:
                return SERVER;
            case SINGLEPLAYER:
                return WORLD;
            default:
                return OTHER;
        }
    }
}
