package io.github.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListInputHandler implements HttpHandler {

    private final EffectMCCore core;

    public ListInputHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("List Input started");
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

        core.getExecutor().log("Sending input list");

        String[] inputList = core.getExecutor().getInputList();

        String response = core.toJson(inputList);

        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}