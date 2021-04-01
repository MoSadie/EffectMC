package io.github.mosadie.mcsde.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.mcsde.core.MCSDECore;
import io.github.mosadie.mcsde.core.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SendChatMessageHandler implements HttpHandler {

    private final MCSDECore core;

    public SendChatMessageHandler(MCSDECore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("SendChatMessage started");
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

        if (parameters.containsKey("message")) {
            String message = parameters.get("message").toString();
            core.getExecutor().log("Sending chat message: " + message);

            core.getExecutor().sendChatMessage(message);

            String response = "Sending chat message: " + message;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } else {
            core.getExecutor().log("SendChatMessage failed");
            String response = "Message not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
        exchange.getResponseBody().close();
    }
}
