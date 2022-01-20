package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoadWorldHandler implements HttpHandler {

    private final EffectMCCore core;

    public LoadWorldHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("LoadWorld started");
        Map<String, Object> parameters = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        try {
            Util.parseQuery(query, parameters);
        } catch (UnsupportedEncodingException e) {
            core.getExecutor().log("Exception occurred parsing query!");
            parameters = new HashMap<>();
        }
        if (!Util.trustCheck(parameters, exchange, core))
            return;

        if (parameters.containsKey("world")) {
            core.getExecutor().log("Loading world");
            String response = "Loading world";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
           core.getExecutor().loadWorld(parameters.get("world").toString());
        } else {
            core.getExecutor().log("LoadWorld failed");
            String response = "World name not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }


    }
}
