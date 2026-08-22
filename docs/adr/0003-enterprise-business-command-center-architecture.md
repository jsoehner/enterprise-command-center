# ADR 0003: Enterprise Business Command Center Architecture and Lifecycle Pipeline

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Core Development Team
* **Date:** 2026-08-22

---

## 1. Context & Business Problem Statement

The application initially served as a developer prototype for aggregating external microservices and processing synthetic orders. To evolve into an operational enterprise business command center, the platform required:
1. **Multi-Department Operational Lifecycle**: Unified, real-time tracking across client intake (CRM / Order Entry), accounts receivable (Billing & Settlement), and logistics (Shipping & Fulfillment).
2. **Executive Financial Intelligence**: Real-time business metrics including Monthly Recurring Revenue (MRR), Annual Recurring Revenue (ARR), Contract Pipeline Value, and SaaS unit economics (LTV/CAC ratio, Gross Margin %, Payback period).
3. **Microservice Resilience & Observability**: Clear SLA metrics, circuit breaker thresholds (Resilience4j), and continuous WebSocket event logging.

---

## 2. Decision Drivers

* **Operational Visibility**: End-to-end visibility into order progression (`PENDING` ➔ `BILLED` ➔ `SHIPPED`).
* **Financial Intelligence**: Dynamic aggregation of contract value and executive SaaS performance indicators.
* **Component Reusability & WCAG Compliance**: Adherence to the established Design System tokens, atomic UI components, and accessible interactions.
* **Service Decoupling & Reactive Integration**: Leveraging Apache Camel 4.x for clean REST endpoint routing and in-memory transactional state management.

---

## 3. Considered Options

### Option 1: Fragmented Standalone Web Pages
Build independent, disconnected HTML views for each department without a unified backend aggregator or shared state.

* **Good, because**: Fast to prototype in isolation.
* **Bad, because**: Causes data drift, lacks real-time financial telemetry, and prevents holistic order lifecycle tracing.

### Option 2: Unified REST & Camel-Driven Command Center Architecture (Chosen)
Implement a centralized in-memory service (`WorkOrderService`), exposed via Apache Camel REST DSL (`WorkOrderRoute`), backed by real-time reactive dashboards (`dashboard-view.html`, `entry.html`, `billing.html`, `shipping.html`, `queue.html`, `status.html`).

* **Good, because**: Provides continuous cross-department state consistency, unified SaaS metric calculations, and automated event tracking.
* **Bad, because**: Requires maintaining API contracts and synchronization across multiple static view controllers.

---

## 4. Decision Outcome

Chosen Option: **Option 2 (Unified REST & Camel-Driven Command Center Architecture)**.

### Architecture Overview

```mermaid
flowchart LR
    subgraph Frontend["Enterprise UI Views"]
        Dashboard["Dashboard View\n(Executive KPIs)"]
        Entry["Order Entry\n(Client Intake)"]
        Billing["Billing Operations\n(Invoice Settlement)"]
        Shipping["Shipping Fulfillment\n(Logistics Dispatch)"]
        Queue["Queue Telemetry\n(Pipeline Counters)"]
        Status["Service Resilience\n(Circuit Breakers)"]
    end

    subgraph Camel["Apache Camel 4.x REST DSL"]
        Routes["WorkOrderRoute\n(/camel/orders/*)"]
    end

    subgraph Service["Backend Business Engine"]
        WOService["WorkOrderService\n(State Machine & Financial KPI Calculator)"]
    end

    Entry -->|POST /create| Routes
    Billing -->|POST /{id}/bill| Routes
    Shipping -->|POST /{id}/ship| Routes
    Dashboard & Queue & Status -->|GET /summary & /all| Routes

    Routes --> WOService
```

---

## 5. Consequences & Trade-Offs

### Positive Consequences
* **Full Lifecycle Traceability**: Orders transition systematically through verified states (`PENDING` ➔ `BILLED` ➔ `SHIPPED`).
* **Real-Time Financial Intelligence**: Executive summary endpoints compute MRR, ARR, LTV/CAC, and gross margin live on every state change.
* **Interactive Operations**: Frontends provide one-click actions for invoice settlement, order dispatch, and carrier handoffs.
* **Consistent Design & Accessibility**: All operational pages consume tokens from `styles.css` with semantic variables and WCAG 2.1 AA focus rings.

### Negative Consequences / Trade-Offs
* **In-Memory Storage**: Active orders reside in concurrent memory in `WorkOrderService` (designed for high-throughput aggregation; durable PostgreSQL persistence needed for cold archival).
* **Polling Overhead**: Client views utilize periodic fetch intervals; future enhancements will fully stream updates over WebSockets.

---

## 6. Implementation & Validation Checklist

- [x] Implement in-memory enterprise state machine in [`WorkOrderService.java`](file:///home/jsoehner/enterprise-command-center/src/main/java/com/camel/aggregator/service/WorkOrderService.java)
- [x] Expose REST endpoints in [`WorkOrderRoute.java`](file:///home/jsoehner/enterprise-command-center/src/main/java/com/camel/aggregator/routes/WorkOrderRoute.java)
- [x] Build and style [`entry.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/entry.html), [`billing.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/billing.html), [`shipping.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/shipping.html), [`queue.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/queue.html), [`status.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/status.html), and [`dashboard-view.html`](file:///home/jsoehner/enterprise-command-center/src/main/resources/static/dashboard-view.html)
- [x] Run and pass automated unit/integration test suite (`mvn test`)
