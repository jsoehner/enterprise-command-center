# ADR 0019: Security Defect Remediation: WebSocket Isolation, CSRF Mitigation, and Automated Pipeline Resilience

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-10-15

---

## 1. Context & Business Problem Statement

Following the comprehensive security posture evaluation of the Enterprise Command Center (documented in `docs/security/SECURITY_POSTURE_EVALUATION.md`), several security defects were identified across network access control, session management, business logic validation, and CI/CD supply chain governance:

1. **Unauthenticated WebSocket Broadcast (`SEC-01`)**: `AggregatorRoute.java` hardcoded binding to `0.0.0.0:8081`, bypassing Spring Security and exposing live aggregated business telemetry across all network interfaces.
2. **Unattended Auto-Merge with SCA Bypassed (`SEC-02`)**: `.github/workflows/nightly-dependency-update.yml` and `auto-manage-prs.yml` bypassed SCA with `-Ddependency-check.skip=true` and forced immediate squash merges with `|| gh pr merge --squash`, creating an unvetted supply chain merge vector.
3. **Order Pipeline Starvation / Head-of-Line Blocking (`SEC-03`)**: `WorkOrderService.java` enforced an overly restrictive regex `ORD-[A-Z0-9]{8}`, rejecting seeded 4-digit order IDs (`ORD-1001`, `ORD-1002`). In `AutomatedOrderProcessorService`, `ORD-1001` remained permanently at index 0 of `getPendingBilling()`, causing infinite retry loops and blocking all subsequent dynamic orders.
4. **CSRF Risk on Stateful REST APIs (`SEC-04`)**: `SecurityConfig.java` disabled CSRF on `/camel/**` without enforcing `SessionCreationPolicy.STATELESS`, creating an ambient cookie CSRF exposure when browser users authenticate via HTTP Basic.
5. **Hardcoded Fallback Credentials in Deployment Scripts (`SEC-05`)**: `docker-run.sh` and `docker-run.ps1` hardcoded `admin:admin123` defaults.

---

## 2. Decision Drivers

1. **Defense-in-Depth & Network Isolation**: Ensure default network listeners bind to secure loopback (`127.0.0.1`) unless explicitly configured for container networking.
2. **Stateless API Security**: Eliminate ambient session cookies to neutralize CSRF vulnerabilities on stateless REST endpoints.
3. **Queue Health & Validation Conformance**: Ensure input validation rules accommodate legitimate business identifiers (both seeded and UUID-derived) while blocking malformed inputs.
4. **Supply Chain Gatekeeping**: Guarantee that automated dependency updates cannot merge without passing OWASP Dependency-Check and branch protection status checks.
5. **Credential Discipline**: Abort execution when default or empty credentials are provided to launch scripts.

---

## 3. Decision Outcome

1. **WebSocket Host Configuration**:
   - Updated [`AggregatorRoute.java`](../../src/main/java/com/camel/aggregator/routes/AggregatorRoute.java) to dynamically inject `websocketHost` (`${websocket.host:127.0.0.1}`).
   - Added `websocket.host: ${WEBSOCKET_HOST:127.0.0.1}` in `application.yml` and `websocket.host: ${WEBSOCKET_HOST:0.0.0.0}` in `application-prod.yml`.

2. **Stateless Session Management**:
   - Configured `SessionCreationPolicy.STATELESS` in [`SecurityConfig.java`](../../src/main/java/com/camel/aggregator/config/SecurityConfig.java), guaranteeing that no `HttpSession` or ambient `JSESSIONID` cookie is created for API requests.

3. **Order ID Regex & Queue Validation**:
   - Broadened regex validation to `ORD-[A-Z0-9]{4,16}` in `WorkOrderService.java` for `createOrder()`, `billOrder()`, and `shipOrder()`.
   - Updated `WorkOrderServiceTest.java` to validate creation rejection of malformed IDs and boundary cases, unblocking the automated order processor.

4. **CI/CD Supply Chain Hardening**:
   - Added NVD cache restoration in `.github/workflows/nightly-dependency-update.yml` and `.github/workflows/pr-validation.yml`.
   - Removed `-Ddependency-check.skip=true` during build verification to enforce OWASP Dependency-Check.
   - Removed forced direct squash merge fallbacks (`|| gh pr merge --squash`), requiring required status checks to succeed before pull requests can merge.

5. **Deployment Script Credential Validation**:
   - Updated `docker-run.sh` and `docker-run.ps1` to require `ADMIN_PASSWORD` from the environment and terminate immediately if left empty or default (`admin123`).

---

## 4. Consequences & Verification

- **Positive**: Eliminates unauthenticated local network exposure of WebSocket telemetry.
- **Positive**: Neutralizes CSRF risk on all Camel REST endpoints by enforcing stateless authentication.
- **Positive**: Restores automated queue processing in `AutomatedOrderProcessorService`, eliminating head-of-line blocking.
- **Positive**: Closes the unvetted dependency auto-merge vulnerability in CI/CD.
- **Verification**:
  - `python3 scripts/adr_gatekeeper.py --reindex` and `python3 scripts/adr_gatekeeper.py --verify` pass with 100% compliance.
  - `python3 scripts/adr_gatekeeper.py --files src/main/java/com/camel/aggregator/config/SecurityConfig.java` passes without gating violations.
