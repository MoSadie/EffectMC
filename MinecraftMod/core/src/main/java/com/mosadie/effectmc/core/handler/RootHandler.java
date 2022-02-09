package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mosadie.effectmc.core.EffectMCCore;

import java.io.IOException;

public class RootHandler implements HttpHandler {
    private final EffectMCCore core;

    private final String homepage;

    public RootHandler(EffectMCCore core) {
        this.core = core;
        homepage = generateHomepage();
    }

    private String generateHomepage() {
        StringBuilder response = new StringBuilder("<html><head><title>EffectMC v" + RootHandler.class.getPackage().getImplementationVersion() + "</title><body><h1>EffectMC v" + RootHandler.class.getPackage().getImplementationVersion() + "</h1><h2>Effect List:</h2><ul>");

        for (EffectRequestHandler effect : core.getEffects()) {
            response.append("<li><a href=\"" + effect.getEffectSlug() + "\">" + effect.getEffectName() + "</a></li>");
        }

        response.append("</ul></body></html>");
        return response.toString();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Root Message Received!");


        exchange.sendResponseHeaders(200, homepage.getBytes().length);
        exchange.getResponseBody().write(homepage.toString().getBytes());
        exchange.getResponseBody().close();
    }
}
