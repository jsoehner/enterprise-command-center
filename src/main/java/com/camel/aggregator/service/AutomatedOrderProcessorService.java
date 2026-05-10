package com.camel.aggregator.service;

import com.camel.aggregator.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomatedOrderProcessorService {

    @Autowired
    private WorkOrderService workOrderService;

    // Run every 15 seconds
    @Scheduled(fixedRate = 15000)
    public void processPendingBilling() {
        List<Order> pending = workOrderService.getPendingBilling();
        if (!pending.isEmpty()) {
            // Process the first one
            Order toBill = pending.get(0);
            workOrderService.billOrder(toBill.getId());
            System.out.println("[AutoProcessor] Auto-billed order: " + toBill.getId());
        }
    }

    // Run every 20 seconds
    @Scheduled(fixedRate = 20000)
    public void processPendingShipping() {
        List<Order> readyToShip = workOrderService.getPendingShipping();
        if (!readyToShip.isEmpty()) {
            // Process the first one
            Order toShip = readyToShip.get(0);
            workOrderService.shipOrder(toShip.getId());
            
            // To ensure the queue doesn't grow infinitely and they are "removed",
            // we could remove them entirely, but keeping them as SHIPPED is good for the stats board.
            System.out.println("[AutoProcessor] Auto-shipped order: " + toShip.getId());
        }
    }
}
