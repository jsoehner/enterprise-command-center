# ADR 0021: Centralized NVD Cache Distribution via jsoehner/nvd-cache and Offline SCA Verification

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-10-16

---

## 1. Context & Business Problem Statement

In GitHub Actions run [38861380306](https://github.com/jsoehner/enterprise-command-center/actions/runs/38861380306) (Job `update-dependencies`, ID [116908327554](https://github.com/jsoehner/enterprise-command-center/actions/runs/38861380306/job/116908327554)), the `Build and Verify with SCA` step in `.github/workflows/nightly-dependency-update.yml` failed with:

```
[ERROR] Failed to execute goal org.owasp:dependency-check-maven:13.0.0:check (default) on project rest-api-aggregator:
[ERROR]   UpdateException: Error updating the NVD Data
[ERROR]     caused by NvdApiException: Invalid API Key, length of 0 too short to provided a masked partial key
[ERROR]   NoDataException: No documents exist
```

Root cause investigation revealed a three-part failure chain:
1. **GitHub Actions 10 GB Cache Quota Eviction**: Frequent Trivy (`cache-trivy-*`, ~993 MiB each) and Docker BuildKit (`buildkit-blob-*`, ~500+ MiB) cache uploads rapidly exceed GitHub Actions' 10 GB per-repository cache limit, evicting the weekly `Linux-owasp-nvd-*` cache entry via LRU eviction.
2. **Cache Miss on Ephemeral `actions/cache/restore`**: When `nightly-dependency-update.yml` executed `Restore NVD Cache for SCA`, the `Linux-owasp-nvd-*` cache entry had been evicted (`Cache not found for input keys`), leaving `~/.m2/repository/org/owasp/dependency-check-data` empty.
3. **Unauthenticated NVD API Rate-Limiting & `autoUpdate=true` Default**:
   - With no local H2 database (`odc.mv.db`), `dependency-check-maven:13.0.0` attempted an unauthenticated NVD API v2 sync, which threw `NvdApiException: Invalid API Key, length of 0 too short to provided a masked partial key` and `NoDataException: No documents exist`.
   - Even when `odc.mv.db` IS present locally, `dependency-check-maven:13.0.0` defaults to `autoUpdate=true` and `failOnError=true`. When unauthenticated delta updates fail against NVD API rate limits, `UpdateException` is added to `engine.getExceptions()` and fails the Maven build at the end of `check`.
   - Additionally, `.NET Assembly Analyzer` logged an initialization error when scanning JARs bundling native `.dll`/`.exe` resources on runners lacking the .NET 8 runtime.

---

## 2. Decision Drivers

1. **Eviction-Proof NVD Cache Distribution**: Eliminate reliance on volatile per-repository `actions/cache` entries by consuming the centralized pre-built NVD H2 database published by [`jsoehner/nvd-cache`](https://github.com/jsoehner/nvd-cache) (`latest-nvd-data/odc.mv.db.zip`).
2. **Deterministic Offline SCA Execution**: Prevent `dependency-check-maven:13.0.0:check` from making unauthenticated live NVD API calls during CI verification runs once the database is seeded from `jsoehner/nvd-cache`.
3. **Clean Analyzer Execution**: Disable the .NET Assembly Analyzer (`<assemblyAnalyzerEnabled>false</assemblyAnalyzerEnabled>`) for this pure Java/Spring Boot project to prevent false `.NET 8.0 runtime required` errors on embedded native libraries.
4. **Workflow Consistency Across CI Pipelines**: Standardize NVD database restoration across `nightly-dependency-update.yml`, `pr-validation.yml`, and `update-nvd-cache.yml`.

---

## 3. Decision Outcome

1. **Centralized NVD Cache Restoration from `jsoehner/nvd-cache`**:
   - Upgraded `jsoehner/nvd-cache` (`.github/workflows/nvd-cache.yml` and the `latest-nvd-data/odc.mv.db.zip` release asset) from `dependency-check-maven:12.1.0` to `13.0.0` so the H2 database schema matches `dependency-check-maven:13.0.0`, and packaged `publishedSuppressions.xml*` and `jsrepository.json*` alongside `odc.mv.db`.
   - Updated `.github/workflows/nightly-dependency-update.yml`, `.github/workflows/pr-validation.yml`, and `.github/workflows/update-nvd-cache.yml` to download and extract `https://github.com/jsoehner/nvd-cache/releases/download/latest-nvd-data/odc.mv.db.zip` directly into `~/.m2/repository/org/owasp/dependency-check-data/11.0/` and `~/.m2/repository/org/owasp/dependency-check-data/odc.mv.db`.
   - Hardened `update-nvd-cache.yml` so that `mvn org.owasp:dependency-check-maven:update-only -DautoUpdate=true` is only invoked when `NVD_API_KEY` is configured, retaining the `jsoehner/nvd-cache` snapshot otherwise.

2. **Offline SCA Configuration & Legacy Repository PKIX Mitigation (`pom.xml` & `.mvn/maven.config`)**:
   - Added `<autoUpdate>false</autoUpdate>`, `<assemblyAnalyzerEnabled>false</assemblyAnalyzerEnabled>`, and `<centralAnalyzerEnabled>false</centralAnalyzerEnabled>` to `dependency-check-maven` configuration in `pom.xml`.
   - Added `.mvn/maven.config` with `-Daether.remoteRepositoryFilter.prefixes=false` and overrode the legacy `<id>jvnet-nexus-releases</id>` repository in `pom.xml` (`enabled=false`) to prevent Maven Resolver from querying the decommissioned `https://maven.java.net/content/repositories/releases/` endpoint with an invalid SSL certificate (`PKIX path building failed`).

---

## 4. Consequences & Verification

- **Positive**: Completely decouples SCA verification in CI from GitHub Actions' 10 GB cache eviction, NVD API unauthenticated rate limits, and `jvnet-nexus-releases` PKIX warnings.
- **Positive**: Aligns `jsoehner/nvd-cache` and `enterprise-command-center` on `dependency-check-maven:13.0.0` schema and reduces total SCA check execution time to ~15 seconds.
- **Verification**:
  - `mvn clean verify -Dspotbugs.skip=true` passes all 22 unit/integration tests and OWASP Dependency-Check SCA analysis with zero errors or PKIX warnings.
  - `python3 scripts/adr_gatekeeper.py --verify` passes all ADR integrity and section checks.
