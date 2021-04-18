package io.github.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SkinLayerHandler implements HttpHandler {

    private final EffectMCCore core;

    public SkinLayerHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("SkinLayerHandler triggered");
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

        if (parameters.containsKey("section")) {
            SKIN_SECTION section = SKIN_SECTION.getFromName(parameters.get("section").toString());

            if (section == null) {
                core.getExecutor().log("Skin section invalid");
                String response = "Invalid Skin Section";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            if (parameters.containsKey("visibility")) {
                // Set skin layer, fallback to false if unknown string.
                boolean newVisibility = Boolean.parseBoolean(parameters.get("visibility").toString());
                core.getExecutor().setSkinLayer(section, newVisibility);
            } else {
                // Toggle skin layer
                core.getExecutor().toggleSkinLayer(section);
            }

            core.getExecutor().log("Toggle Skin Layer");
            String response = "Toggled Skin Layer: " + section.toString();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("SkinLayer failed");
            String response = "Section not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();

    }

    public enum SKIN_SECTION {
        ALL,
        ALL_BODY,
        CAPE,
        JACKET,
        LEFT_SLEEVE,
        RIGHT_SLEEVE,
        LEFT_PANTS_LEG,
        RIGHT_PANTS_LEG,
        HAT;

        public static SKIN_SECTION getFromName(String name) {
            try {
                return SKIN_SECTION.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}