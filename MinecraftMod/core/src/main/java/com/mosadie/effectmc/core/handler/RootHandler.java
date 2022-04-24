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
        String title = "EffectMC" + (RootHandler.class.getPackage().getImplementationVersion() != null ? " v" + RootHandler.class.getPackage().getImplementationVersion() : "");
        StringBuilder response = new StringBuilder("<html><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>" + title + "</title><link rel=\"stylesheet\" href=\"/style.css\"></head><body><div class=\"wrapper\"><h1>" + title + "</h1><h2>Effect List:</h2><ul>");

        for (EffectRequestHandler effect : core.getEffects()) {
            response.append("<li><a href=\"" + effect.getEffectSlug() + "\">" + effect.getEffectName() + "</a></li>");
        }

        response.append("</ul></div></body></html>");
        return response.toString();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Showing homepage!");


        exchange.sendResponseHeaders(200, homepage.getBytes().length);
        exchange.getResponseBody().write(homepage.getBytes());
        exchange.getResponseBody().close();
    }
}
