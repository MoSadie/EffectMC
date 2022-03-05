package com.mosadie.effectmc.core.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class CSSRequestHandler implements HttpHandler {

    final static String DATA = ".wrapper {\n" +
            "    margin: auto;\n" +
            "    width: fit-content;\n" +
            "    text-align: center;\n" +
            "    margin-top: 2em;\n" +
            "    border: grey 5px solid;\n" +
            "    padding: 2em;\n" +
            "    background: darkgrey;\n" +
            "    border-radius: 5px;\n" +
            "}\n" +
            "\n" +
            "body {\n" +
            "    background: dimgrey;\n" +
            "}\n" +
            "\n" +
            "label {\n" +
            "    padding-right: 5px;\n" +
            "}\n" +
            "\n" +
            "input {\n" +
            "    margin: 5px\n" +
            "}";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, DATA.getBytes().length);
        exchange.getResponseBody().write(DATA.getBytes());
        exchange.getResponseBody().close();
    }
}
