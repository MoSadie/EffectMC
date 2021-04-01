package io.github.mosadie.mcsde.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import io.github.mosadie.mcsde.core.handler.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class MCSDECore {

    private final static int DEFAULT_PORT = 3000;

    private final File configFile;
    private final File trustFile;
    private final EffectExecutor executor;
    private final Gson gson;

    private HttpServer server;

    private Set<String> trustedDevices;
    private boolean trustNextRequest;

    public MCSDECore(File configFile, File trustFile, EffectExecutor executor) {
        this.configFile = configFile;
        this.trustFile = trustFile;
        this.executor = executor;
        trustedDevices = new HashSet<>();
        trustNextRequest = false;

        gson = new Gson();
    }

    public boolean initServer() {
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
        server.createContext("/joinserver", new JoinServerHandler(this));
        server.createContext("/setskinlayervisibility", new SkinLayerHandler(this));
        server.createContext("/sendchat", new SendChatMessageHandler(this));
        server.createContext("/receivechat", new ReceiveChatMessageHandler(this));
        server.createContext("/showtitle", new ShowTitleHandler(this));
        server.createContext("/showactionmessage", new ShowActionMessageHandler(this));

        server.setExecutor(null);

        server.start();

        return true;
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

    public static class TrustBooleanConsumer implements BooleanConsumer {
        private final String device;
        private final MCSDECore core;

        public TrustBooleanConsumer(String device, MCSDECore core) {
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
