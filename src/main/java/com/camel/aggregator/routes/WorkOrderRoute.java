package com.camel.aggregator.routes;

import com.camel.aggregator.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        rest("/orders")
            .get("/summary")
                .to("bean:workOrderService?method=getQueueSummary")
            
            .post("/create")
                .type(Order.class)
                .to("bean:workOrderService?method=createOrder")
            
            .get("/billing")
                .to("bean:workOrderService?method=getPendingBilling")
            
            .post("/billing/{id}/bill")
                .to("direct:process-billing")
            
            .get("/shipping")
                .to("bean:workOrderService?method=getPendingShipping")
            
            .post("/shipping/{id}/ship")
                .to("direct:process-shipping");

        rest("/health")
            .get("/status")
                .to("bean:serviceHealthMonitor?method=getAllStatuses");

        from("direct:process-billing")
            .log("Processing billing for order ${header.id}")
            .bean("workOrderService", "billOrder(${header.id})")
            .setBody(constant("Order billed successfully"));

        from("direct:process-shipping")
            .log("Processing shipping for order ${header.id}")
            .bean("workOrderService", "shipOrder(${header.id})")
            .setBody(constant("Order shipped successfully"));
    }
}
