package com.mosadie.effectmc.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mosadie.effectmc.core.effect.*;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;
import com.mosadie.effectmc.core.handler.EffectHandler;
import com.mosadie.effectmc.core.handler.TrustHandler;
import com.mosadie.effectmc.core.handler.http.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class EffectMCCore {

    private final static int DEFAULT_PORT = 3000;

    public static final String TRANSLATION_TRIGGER_KEY = "com.mosadie.effectmc.trigger";

    private final File configFile;
    private final File trustFile;
    private final EffectExecutor executor;
    private final Gson gson;

    public Gson getGson() {
        return gson;
    }

    private final List<Effect> effects;
    private final Map<String, Effect> effectMap;

    private EffectHttpServer httpServer;

    private EffectHandler effectHandler;

    private TrustHandler trustHandler;

    public EffectMCCore(File configFile, File trustFile, EffectExecutor executor) {
        this.configFile = configFile;
        this.trustFile = trustFile;
        this.executor = executor;


        trustHandler = new TrustHandler(this, trustFile);

        gson = new Gson();

        effects = new ArrayList<>();
        effectMap = new HashMap<>();
        registerEffect(new JoinServerEffect());
        registerEffect(new SkinLayerEffect());
        registerEffect(new SendChatMessageEffect());
        registerEffect(new ReceiveChatMessageEffect());
        registerEffect(new ShowTitleEffect());
        registerEffect(new ShowActionMessageEffect());
        registerEffect(new DisconnectEffect());
        registerEffect(new PlaySoundEffect());
        registerEffect(new StopSoundEffect());
        registerEffect(new ShowToastEffect());
        registerEffect(new OpenBookEffect());
        registerEffect(new NarrateEffect());
        registerEffect(new LoadWorldEffect());
        registerEffect(new SetSkinEffect());
        registerEffect(new OpenScreenEffect());
        registerEffect(new SetFovEffect());
        registerEffect(new SetPovEffect());
        registerEffect(new SetGUIScaleEffect());
        registerEffect(new SetGammaEffect());
        registerEffect(new SetGameModeEffect());
        registerEffect(new ChatVisibilityEffect());
        registerEffect(new SetRenderDistanceEffect());
        registerEffect(new RejoinEffect());
        registerEffect(new ShowItemToastEffect());

        effectHandler = new EffectHandler(this, effectMap);
    }

    private void registerEffect(Effect effect) {
        effects.add(effect);
        effectMap.put(effect.getEffectId(), effect);
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
                return false;
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
                return false;
            }
        }

        if (!trustHandler.readTrustFile()) {
            getExecutor().log("Failed to read trust file.");
            return false;
        }


        // Start server
        httpServer = new EffectHttpServer(port, this);

        if (!httpServer.initServer()) {
            getExecutor().log("Failed to start http server.");
            return false;
        }

        return true;
    }

    public EffectRequest requestFromJson(String json) {
        try {
            return gson.fromJson(json, new TypeToken<EffectRequest>() {}.getType());
        } catch (JsonSyntaxException e) {
            executor.log("Invalid Request JSON! " + e.getMessage());
            return null;
        } catch (Exception e) {
            executor.log("Exception parsing Request JSON: " + e.getMessage());
            return null;
        }
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

    public List<Effect> getEffects() {
        return effects;
    }

    public static class TrustBooleanConsumer implements BooleanConsumer {
        private final Device device;
        private final EffectMCCore core;

        public TrustBooleanConsumer(Device device, EffectMCCore core) {
            this.device = device;
            this.core = core;
        }

        @Override
        public void accept(boolean t) {
            if (t) {
                core.trustHandler.addDevice(device);
            }

            core.getExecutor().resetScreen();
        }
    }

    public Effect.EffectResult triggerEffect(Device device, EffectRequest request) {
        return effectHandler.handleRequest(device, request);
    }

    public void setExportFlag() {
        effectHandler.setExportFlag();
    }

    public void setTrustFlag() {
        effectHandler.setTrustFlag();
    }

    public boolean checkTrust(Device device) {
        return trustHandler.checkTrust(device);
    }
}
