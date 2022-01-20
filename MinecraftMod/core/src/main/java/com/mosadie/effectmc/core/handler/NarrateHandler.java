package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class NarrateHandler implements HttpHandler {

    private final EffectMCCore core;

    public NarrateHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Narrate started");
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

        boolean interrupt = false;
        if (parameters.containsKey("interrupt")) {
            interrupt = Boolean.parseBoolean(parameters.get("interrupt").toString());
        }


        if (parameters.containsKey("message")) {
            String message = parameters.get("message").toString();
            core.getExecutor().log("Narrating message: " + message);

            core.getExecutor().narrate(message, interrupt);

            String response = "Narrated message: " + message;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("Narrate failed");
            String response = "Message not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();


    }
}
