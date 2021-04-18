package io.github.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DisconnectHandler implements HttpHandler {

    private final EffectMCCore core;

    public DisconnectHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Disconnect started");
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

        if (parameters.containsKey("nextscreen")) {
            NEXT_SCREEN nextScreen = NEXT_SCREEN.getFromName(parameters.get("nextscreen").toString());

            if (nextScreen == null) {
                core.getExecutor().log("Next Screen invalid");
                String response = "Invalid Next Screen";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            core.getExecutor().log("Triggering Disconnect");
            String response = "Triggering Disconnect";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();

            String title = parameters.containsKey("title") ? parameters.get("title").toString() : "";
            String message = parameters.containsKey("message") ? parameters.get("message").toString() : "";
            core.getExecutor().triggerDisconnect(nextScreen, title, message);
        } else {
            core.getExecutor().log("Disconnect failed");
            String response = "Next Screen not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }


    }

    public enum NEXT_SCREEN {
        MAIN_MENU,
        SERVER_SELECT,
        WORLD_SELECT;

        public static DisconnectHandler.NEXT_SCREEN getFromName(String name) {
            try {
                return DisconnectHandler.NEXT_SCREEN.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
