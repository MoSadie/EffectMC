package io.github.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;

import java.io.IOException;

public class RootHandler implements HttpHandler {
    private final EffectMCCore core;

    public RootHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Root Message Received!");
        String response = "MCSDE Version " + RootHandler.class.getPackage().getImplementationVersion();
        //core.getExecutor().log("Response string created!");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        //core.getExecutor().log("Response Header Set");
        exchange.getResponseBody().write(response.getBytes());
        //core.getExecutor().log("Response Written");
        exchange.getResponseBody().close();
        //core.getExecutor().log("Response Output Closed");
    }
}