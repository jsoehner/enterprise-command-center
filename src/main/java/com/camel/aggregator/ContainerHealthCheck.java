package com.camel.aggregator;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Minimal distroless-compatible container health check using only java.base.
 * Avoids requiring curl, wget, or a shell inside the runtime container image.
 */
public final class ContainerHealthCheck {

    private static final int TIMEOUT_MS = 3000;

    private ContainerHealthCheck() {
    }

    public static boolean isHealthy(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(urlString);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            int status = connection.getResponseCode();
            return status >= 200 && status < 400;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        String portEnv = System.getenv("SERVER_PORT");
        int port = 8080;
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv.trim());
                if (port < 1 || port > 65535) {
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.exit(1);
            }
        }
        String url = "http://127.0.0.1:" + port + "/";
        System.exit(isHealthy(url) ? 0 : 1);
    }
}
