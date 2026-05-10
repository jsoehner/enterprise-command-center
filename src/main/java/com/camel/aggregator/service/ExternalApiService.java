package com.camel.aggregator.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class ExternalApiService {

    private String name = "John Doe";
    private String status = "SHIPPED";
    private int stockCount = 42;
    private double amount = 125.50;

    public Map<String, Object> getUserData() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", "user-123");
        data.put("name", name);
        data.put("email", name.toLowerCase().replace(" ", ".") + "@example.com");
        return data;
    }

    public Map<String, Object> getOrderData() {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", "order-999");
        data.put("amount", amount);
        data.put("status", status);
        return data;
    }

    public Map<String, Object> getInventoryData() {
        Map<String, Object> data = new HashMap<>();
        data.put("stockCount", stockCount);
        data.put("warehouse", "Central-01");
        return data;
    }

    // Update methods for the generator
    public void modifyName(String name) { this.name = name; }
    public void modifyStatus(String status) { this.status = status; }
    public void modifyStock(int stock) { this.stockCount = stock; }
    public void modifyAmount(double amount) { this.amount = amount; }
}
