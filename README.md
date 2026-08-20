# REST API Aggregator

A high-performance **Backend-for-Frontend (BFF)** and **REST API Aggregator** built with Apache Camel, Spring Boot, and a defense-in-depth security posture.

This project serves as a centralized gateway to aggregate multiple downstream services, providing a unified, resilient, and secure interface for frontend applications.

## 🚀 Key Features

- **API Aggregation**: Orchestrates complex requests across multiple microservices using Apache Camel.
- **Resilience**: Implements circuit breakers and retries via Resilience4j.
- **Caching**: High-speed data retrieval using Caffeine and Redis.
- **Security**: Hardened with CSP, HSTS, and X-Content-Type-Options headers.
- **Scalability**: Production-ready with PostgreSQL for persistence and Kafka for event streaming.
- **Observability**: Integrated with Micrometer, Prometheus, and Zipkin for distributed tracing.

## 🛠 Tech Stack

| Component | Technology |
| --- | --- |
| **Framework** | Spring Boot 3.5.15 |
| **Integration** | Apache Camel 4.10.0 |
| **Database** | PostgreSQL |
| **Cache** | Redis & Caffeine |
| **Messaging** | Apache Kafka |
| **Security** | Spring Security |
| **Build Tool** | Maven |
| **Language** | Java 21 |

## 📋 Prerequisites

Before running the application, ensure you have the following installed:
- JDK 21
- Maven 3.9+
- PostgreSQL instance
- Redis instance

## 🚀 Quick Start

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd rest-api-aggregator
   ```

2. **Configure Environment**:
   Create an `application.yml` or set environment variables for:
   - `app.admin.username`
   - `app.admin.password`
   - `spring.datasource.url` (PostgreSQL)
   - `spring.redis.host`

3. **Build and Run**:
   ```bash
   mvn clean package -DskipTests
   java -jar target/rest-api-aggregator-0.9.12-SNAPSHOT.jar
   ```

## 🛡 Security & Architecture

This project follows a **defense-in-depth** security strategy. 

### Architectural Decision Records (ADRs)
We document all major architectural decisions in the `docs/adr/` directory.
- **ADR 0001**: [Production Security Hardening](docs/adr/0001-production-security-hardening.md) - Details the transition to environment-variable-based secrets, PostgreSQL persistence, and mandatory security headers.

## 🤝 Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---
*Built with ❤️ and Apache Camel.*
