package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ShowToastHandler implements HttpHandler {

    private final EffectMCCore core;

    public ShowToastHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("ShowToast started");
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

        if (parameters.containsKey("title")) {
            String title = parameters.get("title").toString();
            String subtitle = "";

            if (parameters.containsKey("subtitle"))
                subtitle = parameters.get("subtitle").toString();


            core.getExecutor().log("Showing toast with title: " + title + " Subtitle: " + subtitle);

            core.getExecutor().showToast(title, subtitle);

            String response = "Show toast with title: " + title + " Subtitle: " + subtitle;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("ShowToast failed");
            String response = "Title not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();
    }
}
