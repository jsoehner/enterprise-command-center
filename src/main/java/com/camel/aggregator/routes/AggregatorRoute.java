package com.camel.aggregator.routes;

import com.camel.aggregator.config.AggregatorProperties;
import com.camel.aggregator.dto.AggregatedResponse;
import com.camel.aggregator.service.MapAggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AggregatorRoute extends RouteBuilder {

    @Autowired
    private AggregatorProperties properties;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Override
    public void configure() throws Exception {
        
        // Kafka Event Bus Listener
        if (kafkaEnabled) {
            from("kafka:api-events?brokers={{kafka.brokers:localhost:9092}}&autoOffsetReset=latest")
                .routeId("kafka-event-listener")
                .log("Received refresh event from Kafka")
                .to("direct:aggregate-data");
        }

        // Mock Bridge for demo purposes (if Kafka is not running)
        from("direct:mock-kafka-events")
            .log("Simulating event from Kafka topic: api-events")
            .to("direct:aggregate-data");
        
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json)
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "REST API Aggregator")
            .apiProperty("api.version", "1.0.0");

        rest("/api")
            .get("/aggregated")
                .description("Aggregated data from all sources")
                .outType(AggregatedResponse.class)
                .routeId("api-aggregated")
                .to("direct:check-cache")
            
            .get("/health")
                .description("Status of backend circuit breakers")
                .routeId("api-health")
                .to("direct:get-health");

        from("direct:get-health")
            .routeId("get-health-route")
            .process(exchange -> {
                // In a real app, we'd query Resilience4j Registry
                // For this demo, we'll return mock health based on recent activity
                Map<String, String> health = Map.of(
                    "UserService", "UP",
                    "OrderService", "UP",
                    "InventoryService", "UP"
                );
                exchange.getIn().setBody(health);
            });

        from("direct:check-cache")
            .routeId("check-cache-route")
            .bean("cacheService", "get('aggregated_data')")
            .choice()
                .when(body().isNotNull())
                    .log("Serving from cache")
                .otherwise()
                    .to("direct:aggregate-data")
                    .bean("cacheService", "put('aggregated_data', ${body})")
            .end();

        from("direct:aggregate-data")
            .routeId("aggregate-data-route")
            .recipientList(constant(
                properties.getBackends().stream()
                    .map(AggregatorProperties.Backend::getEndpoint)
                    .collect(Collectors.joining(","))
            )).aggregationStrategy(new MapAggregationStrategy())
                .parallelProcessing()
            .end()
            .process(exchange -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> aggregatedData = exchange.getIn().getBody(Map.class);
                AggregatedResponse response = new AggregatedResponse(
                    "SUCCESS",
                    aggregatedData,
                    System.currentTimeMillis()
                );
                exchange.getIn().setBody(response);
            })
            .wireTap("direct:broadcast-update");

        from("direct:broadcast-update")
            .marshal().json()
            .to("vertx-websocket:0.0.0.0:8081/aggregated-updates?sendToAll=true");

        // Start the WebSocket server by having at least one consumer
        from("vertx-websocket:0.0.0.0:8081/aggregated-updates")
            .log("New WebSocket client connected: ${header.connectionKey}")
            .routeId("websocket-server-start");

        from("direct:fetch-user")
            .routeId("fetch-user-route")
            .circuitBreaker()
                .resilience4jConfiguration().timeoutEnabled(true).timeoutDuration(2000).end()
                .bean("externalApiService", "getUserData")
            .onFallback()
                .setBody(constant(Map.of("user", "unavailable")))
            .end();

        from("direct:fetch-order")
            .routeId("fetch-order-route")
            .circuitBreaker()
                .resilience4jConfiguration().timeoutEnabled(true).timeoutDuration(2000).end()
                .bean("externalApiService", "getOrderData")
            .onFallback()
                .setBody(constant(Map.of("order", "unavailable")))
            .end();

        from("direct:fetch-inventory")
            .routeId("fetch-inventory-route")
            .circuitBreaker()
                .resilience4jConfiguration().timeoutEnabled(true).timeoutDuration(2000).end()
                .bean("externalApiService", "getInventoryData")
            .onFallback()
                .setBody(constant(Map.of("inventory", "unavailable")))
            .end();
    }
}
