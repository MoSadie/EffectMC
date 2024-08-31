package com.mosadie.effectmc.core.handler.http;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;
import com.mosadie.effectmc.core.property.EffectProperty;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles requests to trigger effects using raw JSON instead of a specific effect handler.
 */
public class EffectRawRequestHandler implements HttpHandler {

    final EffectMCCore core;

    public EffectRawRequestHandler(EffectMCCore core) {
        this.core = core;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log("Raw Request Started");

        // Process params
        Map<String, Object> parameters = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        try {
            Util.parseQuery(query, parameters);
        } catch (UnsupportedEncodingException e) {
            core.getExecutor().log("Exception occurred parsing query!");
            String response = "Something went wrong parsing the query.";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            return;
        }



        // Read from Body
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String body = null;

        try {
            body = reader.readLine();

        } catch (IOException e) {
            core.getExecutor().log("WARN: An IOException occurred reading body: " + e.toString());
            String response = "Something went wrong reading the body of the request.";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        Map<String, Object> bodyParameters = new HashMap<>();

        try {
            Util.parseQuery(body, bodyParameters);
        } catch (UnsupportedEncodingException e) {
            core.getExecutor().log("Exception occurred parsing body query!");
            String response = "Something went wrong parsing the body query.";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        // Handle no device (send unauthorized)
        if (!parameters.containsKey("device") && !bodyParameters.containsKey("device")) {
            String message = "Missing device property";
            exchange.sendResponseHeaders(401, message.getBytes().length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        // Combine the parameters, where any specified in the body overwrite the query parameters
        for (String key : bodyParameters.keySet()) {
            parameters.put(key, bodyParameters.get(key));
        }

        // Check for request parameter, if not present, send 400
        if (!parameters.containsKey("request")) {
            String message = "Missing request property";
            exchange.sendResponseHeaders(400, message.getBytes().length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        // Create EffectRequest
        EffectRequest request = core.requestFromJson(parameters.get("request").toString());

        if (request == null) {
            String message = "Invalid request JSON, check game logs for more information";
            exchange.sendResponseHeaders(400, message.getBytes().length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        Device device = new Device(parameters.get("device").toString(), DeviceType.OTHER);

        request.getArgs().remove("device");

        // Execute effect
        Effect.EffectResult response = core.triggerEffect(device, request);

        if (response == null) {
            String message = "Internal Server Error";
            exchange.sendResponseHeaders(500, message.getBytes().length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        int status = 500;

        switch (response.result) {
            case ERROR:
            case SKIPPED:
                status = 500;
                break;
            case UNAUTHORIZED:
                status = 401;
                break;
            case UNKNOWN:
                status = 404;
                break;
            case SUCCESS:
                status = 200;
                break;
        }

        // Send response
        exchange.sendResponseHeaders(status, response.message.getBytes().length);
        exchange.getResponseBody().write(response.message.getBytes());
        exchange.getResponseBody().close();
    }
}
