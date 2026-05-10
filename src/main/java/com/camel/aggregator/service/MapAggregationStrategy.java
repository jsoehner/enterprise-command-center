package com.camel.aggregator.service;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.util.Map;
import java.util.HashMap;

public class MapAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }

        Map<String, Object> oldBody = oldExchange.getIn().getBody(Map.class);
        Map<String, Object> newBody = newExchange.getIn().getBody(Map.class);

        if (oldBody == null) {
            oldBody = new HashMap<>();
        }
        
        if (newBody != null) {
            oldBody.putAll(newBody);
        }

        oldExchange.getIn().setBody(oldBody);
        return oldExchange;
    }
}
