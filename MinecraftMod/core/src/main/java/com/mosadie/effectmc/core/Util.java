package com.mosadie.effectmc.core;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {
    public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String[] pairs = query.split("[&]");
            for (String pair : pairs) {
                String[] param = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    public static boolean trustCheck(Map<String, Object> parameters, Map<String, Object> bodyParameters, HttpExchange exchange, EffectMCCore core) {
        if (parameters != null && parameters.containsKey("device")) {
            if (core.checkTrust(parameters.get("device").toString(), DeviceType.OTHER))
                return true;
        } else if (bodyParameters != null && bodyParameters.containsKey("device")) {
            if (core.checkTrust(bodyParameters.get("device").toString(),  DeviceType.OTHER))
                return true;
        }

        unauthenticatedResponse(exchange);
        return false;
    }

    private static void unauthenticatedResponse(HttpExchange exchange) {
        try {
            String response = "Unauthenticated";
            exchange.sendResponseHeaders(401, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
