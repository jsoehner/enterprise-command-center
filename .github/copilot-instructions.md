# REST API Aggregator - Apache Camel Project

## Overview
Apache Camel + Spring Boot 3.x project that aggregates multiple REST APIs into a unified backend-for-fronted interface with Redis caching.

## Tech Stack
- Java 17, Maven
- Spring Boot 3.2.4
- Apache Camel 4.6.0
- Redis (caching)

## Build & Run
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

## Key Conventions
- Use Java DSL for Camel routes
- Routes live under `src/main/java/com/camel/aggregator/routes/`
- DTOs under `dto/`, services under `service/`
- Redis caching configured in `application.yml`
