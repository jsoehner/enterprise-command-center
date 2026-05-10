package com.camel.aggregator.service;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;

@Service
@EnableScheduling
public class DataGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(DataGeneratorService.class);

    @Autowired
    private ExternalApiService apiService;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private WorkOrderService workOrderService;

    private final Random random = new java.security.SecureRandom();
    private final String[] names = {"John Doe", "Jane Smith", "Alice Johnson", "Bob Brown", "Charlie Davis"};
    private final String[] statuses = {"SHIPPED", "PENDING", "PROCESSING", "DELIVERED", "CANCELLED"};

    @Scheduled(fixedRate = 5000)
    public void generateData() {
        log.info("[DataGenerator] Generating new dataset...");
        
        // Randomly update values
        apiService.modifyName(names[random.nextInt(names.length)]);
        apiService.modifyStatus(statuses[random.nextInt(statuses.length)]);
        apiService.modifyStock(10 + random.nextInt(90));
        apiService.modifyAmount(50 + (1000 * random.nextDouble()));

        // Send a trigger to our mock Kafka bridge
        producerTemplate.sendBody("direct:mock-kafka-events", "REFRESH");

        // Randomly generate new orders (100% chance per tick) to populate the queue
        com.camel.aggregator.model.Order newOrder = new com.camel.aggregator.model.Order();
        newOrder.setCustomer(names[random.nextInt(names.length)] + " Corp");
        newOrder.setAmount(100 + (1000 * random.nextDouble()));
        workOrderService.createOrder(newOrder);
        log.info("[DataGenerator] Created new order for {}", newOrder.getCustomer());
    }
}
