package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveChatMessageHandler implements HttpHandler {

    private final EffectMCCore core;

    public ReceiveChatMessageHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("ReceiveChatMessage started");
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


        if (parameters.containsKey("message")) {
            String message = parameters.get("message").toString();
            core.getExecutor().log("Receiving chat message: " + message);

            core.getExecutor().receiveChatMessage(message);

            String response = "Received chat message: " + message;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("ReceiveChatMessage failed");
            String response = "Message not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();


    }
}
