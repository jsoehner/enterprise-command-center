# ADR 0020: Jackson 2 BOM Alignment and Spring Data Redis Serialization Remediation

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-10-15

---

## 1. Context & Business Problem Statement

During container startup and test execution in the Spring Boot `4.2.0-M2` environment, application context initialization failed during the creation of `cacheService` and `redisTemplate`:

```
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cacheService': 
Unsatisfied dependency expressed through field 'redisTemplate': 
Error creating bean with name 'redisTemplate' defined in class path resource [com/camel/aggregator/config/RedisConfig.class]: 
Failed to instantiate [org.springframework.data.redis.core.RedisTemplate]: 
Factory method 'redisTemplate' threw exception with message: Could not initialize class tools.jackson.databind.json.JsonMapper$Builder
```

Detailed log analysis and bytecode inspection of the packaged `app.jar` identified the root cause:
1. `RedisConfig.java` configures `RedisTemplate` with `template.setValueSerializer(RedisSerializer.json())`.
2. In Spring Boot 4 / Spring Data Redis 4, `RedisSerializer.json()` delegates to Jackson 3 (`tools.jackson.databind.json.JsonMapper.builder()`).
3. During static class initialization of `tools.jackson.databind.introspect.JacksonAnnotationIntrospector` (which executes in `background-preinit`), Jackson 3 inspects annotations for Jackson 2 backwards-compatibility, expecting `com.fasterxml.jackson.annotation.JsonSerializeAs` (introduced in Jackson `2.21+`).
4. An accidental property pin in `pom.xml` (`<jackson-2-bom.version>2.18.2</jackson-2-bom.version>`) downgraded `com.fasterxml.jackson:jackson-annotations` to `2.18.2`, where `JsonSerializeAs` does not exist.
5. This threw `java.lang.NoClassDefFoundError: com/fasterxml/jackson/annotation/JsonSerializeAs` inside `ExceptionInInitializerError`, causing subsequent invocations of `JsonMapper.builder()` in the main thread to fail with `NoClassDefFoundError: Could not initialize class tools.jackson.databind.json.JsonMapper$Builder`, crashing the application context refresh.

---

## 2. Decision Drivers

1. **Clean Application Startup & Runtime Stability**: Restore clean startup of `RedisConfig`, `RedisTemplate`, `CacheService`, and all Redis-backed serialization workflows without class initialization errors.
2. **Spring Boot 4 / Jackson 3 Architectural Harmony**: Maintain proper alignment between Jackson 3 (`tools.jackson.*`) used by Spring Boot 4 / Apache Camel 4.22.x and legacy transitive Jackson 2 annotations (`com.fasterxml.jackson.annotation.*`).
3. **Supply Chain Security & CVE Governance**: Keep both Jackson 2 and Jackson 3 BOMs pinned to secure, non-vulnerable releases (`jackson-2-bom: 2.22.3` and `jackson-bom: 3.2.3`), conforming to ADR-0018 to prevent reintroducing Jackson CVEs.
4. **Zero Test Regressions**: Ensure 100% of integration and unit tests pass cleanly.

---

## 3. Decision Outcome

1. **Jackson 2 BOM Alignment**:
   - Updated `<jackson-2-bom.version>` from `2.18.2` to `2.22.3` in `pom.xml`. This ensures `com.fasterxml.jackson.core:jackson-annotations` resolves to `2.22.3`, providing the required `@JsonSerializeAs` annotation.

2. **Jackson 3 BOM Alignment**:
   - Re-introduced `<jackson-bom.version>3.2.3</jackson-bom.version>` to `pom.xml` `<properties>`, aligning with ADR-0018.
   - Pinned `tools.jackson.core:jackson-databind` and `tools.jackson.core:jackson-core` to `${jackson-bom.version}` in `<dependencyManagement>`.

3. **Container Rebuild & Verification**:
   - Rebuilt the Docker container artifact with `mvn package`.
   - Verified clean application startup inside the Chainguard JRE container with zero class initialization errors.
   - Verified that `RedisSerializer.json()` initializes `JsonMapper.builder()` properly.
   - Executed full test suite: all 22 tests across `AggregatorRouteTest`, `PostQuantumCryptoServiceTest`, and `WorkOrderServiceTest` pass with zero failures.

---

## 4. Consequences & Verification

- **Positive**: Eradicates `NoClassDefFoundError: Could not initialize class tools.jackson.databind.json.JsonMapper$Builder` on startup.
- **Positive**: Restores Redis serialization functionality in `RedisConfig` and `CacheService`.
- **Positive**: Maintains alignment with ADR-0018, keeping Jackson components hardened against known DoS vulnerabilities.
- **Verification**:
  - `python3 scripts/adr_gatekeeper.py --verify` passes all ADR integrity and section checks.
  - `docker run` boots `CamelAggregatorApplication` in detached and test modes successfully.
  - `mvn test` completes with 22/22 passing tests and zero errors.
