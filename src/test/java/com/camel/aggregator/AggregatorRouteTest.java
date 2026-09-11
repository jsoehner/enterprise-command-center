package com.camel.aggregator;

import com.camel.aggregator.dto.AggregatedResponse;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    @Autowired
    private TestRestTemplate restTemplate;

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
    void testProtectedEndpointRejectsUnauthenticated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/camel/orders/summary", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testProtectedEndpointAcceptsValidAdminCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("testadmin", "testpassword")
                .getForEntity("/camel/orders/summary", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testProtectedEndpointRejectsInvalidCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("testadmin", "wrongpassword")
                .getForEntity("/camel/orders/summary", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
