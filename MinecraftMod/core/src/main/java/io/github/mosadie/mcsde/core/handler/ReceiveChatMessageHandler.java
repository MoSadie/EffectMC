package io.github.mosadie.mcsde.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.mcsde.core.MCSDECore;
import io.github.mosadie.mcsde.core.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReceiveChatMessageHandler implements HttpHandler {

    private final MCSDECore core;

    public ReceiveChatMessageHandler(MCSDECore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("ReceiveChatMessage started");
        Map<String, Object> parameters = new HashMap<String, Object>();
        String query = exchange.getRequestURI().getQuery();
        core.getExecutor().log(query);
        Util.parseQuery(query, parameters);

        if (parameters.containsKey("message")) {
            String message = parameters.get("message").toString();
            core.getExecutor().log("Receiving chat message: " + message);

            core.getExecutor().receiveChatMessage(message);

            String response = "Received chat message: " + message;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } else {
            core.getExecutor().log("ReceiveChatMessage failed");
            String response = "Message not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }


    }
}
