# ADR 0013: User Entity Instantiation Hardening and Command Center Telemetry Layout Rebalancing

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Core Development Team
* **Date:** 2026-09-11

---

## 1. Context & Business Problem Statement

During containerized operations via `docker-run.sh`, users authenticating against the command center interface with default administrative credentials (`admin` / `admin123`) were trapped in an infinite browser Basic Authentication prompt loop.

Technical root-cause diagnosis revealed two contributing failure modes:

1. **JPA Entity Reflection Failure under Java 25**:
   The `User` entity (`com.camel.aggregator.model.User`) relied on Lombok's `@Data` and `@NoArgsConstructor` annotations for constructor and accessor generation. Due to compiler and annotation processing semantics under modern Java 25 toolchains and explicit overloaded constructors, Hibernate/JPA could not locate a default no-argument constructor at runtime during database user lookup:
   ```text
   org.springframework.security.authentication.InternalAuthenticationServiceException: No default constructor for entity: 'com.camel.aggregator.model.User'
       at org.hibernate.metamodel.internal.StandardEntityInstantiator.newInstance(StandardEntityInstantiator.java:28)
       at org.springframework.security.authentication.dao.DaoAuthenticationProvider.retrieveUser(...)
   ```
   This resulted in `DaoAuthenticationProvider` rejecting valid credentials with an HTTP 401 response containing `WWW-Authenticate: Basic realm="Realm"`. Because the command center frontend (`index.html`) embeds five independent iframes continuously polling protected endpoints every 2 to 3 seconds, the browser continuously prompted for credentials.

2. **Circular Error Dispatch Authentication**:
   When Spring Security encountered an authentication or authorization failure, the internal dispatch to `/error` was not explicitly permitted in `SecurityConfig`. This created secondary authentication challenges during error handling.

Additionally, in the operations dashboard layout:
- The **Status View** (`status.html`) suffered from visual crowding: the "Resilience Metrics" card consumed vertical layout space, forcing the "Backend Services" section to scroll vertically to display all five monitored services.
- The **Enterprise Command Center View** (`dashboard-view.html`) had unused horizontal capacity alongside the Kafka Activity Log.

---

## 2. Decision Drivers

1. **Deterministic Runtime Instantiation**: Eliminate reliance on compiler-sensitive bytecode generation for foundational security entities in Java 25 environments.
2. **Robust Authentication Workflow**: Ensure Spring Security error dispatches (`/error`) do not generate secondary authentication challenges.
3. **Automated Verification**: Introduce integration test coverage explicitly asserting Basic Authentication behavior (success with valid credentials, 401 rejection with invalid credentials, 401 on unauthenticated access) against protected endpoints.
4. **Ergonomic Operator Experience**: Optimize dashboard real estate so all five backend microservices (Inventory, Orders, Customers, Payments, Shipping) are visible simultaneously without vertical scrolling.

---

## 3. Decision Outcome

We decided to implement the following architectural and interface enhancements:

### 1. Explicit POJO Hardening in `User.java`
Declare explicit no-argument and all-argument constructors, as well as explicit getters and setters for all entity properties (`id`, `username`, `password`, `roles`), ensuring runtime reflection and JPA instantiation succeed independently of annotation processor behavior.

### 2. Error Endpoint Security Configuration
Explicitly include `/error` in the `permitAll()` list in `SecurityConfig.java` alongside `/login`, static assets (`*.html`, `*.css`, `*.js`), and Swagger/OpenAPI resources.

### 3. Basic Auth Test Coverage in `AggregatorRouteTest.java`
Integrate end-to-end integration tests using `TestRestTemplate` to validate that:
- Unauthenticated requests to protected endpoints (`/camel/orders/summary`) return HTTP 401 Unauthorized.
- Authenticated requests with valid credentials (`admin` / `admin123`) return HTTP 200 OK.
- Requests with invalid credentials return HTTP 401 Unauthorized.

### 4. Telemetry Layout Rebalancing
- Move the **Resilience Metrics** card from `status.html` into `dashboard-view.html`, organizing it in a responsive two-column grid (`.resilience-activity-grid`) adjacent to the Live Event Stream & Kafka Activity Log.
- Reconfigure `status.html`'s "Backend Services" into a responsive CSS grid (`grid-template-columns: repeat(auto-fit, minmax(220px, 1fr))`) occupying full width, completely eliminating the vertical scrollbar.

---

## 4. Consequences & Trade-Offs

### Positive Consequences
* **Deterministic Authentication**: Eliminates the infinite login prompt loop. Hibernate reliably instantiates `User` records from H2/AlloyDB/PostgreSQL across any Java runtime version.
* **No Unauthenticated Dispatch Deadlocks**: Error responses can be rendered cleanly without triggering cascaded basic authentication challenges.
* **Regression Protection**: Automated CI tests verify security endpoints against regressions.
* **Enhanced Observability UX**: Operators can monitor all five backend services at a glance without scrolling, while resilience telemetry is positioned directly beside live event and Kafka stream telemetry.

### Negative Consequences & Operational Costs
* **Explicit Boilerplate**: Explicit constructors and accessors in `User.java` require manual maintenance if new entity fields are added, slightly increasing lines of code compared to pure Lombok annotations.

---

## 5. Next Steps & Validation

- [x] Hardened `User.java` with explicit no-arg constructor, all-arg constructor, and accessors.
- [x] Updated `SecurityConfig.java` to permit `/error`.
- [x] Verified test suite passes: `mvn test` (10 passed, 0 failures, 0 errors).
- [x] Verified code quality: `mvn spotbugs:check` (0 bug patterns reported).
- [x] Rebuilt Docker container image and verified live HTTP authentication on port 8080.
- [x] Verified `status.html` and `dashboard-view.html` layout and responsive rendering.
- [x] Author ADR 0013 and update ADR index via `python3 scripts/adr_gatekeeper.py --reindex`.
- [x] Run ADR gatekeeper validation via `python3 scripts/adr_gatekeeper.py --verify`.
