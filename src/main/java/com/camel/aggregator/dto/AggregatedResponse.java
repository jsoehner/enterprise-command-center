package com.camel.aggregator.dto;

import java.util.Map;

public record AggregatedResponse(
    String status,
    Map<String, Object> data,
    long timestamp
) {
    public AggregatedResponse {
        data = data == null ? null : new java.util.HashMap<>(data);
    }
    
    @Override
    public Map<String, Object> data() {
        return data == null ? null : java.util.Collections.unmodifiableMap(data);
    }
}
