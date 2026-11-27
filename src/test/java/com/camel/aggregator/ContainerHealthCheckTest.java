package com.camel.aggregator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerHealthCheckTest {

    @Test
    void returnsTrueOnHttp200() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            int port = server.getLocalPort();
            executor.submit(() -> respondOnce(server, "HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nOK"));
            assertTrue(ContainerHealthCheck.isHealthy("http://127.0.0.1:" + port + "/"));
        }
    }

    @Test
    void returnsFalseOnHttp503() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            int port = server.getLocalPort();
            executor.submit(() -> respondOnce(server, "HTTP/1.1 503 Service Unavailable\r\nContent-Length: 4\r\nConnection: close\r\n\r\nDOWN"));
            assertFalse(ContainerHealthCheck.isHealthy("http://127.0.0.1:" + port + "/"));
        }
    }

    @Test
    void returnsFalseOnUnreachablePort() throws Exception {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            unusedPort = socket.getLocalPort();
        }
        assertFalse(ContainerHealthCheck.isHealthy("http://127.0.0.1:" + unusedPort + "/"));
    }

    private static void respondOnce(ServerSocket server, String response) {
        try (Socket client = server.accept();
             OutputStream out = client.getOutputStream()) {
            out.write(response.getBytes(StandardCharsets.US_ASCII));
            out.flush();
        } catch (IOException ignored) {
            // Test cleanup may close the socket
        }
    }
}
