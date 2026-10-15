package com.camel.aggregator.service;

import com.camel.aggregator.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkOrderService {
    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);
    
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong completedCount = new AtomicLong(0);

    public WorkOrderService() {
        // Seed initial enterprise business data
        createOrder(new Order("ORD-1001", "Acme Global Corp", 4500.00, "PENDING"));
        createOrder(new Order("ORD-1002", "Cyberdyne Systems", 12850.00, "BILLED"));
        createOrder(new Order("ORD-1003", "Stark Industries", 24900.00, "PENDING"));
        createOrder(new Order("ORD-1004", "Wayne Enterprises", 8750.00, "PENDING"));
    }

    public Order createOrder(Order order) {
        if (order.getId() == null || order.getId().trim().isEmpty()) {
            order.setId("ORD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else if (!order.getId().matches("ORD-[A-Z0-9]{4,16}")) {
            throw new IllegalArgumentException("Invalid Order ID format: " + order.getId() + ". Expected format 'ORD-[A-Z0-9]{4,16}'.");
        }
        if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
            order.setStatus("PENDING");
        }
        orders.put(order.getId(), order);
        return order;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> getPendingBilling() {
        return orders.values().stream()
                .filter(o -> "PENDING".equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    public void billOrder(@org.apache.camel.Header("id") String id) {
        if (id == null || !id.matches("ORD-[A-Z0-9]{4,16}")) {
            log.error("Invalid or null Order ID provided for billing: {}", id);
            return;
        }
        orders.computeIfPresent(id, (key, o) -> {
            if ("PENDING".equals(o.getStatus())) {
                o.setStatus("BILLED");
                log.info("Order {} transitioned to BILLED state", id);
            }
            return o;
        });
    }

    public List<Order> getPendingShipping() {
        return orders.values().stream()
                .filter(o -> "BILLED".equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    public void shipOrder(@org.apache.camel.Header("id") String id) {
        if (id == null || !id.matches("ORD-[A-Z0-9]{4,16}")) {
            log.error("Invalid or null Order ID provided for shipping: {}", id);
            return;
        }
        orders.computeIfPresent(id, (key, o) -> {
            if ("BILLED".equals(o.getStatus())) {
                o.setStatus("SHIPPED");
                completedCount.incrementAndGet();
                log.info("Order {} has been shipped and completed.", id);
            }
            return o;
        });
    }

    public Map<String, Object> getQueueSummary() {
        Map<String, Object> summary = new HashMap<>();
        long pending = orders.values().stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long billed = orders.values().stream().filter(o -> "BILLED".equals(o.getStatus())).count();
        long shipped = orders.values().stream().filter(o -> "SHIPPED".equals(o.getStatus())).count() + completedCount.get();

        double totalRevenue = orders.values().stream()
                .mapToDouble(Order::getAmount)
                .sum();

        summary.put("PENDING", pending);
        summary.put("BILLED", billed);
        summary.put("SHIPPED", shipped);
        summary.put("TOTAL_COUNT", orders.size());
        summary.put("TOTAL_REVENUE", totalRevenue);
        summary.put("MRR", totalRevenue * 0.85);
        summary.put("ARR", totalRevenue * 0.85 * 12);
        summary.put("CAC", 450.00);
        summary.put("LTV", 2700.00);
        summary.put("GROSS_MARGIN_PCT", 76.5);
        return summary;
    }
}
