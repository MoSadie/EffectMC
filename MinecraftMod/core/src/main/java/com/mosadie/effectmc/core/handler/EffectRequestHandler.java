package com.mosadie.effectmc.core.handler;

import com.mosadie.effectmc.core.EffectMCCore;
import com.mosadie.effectmc.core.Util;
import com.mosadie.effectmc.core.property.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public abstract class EffectRequestHandler implements HttpHandler {

    final EffectMCCore core;
    private final Map<String, EffectProperty> properties;
    private final List<EffectProperty> propertiesList;

    public EffectRequestHandler(EffectMCCore core) {
        this.core = core;
        this.properties = new HashMap<>();
        this.propertiesList = new ArrayList<>();
    }

    void addProperty(String propKey, EffectProperty property) {
        properties.put(propKey, property);
        propertiesList.add(property);
    }

    public EffectProperty getProperty(String propKey) {
        return properties.getOrDefault(propKey, null);
    }

    public Map<String, EffectProperty> getProperties() {
        return properties;
    }

    public List<EffectProperty> getPropertiesList() {
        return propertiesList;
    }

    public abstract String getEffectName();
    public String getEffectSlug() {
        return getEffectName().replaceAll(" ", "").toLowerCase();
    }
    public abstract String getEffectTooltip();
    abstract EffectResult execute();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        core.getExecutor().log(getEffectName() + " Started");

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

        // Trust Check
        if (!Util.trustCheck(parameters, bodyParameters, exchange, core))
            return;

        // Set all properties
        for(String propKey : properties.keySet()) {
            if (properties.get(propKey).getPropType() == EffectProperty.PropertyType.BODY) {
                if (bodyParameters.containsKey(propKey)) {
                    if (!properties.get(propKey).setValue(bodyParameters.get(propKey))) {
                        core.getExecutor().log(getEffectName() + "failed: " + propKey + " in body is invalid.");
                        String response = propKey + " in body is invalid";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                        return;
                    }
                } else if (properties.get(propKey).isRequired()) {
                    core.getExecutor().log(getEffectName() + " failed: " + propKey + " in body is missing.");
                    String response = propKey + " in body is missing";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
            } else if (parameters.containsKey(propKey)) {
                if (!properties.get(propKey).setValue(parameters.get(propKey)) && properties.get(propKey).isRequired()) {
                    core.getExecutor().log(getEffectName() + " failed: " + propKey + " is invalid.");
                    String response = propKey + " is invalid";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
            } else if (properties.get(propKey).isRequired()) {
                core.getExecutor().log(getEffectName() + " failed: " + propKey + " is missing.");
                String response = propKey + " is missing";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }
        }

        // Execute effect
        EffectResult response = execute();

        // Send response
        exchange.sendResponseHeaders((response != null && response.success ? 200 : 500), response.message.getBytes().length);
        exchange.getResponseBody().write(response.message.getBytes());
        exchange.getResponseBody().close();
    }

    void addStringProperty(String id, String value, boolean required, String label, String placeholder) {
        addProperty(id, new StringProperty(id, value, required, label, placeholder));
    }

    void addBooleanProperty(String id, boolean value, boolean required, String label, String trueLabel, String falseLabel) {
        addProperty(id, new BooleanProperty(id, value, required, label, trueLabel, falseLabel));
    }

    void addFloatProperty(String id, float value, boolean required, String label, float min, float max) {
        addProperty(id, new FloatProperty(id, value, required, label, min, max));
    }

    void addSelectionProperty(String id, String selected, boolean required, String label, String... options) {
        addProperty(id, new SelectionProperty(id, selected, required, label, options));
    }

    void addIntegerProperty(String id, int value, boolean required, String label, String placeholder) {
        addProperty(id, new IntegerProperty(id, value, required, label, placeholder));
    }

    void addBodyProperty(String id, String value, boolean required, String label, String placeholder) {
        addProperty(id, new BodyProperty(id, value, required, label, placeholder));
    }

    void addCommentProperty(String comment) {
        String id = UUID.randomUUID().toString();
        addProperty(id, new CommentProperty(id, comment));
    }

    private void sendToWebInterface(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder("<html><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>" + getEffectName() + " - EffectMC</title><link rel=\"stylesheet\" href=\"/style.css\"><body><div class=\"wrapper\"><h1>" + getEffectName() + "</h1>");
        boolean needsPost = false;
        for (EffectProperty property : properties.values()) {
            if (property.getPropType().equals(EffectProperty.PropertyType.BODY)) {
                needsPost = true;
                break;
            }
        }
        response.append("<form method=\"").append(needsPost ? "post" : "get").append("\" target=\"result\"><input type=\"hidden\" name=\"device\" value=\"browser\"/>");

        for(EffectProperty property : propertiesList) {
            response.append(property.getHTMLInput()).append("</br>");
        }

        response.append("<input type=\"submit\" value=\"Trigger Effect\"></form><br/><iframe name=\"result\"></iframe></br></br><a href=\"/\">Back to effect list</a></div></body></html>");
        exchange.sendResponseHeaders(200, response.toString().getBytes().length);
        exchange.getResponseBody().write(response.toString().getBytes());
        exchange.getResponseBody().close();
    }

    public static class EffectResult {
        public final String message;
        public final boolean success;

        public EffectResult(String message, boolean success) {
            this.message = message;
            this.success = success;
        }
    }
}
