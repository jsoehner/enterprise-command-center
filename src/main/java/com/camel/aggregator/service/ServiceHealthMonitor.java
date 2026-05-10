package com.camel.aggregator.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class ServiceHealthMonitor {
    
    private final Map<String, String> serviceStatus = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    public ServiceHealthMonitor() {
        serviceStatus.put("User API", "UP");
        serviceStatus.put("Order Engine", "UP");
        serviceStatus.put("Inventory System", "UP");
        serviceStatus.put("Billing Gateway", "UP");
        serviceStatus.put("Kafka Bus", "UP");
    }
    
    public Map<String, String> getAllStatuses() {
        return serviceStatus;
    }
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds, potentially toggle a service
    public void simulateOutages() {
        String[] services = serviceStatus.keySet().toArray(new String[0]);
        String targetService = services[random.nextInt(services.length)];
        
        // 30% chance to flip status
        if (random.nextDouble() < 0.3) {
            String current = serviceStatus.get(targetService);
            String newStatus = "UP".equals(current) ? "DOWN" : "UP";
            serviceStatus.put(targetService, newStatus);
            System.out.println("[HealthMonitor] " + targetService + " is now " + newStatus);
        } else {
            // Self-healing: if down, bring it back up eventually
            if ("DOWN".equals(serviceStatus.get(targetService))) {
                serviceStatus.put(targetService, "UP");
                System.out.println("[HealthMonitor] " + targetService + " auto-recovered to UP");
            }
        }
    }
}
