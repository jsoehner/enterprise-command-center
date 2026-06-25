package com.camel.aggregator;

import com.camel.aggregator.dto.AggregatedResponse;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"bucket4j.enabled=false"})
class AggregatorRouteTest {

    @Autowired
    private ProducerTemplate producerTemplate;


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
}
