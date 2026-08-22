package com.camel.aggregator.routes;

import com.camel.aggregator.model.Order;
import org.apache.camel.builder.RouteBuilder;
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
            .to("bean:workOrderService?method=billOrder")
            .setBody(constant("Order billed successfully"));

        from("direct:process-shipping")
            .log("Processing shipping for order ${header.id}")
            .to("bean:workOrderService?method=shipOrder")
            .setBody(constant("Order shipped successfully"));
    }
}
