package com.camel.aggregator.dto;

import java.util.Map;

public record AggregatedResponse(
    String status,
    Map<String, Object> data,
    long timestamp
) {}
