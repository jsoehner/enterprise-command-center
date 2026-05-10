package com.camel.aggregator;

import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CamelAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamelAggregatorApplication.class, args);
    }
}
