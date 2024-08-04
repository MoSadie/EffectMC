package com.mosadie.effectmc.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mosadie.effectmc.core.effect.*;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;
import com.mosadie.effectmc.core.handler.TrustHandler;
import com.mosadie.effectmc.core.handler.http.*;
import com.sun.net.httpserver.HttpServer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.*;

public class EffectMCCore {

    private final static int DEFAULT_PORT = 3000;

    private final File configFile;
    private final File trustFile;
    private final EffectExecutor executor;
    private final Gson gson;

    public Gson getGson() {
        return gson;
    }

    private final List<Effect> effects;

    private HttpServer server;

    private TrustHandler trustHandler;
    private boolean trustNextRequest;

    public EffectMCCore(File configFile, File trustFile, EffectExecutor executor) {
        this.configFile = configFile;
        this.trustFile = trustFile;
        this.executor = executor;
        trustedDevices = new HashMap<>(); // This is where I left off

        for (DeviceType type : DeviceType.values()) {
            trustedDevices.put(type, new HashSet<>());
        }

        trustNextRequest = false;

        gson = new Gson();

        effects = new ArrayList<>();
        effects.add(new JoinServerEffect());
        effects.add(new SkinLayerEffect());
        effects.add(new SendChatMessageEffect());
        effects.add(new ReceiveChatMessageEffect());
        effects.add(new ShowTitleEffect());
        effects.add(new ShowActionMessageEffect());
        effects.add(new DisconnectEffect());
        effects.add(new PlaySoundEffect());
        effects.add(new StopSoundEffect());
        effects.add(new ShowToastEffect());
        effects.add(new OpenBookEffect());
        effects.add(new NarrateEffect());
        effects.add(new LoadWorldEffect());
        effects.add(new SetSkinEffect());
        effects.add(new OpenScreenEffect());
        effects.add(new SetFovEffect());
        effects.add(new SetPovEffect());
        effects.add(new SetGUIScaleEffect());
        effects.add(new SetGammaEffect());
        effects.add(new SetGameModeEffect());
        effects.add(new ChatVisibilityEffect());
        effects.add(new SetRenderDistanceEffect());
        effects.add(new RejoinEffect());
        effects.add(new ShowItemToastEffect());
    }

    @SuppressWarnings("unused")
    public boolean initServer() throws URISyntaxException {
        int port = DEFAULT_PORT;
        if (configFile.exists()) {
            try {
                Map<String, String> config = gson.fromJson(new FileReader(configFile), new TypeToken<Map<String,String>>() {}.getType());
                if (config != null && config.containsKey("port")) {
                    port = Integer.parseInt(config.get("port"));
                }
            } catch (FileNotFoundException | NumberFormatException e) {
                e.printStackTrace();

            }
        } else {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("port", "" + port);

                FileWriter writer = new FileWriter(configFile);
                writer.write(gson.toJson(config));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (!trustFile.exists()) {
                // Create blank trust file
                FileWriter writer = new FileWriter(trustFile);
                writer.write(gson.toJson(trustedDevices));
                writer.close();
            } else {
                FileReader reader = new FileReader(trustFile);
                trustedDevices = gson.fromJson(reader, new TypeToken<Map<DeviceType, Set<String>>>() {}.getType());
                reader.close();
            }
        } catch (JsonSyntaxException e) {

            // Check if the old syntax is being used and convert if possible.
            try {
                FileReader reader = new FileReader(trustFile);
                Set<String> devices = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
                trustedDevices.put(DeviceType.OTHER, devices);
                reader.close();

                FileWriter writer = new FileWriter(trustFile);
                writer.write(gson.toJson(trustedDevices));
                writer.close();

                getExecutor().log("Converted old trust file to new format.");
            } catch (IOException ex) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        server.createContext("/", new RootHandler(this));

        server.createContext("/style.css", new CSSRequestHandler());

        for(Effect effect : effects) {
            server.createContext("/" + effect.getEffectId(), new EffectRequestHandler(this, effect));
        }

        server.setExecutor(null);

        server.start();

        return true;
    }

    public JsonObject fromJson(String json) {
        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException e) {
            executor.log("WARNING: Invalid Json attempted to be parsed.");
            return null;
        }
    }

    public String toJson(Object obj) {
            return gson.toJson(obj);
    }

    public EffectExecutor getExecutor() {
        return executor;
    }

    public void setTrustNextRequest() {
        trustNextRequest = true;
    }

    boolean checkTrust(String device, DeviceType type) {
        if (device == null || device.isEmpty() || type == null) {
            return false;
        }

        if (trustNextRequest) {
            trustNextRequest = false;
            executor.log("Prompting to trust device of type " + type + " with id " + device);
            executor.showTrustPrompt(device, type);
        }
        return trustedDevices.get(type).contains(device);
    }

    public void addTrustedDevice(String device, DeviceType type) {
        if (device == null || device.isEmpty() || type == null) {
            return;
        }

        trustedDevices.get(type).add(device);

        try {
            FileWriter writer = new FileWriter(trustFile);
            writer.write(gson.toJson(trustedDevices));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public static class TrustBooleanConsumer implements BooleanConsumer {
        private final String device;

        private final DeviceType type;
        private final EffectMCCore core;

        public TrustBooleanConsumer(String device, DeviceType type, EffectMCCore core) {
            this.device = device;
            this.type = type;
            this.core = core;
        }

        @Override
        public void accept(boolean t) {
            if (t) {
                core.addTrustedDevice(device, type);
            }

            core.getExecutor().resetScreen();
        }
    }

    public Effect.EffectResult triggerEffect(Device device, EffectRequest request) {
        for (Effect effect : effects) {
            if (effect.getEffectId().equals(request.getEffectId())) {
                return effect.execute(this, request.getArgs(), device);
            }
        }

        return new Effect.EffectResult("Effect not found.", Effect.EffectResult.Result.ERROR);
    }
}
