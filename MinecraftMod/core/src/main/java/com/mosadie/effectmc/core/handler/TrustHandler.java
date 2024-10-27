package com.mosadie.effectmc.core.handler;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mosadie.effectmc.core.EffectMCCore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TrustHandler {
    private final EffectMCCore core;
    private final File trustFile;
    private List<Device> trustedDevices;

    public TrustHandler(EffectMCCore core, File trustFile) {
        this.core = core;
        this.trustFile = trustFile;
        this.trustedDevices = new ArrayList<>();
    }


    public boolean readTrustFile() {

        // Read the trust file, verifying it contains a list of Device objects

        if (!trustFile.exists()) {
            // Create the file
            try {
                if (!trustFile.createNewFile()) {
                    core.getExecutor().log("Failed to create trust file. File already exists.");
                    return false;
                }

                trustedDevices = new ArrayList<>();

                writeTrustFile();

                return true;
            } catch (Exception e) {
                core.getExecutor().log("Failed to create trust file. Exception: " + e.getMessage());
                trustedDevices = null;
            }
            return false;
        }

        try {
             trustedDevices = core.getGson().fromJson(new FileReader(trustFile), new TypeToken<List<Device>>() {}.getType());
             return true;
        } catch (JsonSyntaxException e) {
            // Check if the old syntax is being used and convert if possible.
            try {
                FileReader reader = new FileReader(trustFile);
                Set<String> devices = core.getGson().fromJson(reader, new TypeToken<Set<String>>() {}.getType());

                reader.close();

                trustedDevices = new ArrayList<>();

                for (String device : devices) {
                    trustedDevices.add(new Device(device, DeviceType.OTHER));
                }

                writeTrustFile();

                core.getExecutor().log("Converted old trust file to new format.");
                return true;
            } catch (IOException ex) {
                // Log the error
                core.getExecutor().log("Failed to parse trust file. Exception: " + e.getMessage());
                e.printStackTrace();
                trustedDevices = null;
                return false;
            }
        } catch (Exception e) {
            core.getExecutor().log("Failed to read trust file. Exception: " + e.getMessage());
            e.printStackTrace();
            trustedDevices = null;
            return false;
        }
    }

    public void writeTrustFile() {
        try {
            FileWriter writer = new FileWriter(trustFile);
            writer.write(core.toJson(trustedDevices));
            writer.close();
        } catch (Exception e) {
            core.getExecutor().log("Failed to write trust file. Exception: " + e.getMessage());
        }
    }

    public boolean checkTrust(Device device) {
        if (device == null) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to check trust. Trust file not loaded.");
            return false;
        }

        return trustedDevices.contains(device);
    }

    public boolean checkWorld(String world) {
        if (world == null || world.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to check world trust. Trust file not loaded.");
            return false;
        }

        Device device = new Device(world, DeviceType.WORLD);

        return trustedDevices.contains(device);
    }

    // Format: "ip:port" (ex "localhost:25565")
    public boolean checkServer(String serverAndPort) {
        if (serverAndPort == null || serverAndPort.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to check server trust. Trust file not loaded.");
            return false;
        }

        // Hash the ip to prevent storing the actual ip
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    serverAndPort.toLowerCase().getBytes(StandardCharsets.UTF_8));
            serverAndPort = new String(encodedhash);
        } catch (Exception e) {
            core.getExecutor().log("Failed to hash server ip. Exception: " + e.getMessage());
            return false;
        }

        Device device = new Device(serverAndPort, DeviceType.SERVER);

        return trustedDevices.contains(device);
    }

    public boolean checkOther(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to check other device trust. Trust file not loaded.");
            return false;
        }

        Device device = new Device(deviceId, DeviceType.OTHER);

        return trustedDevices.contains(device);
    }

    public boolean addDevice(Device device) {
        if (device == null) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to add device. Trust file not loaded.");
            return false;
        }

        if (trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.add(device);
        writeTrustFile();
        return true;
    }

    public boolean removeDevice(Device device) {
        if (device == null) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to remove device. Trust file not loaded.");
            return false;
        }

        if (!trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.remove(device);
        writeTrustFile();
        return true;
    }

    public boolean addWorld(String world) {
        if (world == null || world.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to add world. Trust file not loaded.");
            return false;
        }

        Device device = new Device(world, DeviceType.WORLD);

        if (trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.add(device);
        writeTrustFile();
        return true;
    }

    public boolean removeWorld(String world) {
        if (world == null || world.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to remove world. Trust file not loaded.");
            return false;
        }

        Device device = new Device(world, DeviceType.WORLD);

        if (!trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.remove(device);
        writeTrustFile();
        return true;
    }

    public boolean addServer(String serverAndPort) {
        if (serverAndPort == null || serverAndPort.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to add server. Trust file not loaded.");
            return false;
        }

        // Hash the ip to prevent storing the actual ip
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    serverAndPort.toLowerCase().getBytes(StandardCharsets.UTF_8));
            serverAndPort = new String(encodedhash);
        } catch (Exception e) {
            core.getExecutor().log("Failed to hash server ip. Exception: " + e.getMessage());
            return false;
        }

        Device device = new Device(serverAndPort, DeviceType.SERVER);

        if (trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.add(device);
        writeTrustFile();
        return true;
    }

    public boolean removeServer(String serverAndPort) {
        if (serverAndPort == null || serverAndPort.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to remove server. Trust file not loaded.");
            return false;
        }

        // Hash the ip to prevent storing the actual ip
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    serverAndPort.toLowerCase().getBytes(StandardCharsets.UTF_8));
            serverAndPort = new String(encodedhash);
        } catch (Exception e) {
            core.getExecutor().log("Failed to hash server ip. Exception: " + e.getMessage());
            return false;
        }

        Device device = new Device(serverAndPort, DeviceType.SERVER);

        if (!trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.remove(device);
        writeTrustFile();
        return true;
    }

    public boolean addOther(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to add other device. Trust file not loaded.");
            return false;
        }

        Device device = new Device(deviceId, DeviceType.OTHER);

        if (trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.add(device);
        writeTrustFile();
        return true;
    }

    public boolean removeOther(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return false;
        }

        if (trustedDevices == null) {
            core.getExecutor().log("Failed to remove other device. Trust file not loaded.");
            return false;
        }

        Device device = new Device(deviceId, DeviceType.OTHER);

        if (!trustedDevices.contains(device)) {
            return false;
        }

        trustedDevices.remove(device);
        writeTrustFile();
        return true;
    }

    public List<Device> getTrustedDevices() {
        return trustedDevices;
    }
}
