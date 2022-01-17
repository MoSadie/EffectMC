package io.github.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class PressInputHandler implements HttpHandler {

    private final EffectMCCore core;

    public PressInputHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Press Input started");
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

        //Parameters:
        // key - key to be pressed, matches description
        // holdtime - how long to hold key

        if (parameters.containsKey("key")) {
            String keyDesc = parameters.get("key").toString();

            if (!core.getExecutor().keyExists(keyDesc)) {
                core.getExecutor().log("Key not found");
                String response = "Key not found!";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            core.getExecutor().log("Pressing input");
            String response = "Pressing input";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();

            long holdTime = getIfLong(parameters.get("holdtime").toString(), 100L);

            core.getExecutor().pressInput(keyDesc, holdTime);
        } else {
            core.getExecutor().log("Press Input failed");
            String response = "Input key not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    public long getIfLong(String string, long fallback) {
        if (string == null)
            return fallback;

        try {
            return Long.parseLong(string);
        } catch (NumberFormatException e) {
            //TODO maybe log?
            return fallback;
        }
    }

}
