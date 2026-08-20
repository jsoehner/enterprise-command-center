# Phase 8: FP Check + Cold Verification

## Findings Reviewed
1. **Non-Atomic State Transitions (WorkOrderService)**
   - **Severity**: MEDIUM
   - **Description**: `billOrder` and `shipOrder` perform non-atomic "get-modify" operations on the `ConcurrentHashMap`.
   - **Verdict**: VALID
   - **Evidence**: `WorkOrderService.java` lines 31-34 and 45-51. `orders.get(id)` followed by `o.setStatus(...)` is not atomic.
   - **Remediation**: Use `orders.computeIfPresent(id, (k, v) -> { ... })` or a `ReentrantLock` per order.

2. **Permissive Security Config (Legacy)**
   - **Severity**: HIGH
   - **Description**: Previous configuration allowed `permitAll()` on all `/camel/` endpoints.
   - **Verdict**: FIXED
   - **Evidence**: `SecurityConfig.java` was updated to require authentication for these paths.

3. **In-Memory State Persistence**
   - **Severity**: MEDIUM
   - **Description**: Use of `ConcurrentHashMap` instead of a persistent database.
   - **Verdict**: VALID (Design Gap)
   - **Evidence**: `plan.md` identifies this as a key roadmap item.

4. **Mock Kafka Bridge (Direct Component)**
   - **Severity**: LOW
   - **Description**: Use of `direct` component for mock Kafka.
   - **Verdict**: VALID (Design Gap)
   - **Evidence**: `WorkOrderRoute.java` uses `direct:process-billing`.

## Cold Verification (Critical/High)
*No Critical or High findings required manual cold verification at this stage as the primary high-severity issue (Permissive Security) was already addressed via configuration hardening.*
