# Enterprise Command Center (REST API Aggregator)

[![Build & Test](https://github.com/jsoehner/enterprise-command-center/actions/workflows/ci.yml/badge.svg)](https://github.com/jsoehner/enterprise-command-center/actions)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Camel](https://img.shields.io/badge/Apache%20Camel-4.22.0-orange.svg)](https://camel.apache.org/)
[![Java](https://img.shields.io/badge/Java-25%2B-blue.svg)](https://www.oracle.com/java/)
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
| **Integration Engine** | Apache Camel 4.22.0 (REST DSL & EIP Aggregator) |
| **Database** | PostgreSQL (Prod) & In-Memory H2 (Dev / Test) |
| **Caching & Rate Limiting** | Bucket4j 8.10.1, Caffeine Cache & Redis |
| **Event Streaming** | Apache Kafka 3.9.2 |
| **Resilience & SLA** | Resilience4j Circuit Breakers & Micrometer Actuator |
| **Security & Auth** | Spring Security 7.1.0, HTTP Basic Auth, CSP & HSTS |
| **Frontend Architecture** | Vanilla HTML5, CSS Design Tokens, WebSockets, Vanilla JS |
| **CI/CD & Security** | GitHub Actions (Node 24, Commit SHA Pinning), Gitleaks, SpotBugs 4.10.4, OWASP 13.0.0 |
| **Containerization** | Multi-stage Dockerfile (Temurin JDK 25 build + Chainguard JRE) |

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
* **[ADR 0002: Post-Quantum Cryptography Roadmap](docs/adr/0002-post-quantum-crypto-ml-kem-merkle-trees.md)** — *(Superseded by ADR 0010)* Strategic roadmap for ML-KEM-768 and Merkle Tree Certificates.
* **[ADR 0003: Enterprise Command Center Architecture](docs/adr/0003-enterprise-business-command-center-architecture.md)** — Unified multi-department business lifecycle pipeline and financial intelligence.
* **[ADR 0004: GitHub Actions Node 24 & SHA Pinning](docs/adr/0004-github-actions-node24-sha-pinning.md)** — CI/CD automation strategy with Node 24 runtime support and immutable 40-character SHA security pinning.
* **[ADR 0005: Java 25 Runtime & CI Secrets Modernization](docs/adr/0005-java-25-runtime-and-ci-secrets-modernization.md)** — Java 25 runtime baseline, SpotBugs SAST, and resilient GitHub Actions container publishing.
* **[ADR 0006: Dependency Security Hardening](docs/adr/0006-dependency-security-hardening.md)** — Identification and mitigation of known vulnerabilities in the dependency tree.
* **[ADR 0007: Docker Deployment with Env Injection](docs/adr/0007-docker-deployment-with-env-injection.md)** — Container lifecycle management and secure environment variable injection.
* **[ADR 0008: Spring Security with BCrypt & H2 Init](docs/adr/0008-spring-security-h2-init.md)** — BCrypt password hashing and zero-config in-memory database seeding.
* **[ADR 0009: ADR Gatekeeper & Architecture Governance](docs/adr/0009-adr-gatekeeper-and-architecture-governance.md)** — Automated ASR significance analysis, CI/CD PR gatekeeping, pre-commit hook, and JSON decision registry.
* **[ADR 0010: Post-Quantum Cryptography Implementation](docs/adr/0010-post-quantum-cryptography-ml-kem-implementation.md)** — NIST FIPS 203 ML-KEM-768 key encapsulation with Bouncy Castle 1.85 and Camel REST telemetry.
* **[ADR 0011: Repository Hygiene & Git Governance](docs/adr/0011-repository-hygiene-and-git-governance.md)** — Multi-ecosystem .gitignore architecture, artifact sanitization, and knowledge archiving.

See the complete index in [docs/adr/README.md](docs/adr/README.md).

---

## 📖 Developer Documentation & Knowledge Base

- **[Developer Guide](docs/DEVELOPER_GUIDE.md)** — Local setup, Docker workflow, testing, and ADR governance commands.
- **[Frequently Asked Questions (FAQ)](docs/FAQ.md)** — Common questions regarding authentication, rate limiting, and design customization.
- **[Lessons Learned](docs/lessons-learned/)** — Architectural and operational insights on dependency automation and code cleanup.

---

## 🤝 Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feat/your-feature-name`).
3. Verify test suite and ADR gatekeeper (`mvn clean test && python3 scripts/adr_gatekeeper.py --verify`).
4. Commit your changes following conventional commits (`git commit -m 'feat: Add your feature'`).
5. Push to the branch (`git push origin feat/your-feature-name`).
6. Open a Pull Request.

---
*Built with ❤️, Apache Camel, and Spring Boot.*
