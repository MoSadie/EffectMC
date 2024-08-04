package com.mosadie.effectmc.core.handler;

public class Device {
    private final String id;
    private final DeviceType type;

    public Device(String id, DeviceType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public DeviceType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Device) {
            Device other = (Device) obj;
            return other.id.equals(id) && other.type == type;
        }
        return false;
    }
}
