package com.gleb.net;

import com.gleb.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {
    private static final Map<String, Integer> requests = new HashMap<>();
    private static final Map<String, Long> blacklist = new HashMap<>();

    private final HttpServer server;

    public Server(String hostname, int port, int backlog, int nThreads) throws IOException {
        server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
        server.setExecutor(Executors.newFixedThreadPool(nThreads));
        server.createContext("/", exchange -> {
            if("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            }
        });
    }

    public void start() {
        if (server != null) {
            server.start();
        } else {
            throw new NullPointerException();
        }
    }

    private static void handlePostRequest(HttpExchange exchange) throws IOException {
        String[] uri = exchange.getRequestURI().toString().substring(1).split("/");
        String host = exchange.getRemoteAddress().getHostString();

        if (uri.length < 1) {
            return;
        }

        OutputStream outputStream = exchange.getResponseBody();
        Scanner scanner = new Scanner(exchange.getRequestBody());
        Map<String, String> params = new HashMap<>();

        String response = "";
        int code = 200;

        if (!scanner.hasNext()) {
            return;
        }

        String[] tmp = scanner.nextLine().split("&");

        for (String str: tmp) {
            params.put(str.split("=")[0], str.split("=")[1]);
        }

        if (uri[0].equalsIgnoreCase("reg")) {
            User.reg(params.get("username"), params.get("password"));
        } else if (uri[0].equalsIgnoreCase("login")) {
            if (blacklist.containsKey(host) && blacklist.get(host) - (new Date()).getTime() < 5000) {
                code = 423;
            } else {
                blacklist.remove(host);

                String token = User.login(params.get("username"), params.get("password"));

                if (token == null) {
                    if (!requests.containsKey(host)) {
                        requests.put(host, 1);
                    } else {
                        requests.compute(host, (key, val) -> val += 1);

                        if (requests.get(host) == 5) {
                            blacklist.put(host, (new Date()).getTime());
                        }
                    }

                    code = 403;
                } else {
                    requests.remove(host);
                    response = token;
                }
            }
        } else if (uri[0].equalsIgnoreCase("logout")) {
            int ret = User.logout(params.get("username"), params.get("token"));

            if (ret == 1) {
                code = 401;
            }
        } else if (uri[0].equalsIgnoreCase("payment")) {
            int ret = User.payment(params.get("username"), params.get("token"));

            if (ret == 1) {
                code = 401;
            } else if (ret == 2) {
                code = 406;
            } else if (ret == 0) {
                code = 202;
            }
        }

        exchange.sendResponseHeaders(code, response.length());

        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
