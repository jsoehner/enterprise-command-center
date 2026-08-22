# Frequently Asked Questions (FAQ)

## General & Architecture

### 1. What is the Enterprise Command Center?
The Enterprise Command Center is a high-performance Backend-for-Frontend (BFF) and API Aggregator built with Apache Camel and Spring Boot 3.5. It integrates disparate enterprise microservices into a unified pipeline spanning client order intake, automated billing & settlement, freight shipping, and real-time financial telemetry.

### 2. How do the 5 dashboard panes communicate?
The main command grid (`/` or `index.html`) embeds modular views via iframes. Each departmental view interacts with Apache Camel REST endpoints (`/camel/orders/*`, `/camel/api/*`, `/camel/health/*`) using asynchronous JSON fetch requests and WebSocket event listeners.

---

## Authentication & Security

### 3. What are the default administrator credentials?
When booting in standalone/local mode without custom environment variables, default credentials are initialized:
* **Username**: `admin`
* **Password**: `admin123`

To override in production:
```bash
export ADMIN_USERNAME="your-custom-admin"
export ADMIN_PASSWORD="your-secure-password"
```

### 4. What browser security headers are enforced?
All responses include:
* `Content-Security-Policy`: Restricts scripts, fonts, and WebSocket endpoints to trusted origins.
* `X-Frame-Options: SAMEORIGIN`: Allows legitimate same-origin iframe embedding in `index.html` while preventing clickjacking.
* `X-Content-Type-Options: nosniff`: Prevents MIME-sniffing attacks.
* `Strict-Transport-Security (HSTS)`: Enforces HTTPS in production deployments.

---

## Operations & Data Lifecycle

### 5. How does the order lifecycle work?
Orders progress through three distinct transactional states:
1. **`PENDING`**: Created via the CRM gateway (`/entry.html`) or seeded on startup. Awaits invoice settlement.
2. **`BILLED`**: Authorized and settled in the Billing department (`/billing.html`). Awaits warehouse dispatch.
3. **`SHIPPED`**: Handed off to freight carriers in Shipping (`/shipping.html`). Increments completed counters and finalized SLA metrics.

### 6. How are financial metrics (MRR, ARR, LTV/CAC) calculated?
`WorkOrderService` computes metrics live on every state change:
* **Total Pipeline Revenue**: Sum of all active contract values.
* **MRR (Monthly Recurring Revenue)**: Calculated based on recurring contract baseline.
* **ARR (Annual Recurring Revenue)**: `MRR * 12`.
* **Unit Economics**: `LTV / CAC` ratio tracking customer acquisition payback efficiency.

---

## Troubleshooting & Diagnostics

### 7. Why do I see a 401 Unauthorized error?
Ensure your request includes HTTP Basic Authentication headers or that your browser session is authenticated. You can authenticate against `http://localhost:8080` with `admin` / `admin123`.

### 8. Can I run the application without external Redis or Kafka?
Yes. The application defaults to resilient local fallbacks (`Caffeine` cache and in-memory test drivers) when `REDIS_HEALTH_ENABLED=false` and `BUCKET4J_ENABLED=false`.

### 9. How do I inspect the Design System tokens and components?
Visit **`/design-system.html`** in your browser. It includes interactive color palette swatches, typography scales, atomic button states, status badges, and a live dark/light mode toggle.
