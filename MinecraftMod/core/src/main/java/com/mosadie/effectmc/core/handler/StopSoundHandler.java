package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class StopSoundHandler implements HttpHandler {

    private final EffectMCCore core;

    public StopSoundHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("StopSound started");
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

        String sound = parameters.containsKey("sound") ? parameters.get("sound").toString() : null;
        String category = parameters.containsKey("category") ? parameters.get("category").toString() : null;

        core.getExecutor().stopSound(sound, category);

        String response = "Stopping sound: " + sound + " in category: " + category;
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());

        exchange.getResponseBody().close();


    }
}
