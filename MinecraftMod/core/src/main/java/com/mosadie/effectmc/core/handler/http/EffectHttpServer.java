package com.mosadie.effectmc.core.handler.http;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class EffectHttpServer {
    private final int port;
    private final EffectMCCore core;

    private HttpServer server;

    public EffectHttpServer(int port, EffectMCCore core) {
        this.port = port;
        this.core = core;
    }

    public boolean initServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            core.getExecutor().log("Exception occurred starting HTTP server: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        server.createContext("/", new RootHandler(core));

        server.createContext("/style.css", new CSSRequestHandler());

        server.createContext("/raw", new EffectRawRequestHandler(core));

        for(Effect effect : core.getEffects()) {
            server.createContext("/" + effect.getEffectId(), new EffectRequestHandler(core, effect));
        }

        server.setExecutor(null);

        server.start();
        return true;
    }
}
