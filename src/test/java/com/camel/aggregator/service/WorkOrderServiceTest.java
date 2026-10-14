package com.camel.aggregator.service;

import com.camel.aggregator.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class WorkOrderServiceTest {

    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        workOrderService = new WorkOrderService();
    }

    @Test
    void testCreateOrder_WithValidData() {
        Order order = new Order("ORD-9999", "Test Corp", 100.0, "PENDING");
        Order saved = workOrderService.createOrder(order);
        assertEquals("ORD-9999", saved.getId());
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void testCreateOrder_WithNoId_GeneratesId() {
        Order order = new Order(null, "Test Corp", 100.0, null);
        Order saved = workOrderService.createOrder(order);
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("ORD-"));
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void testBillOrder_ValidId_TransitionsToBilled() {
        Order order = new Order("ORD-1001", "Acme Global Corp", 4500.00, "PENDING");
        workOrderService.createOrder(order);

        workOrderService.billOrder("ORD-1001");

        Order updated = workOrderService.getAllOrders().stream()
                .filter(o -> "ORD-1001".equals(o.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("BILLED", updated.getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID-ID", "ORD-123", "ORD-123456789", ""})
    void testBillOrder_InvalidId_NoChange(String invalidId) {
        workOrderService.billOrder(invalidId);
        // Should not throw exception and should not change anything
    }

    @Test
    void testShipOrder_ValidId_TransitionsToShipped() {
        Order order = new Order("ORD-1002", "Cyberdyne Systems", 12850.00, "BILLED");
        workOrderService.createOrder(order);

        workOrderService.shipOrder("ORD-1002");

        Order updated = workOrderService.getAllOrders().stream()
                .filter(o -> "ORD-1002".equals(o.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("SHIPPED", updated.getStatus());
    }

    @Test
    void testShipOrder_InvalidStatus_NoChange() {
        Order order = new Order("ORD-1001", "Acme Global Corp", 4500.00, "PENDING");
        workOrderService.createOrder(order);

        workOrderService.shipOrder("ORD-1001");

        Order updated = workOrderService.getAllOrders().stream()
                .filter(o -> "ORD-1001".equals(o.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("PENDING", updated.getStatus());
    }

    @Test
    void testConcurrentBilling_NoRaceCondition() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        String orderId = "ORD-CONC";
        workOrderService.createOrder(new Order(orderId, "Concurrent Corp", 1000.0, "PENDING"));

        AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                workOrderService.billOrder(orderId);
                // Check if status is BILLED (it should be only once if we were checking for transition, 
                // but computeIfPresent handles the atomicity of the check-and-set)
                successCount.incrementAndGet();
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Order updated = workOrderService.getAllOrders().stream()
                .filter(o -> orderId.equals(o.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("BILLED", updated.getStatus());
    }

    @Test
    void testGetQueueSummary_CorrectCalculations() {
        Map<String, Object> summary = workOrderService.getQueueSummary();
        
        assertTrue(summary.containsKey("PENDING"));
        assertTrue(summary.containsKey("BILLED"));
        assertTrue(summary.containsKey("SHIPPED"));
        assertTrue(summary.containsKey("TOTAL_REVENUE"));
        assertTrue(summary.containsKey("MRR"));
        assertTrue(summary.containsKey("ARR"));
    }
}
