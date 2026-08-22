# Enterprise Command Center (REST API Aggregator)

[![Build & Test](https://github.com/jsoehner/enterprise-command-center/actions/workflows/ci.yml/badge.svg)](https://github.com/jsoehner/enterprise-command-center/actions)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Camel](https://img.shields.io/badge/Apache%20Camel-4.10.0-orange.svg)](https://camel.apache.org/)
[![Java](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A high-performance **Enterprise Command Center** and **REST API Aggregator** built with Apache Camel, Spring Boot 3.5, and a defense-in-depth security posture.

This platform unifies downstream microservices, real-time business telemetry, automated billing & settlement, freight logistics fulfillment, and circuit-breaker resilience into an interactive, high-density command interface.

---

## 🚀 Key Features

* **Real-Time Executive Intelligence**: Live tracking of Monthly Recurring Revenue (MRR), Annual Recurring Revenue (ARR), Contract Pipeline Value, and SaaS unit economics (LTV/CAC ratio, Gross Margin %, Payback period).
* **Client Intake & Order Entry (CRM Gateway)**: Instantaneous work order creation and pipeline dispatching via Camel REST DSL.
* **Billing & Accounts Receivable**: Automated invoice generation, real-time settlement authorizations, and dunning state management.
* **Shipping & Logistics Fulfillment**: Freight carrier dispatch triggers, tracking manifest generation, and completion tracking.
* **Microservice Resilience & Telemetry**: Resilience4j circuit breakers, timeout isolation, and live service health monitoring.
* **Comprehensive Design System**: WCAG 2.1 Level AA compliant atomic component architecture with slate tokens, glassmorphic cards, and light/dark theme switching.
* **Post-Quantum Security Roadmap**: Architectural strategy for hybrid ML-KEM-768 key encapsulation and Merkle Tree Certificates (MTCs).

---

## 🛠 Tech Stack

| Domain | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.5.16 |
| **Integration Engine** | Apache Camel 4.10.0 (REST DSL & EIP Aggregator) |
| **Database** | PostgreSQL (Prod) & In-Memory H2 (Dev / Test) |
| **Caching & Rate Limiting** | Bucket4j, Caffeine Cache & Redis |
| **Event Streaming** | Apache Kafka |
| **Resilience & SLA** | Resilience4j Circuit Breakers & Micrometer Actuator |
| **Security & Auth** | Spring Security 6.x, HTTP Basic Auth, CSP & HSTS |
| **Frontend Architecture** | Vanilla HTML5, CSS Design Tokens, WebSockets, Vanilla JS |
| **Containerization** | Multi-stage Dockerfile (Temurin JDK 21 build + Chainguard JRE) |

---

## 📋 Interactive Views & Dashboards

The web interface is modular and accessible at the following endpoints:

| View | Path | Description |
| :--- | :--- | :--- |
| **Executive Command Grid** | `/` (`index.html`) | 5-pane high-density multi-view dashboard optimized for 80% screen zoom. |
| **Executive Dashboard** | `/dashboard-view.html` | Real-time MRR/ARR metrics, aggregated customer stream, and Kafka activity log. |
| **Order Intake & CRM** | `/entry.html` | Client organization intake and enterprise work order dispatching. |
| **Billing & Accounts Receivable** | `/billing.html` | Interactive invoice settlement and payment processing board. |
| **Shipping & Logistics** | `/shipping.html` | Freight carrier dispatch and fulfillment manifest management. |
| **Queue & Telemetry** | `/queue.html` | 3-stage visual order counters and financial velocity tracking. |
| **Resilience Status** | `/status.html` | Microservice health checks and circuit breaker SLA telemetry. |
| **Design System Showcase** | `/design-system.html` | Interactive design token swatches, component sandboxes, and theme switcher. |

---

## 🚀 Quick Start

### 1. Run with Docker (Recommended)
```bash
# Clone the repository
git clone https://github.com/jsoehner/enterprise-command-center.git
cd enterprise-command-center

# Build and run the container
./docker-run.sh
```
Open your browser at **`http://localhost:8080`** (Default credentials: `admin` / `admin123`).

### 2. Local Maven Development
```bash
# Run unit & integration tests
mvn test

# Start the Spring Boot application
mvn spring-boot:run
```

---

## 🛡 Security & Architecture

### Architectural Decision Records (ADRs)
All architectural decisions are documented under [`docs/adr/`](docs/adr/):
* **[ADR 0001: Production Security Hardening](docs/adr/0001-production-security-hardening.md)** — Environment variable secrets, PostgreSQL profile, and browser security headers.
* **[ADR 0002: Post-Quantum Cryptography Roadmap](docs/adr/0002-post-quantum-crypto-ml-kem-merkle-trees.md)** — Roadmap for ML-KEM-768 key encapsulation and Merkle Tree Certificates (MTCs).
* **[ADR 0003: Enterprise Command Center Architecture](docs/adr/0003-enterprise-business-command-center-architecture.md)** — Unified multi-department business lifecycle pipeline and financial intelligence.

See the complete index in [docs/adr/README.md](docs/adr/README.md).

---

## ❓ Frequently Asked Questions (FAQ)

Refer to **[FAQ.md](docs/FAQ.md)** for common questions regarding setup, authentication, rate limiting, and design customization.

---

## 🤝 Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'feat: Add AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---
*Built with ❤️, Apache Camel, and Spring Boot.*
