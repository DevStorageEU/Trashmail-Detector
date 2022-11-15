package eu.devstorage.tmd.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import eu.devstorage.tmd.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ApiHttpServer {
    private static final String DOMAIN_NAME_PATTERN_REGEX = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile(DOMAIN_NAME_PATTERN_REGEX);


    public void listen() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/check", new CheckHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server is listening on " + server.getAddress().toString());
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("domains", "" + Main.getInstance().getDatabase().getDomains().size());
            responseMap.put("info", "Usage: /check?validate=test.com or /check?validate=hello@world.com");
            String response = gson.toJson(responseMap);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }


    static class CheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long start = System.currentTimeMillis();
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            Map<String, String> responseMap = new HashMap<>();

            if (exchange.getRequestURI().getQuery() == null) {
                responseMap.put("error", "Please provide an 'email' or a 'domain' parameter to check. Example: /check?validate=test.com or /?validate=max@test.tld");
            } else {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                if (params.get("validate") != null) {
                    String email = params.get("validate").toLowerCase();
                    String domain = email.contains("@") ? email.split("@")[1] : email;
                    responseMap.put("provided", email.contains("@") ? "email" : "domain");
                    responseMap.put("domain", domain);
                    if (DOMAIN_NAME_PATTERN.matcher(domain).find()) {
                        responseMap.put("status", Main.getInstance().getDatabase().found(domain) ? "suspicious" : "unsuspicious");
                    } else {
                        responseMap.put("error", "The domain entered is invalid. Please check it for a valid structure.");
                    }
                } else {
                    responseMap.put("error", "Please provide an 'email' or a 'domain' parameter to check. Example: /check?validate=test.com or /?validate=max@test.tld");
                }
            }

            long end = System.currentTimeMillis();
            responseMap.put("processing ", end - start + "ms");
            String response = gson.toJson(responseMap);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    private static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
