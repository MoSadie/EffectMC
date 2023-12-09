package com.mosadie.effectmc.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mosadie.effectmc.core.handler.*;
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

    private final List<EffectRequestHandler> effects;

    private HttpServer server;

    private Set<String> trustedDevices;
    private boolean trustNextRequest;

    public EffectMCCore(File configFile, File trustFile, EffectExecutor executor) {
        this.configFile = configFile;
        this.trustFile = trustFile;
        this.executor = executor;
        trustedDevices = new HashSet<>();
        trustNextRequest = false;

        gson = new Gson();

        effects = new ArrayList<>();
        effects.add(new JoinServerHandler(this));
        effects.add(new SkinLayerHandler(this));
        effects.add(new SendChatMessageHandler(this));
        effects.add(new ReceiveChatMessageHandler(this));
        effects.add(new ShowTitleHandler(this));
        effects.add(new ShowActionMessageHandler(this));
        effects.add(new DisconnectHandler(this));
        effects.add(new PlaySoundHandler(this));
        effects.add(new StopSoundHandler(this));
        effects.add(new ShowToastHandler(this));
        effects.add(new OpenBookHandler(this));
        effects.add(new NarrateHandler(this));
        effects.add(new LoadWorldHandler(this));
        effects.add(new SetSkinHandler(this));
        effects.add(new OpenScreenHandler(this));
        effects.add(new SetFovHandler(this));
        effects.add(new SetPovHandler(this));
        effects.add(new SetGUIScaleHandler(this));
        effects.add(new SetGammaHandler(this));
        effects.add(new SetGameModeHandler(this));
        effects.add(new ChatVisibilityHandler(this));
        effects.add(new SetRenderDistanceHandler(this));
        effects.add(new RejoinHandler(this));
        effects.add(new ShowItemToastHandler(this));
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
                trustedDevices = gson.fromJson(reader, new TypeToken<Set<String>>() {}.getType());
                reader.close();
            }
        } catch (IOException | JsonSyntaxException e) {
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

        for(EffectRequestHandler effect : effects) {
            server.createContext("/" + effect.getEffectSlug(), effect);
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

    boolean checkTrust(String device) {
        if (trustNextRequest) {
            trustNextRequest = false;
            executor.log("Prompting to trust device with id " + device);
            executor.showTrustPrompt(device);
        }
        return trustedDevices.contains(device);
    }

    public void addTrustedDevice(String device) {
        trustedDevices.add(device);

        try {
            FileWriter writer = new FileWriter(trustFile);
            writer.write(gson.toJson(trustedDevices));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<EffectRequestHandler> getEffects() {
        return effects;
    }

    public static class TrustBooleanConsumer implements BooleanConsumer {
        private final String device;
        private final EffectMCCore core;

        public TrustBooleanConsumer(String device, EffectMCCore core) {
            this.device = device;
            this.core = core;
        }

        @Override
        public void accept(boolean t) {
            if (t) {
                core.addTrustedDevice(device);
            }

            core.getExecutor().resetScreen();
        }
    }
}
