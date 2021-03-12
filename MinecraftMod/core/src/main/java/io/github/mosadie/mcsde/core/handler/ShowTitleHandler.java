package io.github.mosadie.mcsde.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.mosadie.mcsde.core.MCSDECore;
import io.github.mosadie.mcsde.core.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShowTitleHandler implements HttpHandler {

    private final MCSDECore core;

    public ShowTitleHandler(MCSDECore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("ShowTitle started");
        Map<String, Object> parameters = new HashMap<String, Object>();
        String query = exchange.getRequestURI().getQuery();
        core.getExecutor().log(query);
        Util.parseQuery(query, parameters);

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
            exchange.getResponseBody().close();
        } else {
            core.getExecutor().log("ShowTitle failed");
            String response = "Title not defined";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }


    }
}
