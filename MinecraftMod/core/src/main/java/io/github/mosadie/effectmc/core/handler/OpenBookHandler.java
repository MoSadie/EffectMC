package io.github.mosadie.effectmc.core.handler;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.effectmc.core.EffectMCCore;
import io.github.mosadie.effectmc.core.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class OpenBookHandler implements HttpHandler {

    private final EffectMCCore core;

    public OpenBookHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("OpenBook started");
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String bookJson = null;

        try {
            bookJson = reader.readLine();
        } catch(IOException e) {
            core.getExecutor().log("WARN: An IOException occurred reading book json: " + e.toString());
            return;
        }

        if (bookJson != null) {
            JsonObject book = core.fromJson(bookJson);

            if (book == null) {
                core.getExecutor().log("Book invalid");
                String response = "Invalid Book";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            core.getExecutor().log("Triggering Open Book");
            String response = "Triggering Open Book";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();

            core.getExecutor().openBook(book);
        } else {
            core.getExecutor().log("OpenBook failed");
            String response = "Book not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}
