package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class PlaySoundHandler implements HttpHandler {

    private final EffectMCCore core;

    public PlaySoundHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("PlaySound started");
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


        if (containsAllParameters(parameters)) {
            try {
                String sound = parameters.get("sound").toString();
                core.getExecutor().log("Attempting to play sound: " + sound);

                String category = parameters.get("category").toString();
                float volume = Float.parseFloat(parameters.get("volume").toString());
                float pitch = Float.parseFloat(parameters.get("pitch").toString());
                boolean repeat = Boolean.parseBoolean(parameters.get("repeat").toString());
                int repeatDelay = Integer.parseInt(parameters.get("repeatDelay").toString());
                String attenuationType = parameters.get("attenuationType").toString();
                double x = Double.parseDouble(parameters.get("x").toString());
                double y = Double.parseDouble(parameters.get("y").toString());
                double z = Double.parseDouble(parameters.get("z").toString());
                boolean relative = Boolean.parseBoolean(parameters.get("relative").toString());
                boolean global = Boolean.parseBoolean(parameters.get("global").toString());

                core.getExecutor().playSound(sound, category, volume, pitch, repeat, repeatDelay, attenuationType, x, y, z, relative, global);

                String response = "Playing sound: " + sound;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            } catch (NumberFormatException e) {
                core.getExecutor().log("PlaySound failed: Format exception");
                String response = "Format Exception";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
        } else {
            core.getExecutor().log("PlaySound failed");
            String response = "Missing parameter";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();


    }

    private boolean containsAllParameters(Map<String, Object> parameters) {
        String[] keys = new String[] { "sound", "category", "volume", "pitch", "repeat", "repeatDelay", "attenuationType", "x", "y", "z", "relative", "global" };

        for(String key : keys) {
            if (!parameters.containsKey(key)) {
                core.getExecutor().log("Missing parameter: " + key);
                return false;
            }
        }

        return true;
    }
}
