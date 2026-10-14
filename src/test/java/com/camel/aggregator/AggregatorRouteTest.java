package com.camel.aggregator;

import com.camel.aggregator.dto.AggregatedResponse;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "bucket4j.enabled=false",
    "websocket.port=0",
    "app.admin.username=testadmin",
    "app.admin.password=testpassword"
})
class AggregatorRouteTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testAggregationRoute() {
        // We trigger the aggregation route
        AggregatedResponse response = producerTemplate.requestBody("direct:aggregate-data", null, AggregatedResponse.class);

        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        
        Map<String, Object> data = response.data();
        assertTrue(data.containsKey("id"));
        assertTrue(data.containsKey("orderId"));
        assertTrue(data.containsKey("stockCount"));
        
        assertEquals("user-123", data.get("id"));
        assertEquals("order-999", data.get("orderId"));
    }

    @Test
    void testProtectedEndpointRejectsUnauthenticated() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/camel/orders/summary"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
    }

    @Test
    void testProtectedEndpointAcceptsValidAdminCredentials() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/camel/orders/summary"))
                .header("Authorization", basicAuth("testadmin", "testpassword"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    void testProtectedEndpointRejectsInvalidCredentials() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/camel/orders/summary"))
                .header("Authorization", basicAuth("testadmin", "wrongpassword"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
    }
}
