package com.camel.aggregator.service;

import com.camel.aggregator.model.Order;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkOrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong completedCount = new AtomicLong(0);

    public Order createOrder(Order order) {
        order.setId("ORD-" + UUID.randomUUID().toString().substring(0, 8));
        order.setStatus("PENDING");
        orders.put(order.getId(), order);
        return order;
    }

    public List<Order> getPendingBilling() {
        return orders.values().stream()
                .filter(o -> "PENDING".equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    public void billOrder(String id) {
        Order o = orders.get(id);
        if (o != null) o.setStatus("BILLED");
    }

    public List<Order> getPendingShipping() {
        return orders.values().stream()
                .filter(o -> "BILLED".equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    public void shipOrder(String id) {
        Order o = orders.remove(id); // Completely remove the order
        if (o != null) {
            o.setStatus("SHIPPED");
            completedCount.incrementAndGet();
            System.out.println("Order " + id + " has been shipped and removed from the system.");
        }
    }

    public Map<String, Long> getQueueSummary() {
        Map<String, Long> summary = new HashMap<>(orders.values().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting())));
        summary.put("SHIPPED", completedCount.get());
        return summary;
    }
}
