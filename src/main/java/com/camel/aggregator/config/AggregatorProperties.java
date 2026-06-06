package com.camel.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {
    private List<Backend> backends;

    public List<Backend> getBackends() { return backends == null ? null : new java.util.ArrayList<>(backends); }
    public void setBackends(List<Backend> backends) { this.backends = backends == null ? null : new java.util.ArrayList<>(backends); }

    public static class Backend {
        private String name;
        private String endpoint;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }
}
