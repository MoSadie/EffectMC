package com.mosadie.effectmc.core.handler.http;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;
import com.mosadie.effectmc.core.effect.internal.Effect;
import com.mosadie.effectmc.core.effect.internal.EffectRequest;
import com.mosadie.effectmc.core.handler.Device;
import com.mosadie.effectmc.core.handler.DeviceType;
import com.mosadie.effectmc.core.property.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class EffectRequestHandler implements HttpHandler {

    final EffectMCCore core;
    final Effect effect;

    public EffectRequestHandler(EffectMCCore core, Effect effect) {
        this.core = core;
        this.effect = effect;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log(effect.getEffectName() + " Started");

        // Process params
        Map<String, Object> parameters = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        try {
            Util.parseQuery(query, parameters);
        } catch (UnsupportedEncodingException e) {
            core.getExecutor().log("Exception occurred parsing query!");
            parameters = new HashMap<>(); // Causes trustCheck to fail automatically.
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
            bodyParameters = new HashMap<>(); // Causes trustCheck to fail automatically.
        }

        // Handle no device (send to web interface)
        if (!parameters.containsKey("device") && !bodyParameters.containsKey("device")) {
            sendToWebInterface(exchange);
            return;
        }

        boolean isBody = false;

        if (bodyParameters.containsKey("device")) {
            isBody = true;
        }

        // Create EffectRequest
        EffectRequest request = new EffectRequest(effect.getEffectId(), isBody ? bodyParameters : parameters);

        Device device = new Device(isBody ? bodyParameters.get("device").toString() : parameters.get("device").toString(), DeviceType.OTHER);

        request.getArgs().remove("device");

        // Execute effect
        Effect.EffectResult response = core.triggerEffect(device, request);



        int status = 500;

        if (response == null) {
            String message = "Internal Server Error";
            exchange.sendResponseHeaders(500, message.getBytes().length);
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
            return;
        }

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
                status = 500;
                break;
        }

        // Send response
        exchange.sendResponseHeaders(status, response.message.getBytes().length);
        exchange.getResponseBody().write(response.message.getBytes());
        exchange.getResponseBody().close();
    }

    private void sendToWebInterface(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder("<html><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>" + effect.getEffectName() + " - EffectMC</title><link rel=\"stylesheet\" href=\"/style.css\"><body><div class=\"wrapper\"><h1>" + effect.getEffectName() + "</h1>");
        boolean needsPost = false;
        for (EffectProperty property : effect.getPropertyManager().getPropertiesList()) {
            if (property.getPropType().equals(EffectProperty.PropertyType.BODY)) {
                needsPost = true;
                break;
            }
        }
        response.append("<form method=\"").append(needsPost ? "post" : "get").append("\" target=\"result\"><input type=\"hidden\" name=\"device\" value=\"browser\"/>");

        for(EffectProperty property : effect.getPropertyManager().getPropertiesList()) {
            response.append(property.getHTMLInput()).append("</br>");
        }

        response.append("<input type=\"submit\" value=\"Trigger Effect\"></form><br/><iframe name=\"result\"></iframe></br></br><a href=\"/\">Back to effect list</a></div></body></html>");
        exchange.sendResponseHeaders(200, response.toString().getBytes().length);
        exchange.getResponseBody().write(response.toString().getBytes());
        exchange.getResponseBody().close();
    }
}
