package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ShowTitleHandler implements HttpHandler {

    private final EffectMCCore core;

    public ShowTitleHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("ShowTitle started");
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


            core.getExecutor().log("Showing title: " + title + " Subtitle: " + subtitle);

            core.getExecutor().showTitle(title, subtitle);

            String response = "Show title: " + title + " Subtitle: " + subtitle;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("ShowTitle failed");
            String response = "Title not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();
    }
}
