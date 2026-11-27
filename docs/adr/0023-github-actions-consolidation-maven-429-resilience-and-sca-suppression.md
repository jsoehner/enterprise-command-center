# ADR-0023: GitHub Actions Consolidation, Maven 429 Resilience, and SCA False-Positive Remediation

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-11-27

---

## 1. Context and Problem Statement
Review of GitHub Actions runs on `main` revealed three failing workflows and four redundant or non-compliant workflow definitions:

1. **Maven Central `HTTP 429 Too Many Requests` Failures**: `Semantic Release` (`45982711281`) and `Build and Publish Docker Container` (`45982710978`) failed because multiple concurrent push workflows downloaded dependencies directly from `https://repo.maven.apache.org/maven2` without throttling or HTTP `429` retry handling (Maven Resolver's default `serviceUnavailable` list excludes `429`).
2. **Nightly Dependency SCA False Positive**: `Nightly Dependency Update` (`45980035925` and three prior nightly runs) upgraded `<httpcore5.version>` from `5.5-beta4` to `5.5` GA, causing `dependency-check-maven:13.0.0` to report `CVE-2026-54399` (CVSS `7.5`) and `CVE-2026-54428` (CVSS `7.5`). Apache fixed both DoS flaws in `5.4.3` and `5.5-beta2`, so `5.5` GA is already patched; NVD's pre-release CPE range (`<= 5.5-beta1`) falsely matched GA `5.5`.
3. **Workflow Redundancy & Governance Drift**:
   - `update-nvd-cache.yml` uploaded `Linux-owasp-nvd-*` caches that no workflow consumes after ADR-0021, wasting GitHub Actions' 10 GB cache quota.
   - `auto-bump-version.yml` duplicated `semantic-release.yml`, created `[skip ci]` commits that caused `origin/main` divergence, triggered extra uncached Maven builds, and interpolated `github.event.head_commit.message` inline in Bash.
   - `security-testing.yml` and `adr-gatekeeper.yml` split security and ADR checks outside the single authoritative `.github/workflows/security-governance.yml` standard.
   - Five workflows (`docker-publish.yml`, `semantic-release.yml`, `pr-validation.yml`, `nightly-dependency-update.yml`, `auto-manage-prs.yml`) installed Node.js 24 unnecessarily.

## 2. Decision Drivers & Quality Attributes
* **Reliability / Availability**: Eliminate Maven Central `429` build failures and unblock nightly dependency automation.
* **Security**: Standardize on `.github/workflows/security-governance.yml`, keep 40-character SHA pinning (ADR-0004), and bind dynamic workflow expressions via `env:` to prevent CWE-78 shell injection.
* **Performance / Maintainability**: Reduce workflow count from 11 to 8, skip redundant Node.js setup steps, and skip heavy multi-arch Docker PR builds on docs-only changes.

## 3. Considered Options
| Option | Summary | Pros | Cons / Trade-offs |
| :--- | :--- | :--- | :--- |
| **Option 1** (Selected) | Consolidate to 8 workflows (`security-governance.yml`), mirror Maven Central via GCS + 429 retries, upgrade `httpcore5` to `5.5` with version-pinned false-positive suppression | Stops 429 storms, fixes nightly SCA, enforces single security gate, preserves 10 GB GHA cache | Requires maintaining GCS mirror fallback in Maven CI steps |
| **Option 2** | Keep all 11 workflows and pin `httpcore5` to `5.5-beta4` | Minimal YAML edits | Leaves 4 redundant workflows, wastes cache/runners, keeps beta dependency |

## 4. Decision Outcome
Chosen option: **Option 1**, because it resolves every failing pipeline while eliminating redundant workflows and aligning with organizational CI/CD governance.

### Implemented Changes
1. **Dependency & SCA Alignment (`pom.xml`, `config/dependency-check/suppressions.xml`)**:
   - Upgraded `<httpcore5.version>` from `5.5-beta4` to `5.5`.
   - Added version-pinned (`@5.5` / `:5.5:`) suppressions for `CVE-2026-54399` and `CVE-2026-54428`.
2. **Maven Central 429 Resilience (`Dockerfile`, `semantic-release.yml`, `pr-validation.yml`, `nightly-dependency-update.yml`)**:
   - Configured Google's Maven Central GCS mirror (`https://maven-central.storage-download.googleapis.com/maven2`) with automatic fallback to Maven Central.
   - Added `-Daether.connector.basic.threads=2`, `-Daether.transport.http.retryHandler.count=6`, `interval=2000`, `intervalMax=15000`, and `serviceUnavailable=429,500,502,503,504`.
3. **Workflow Consolidation & Pruning (11 → 8)**:
   - Removed `.github/workflows/update-nvd-cache.yml`, `auto-bump-version.yml`, `security-testing.yml`, and `adr-gatekeeper.yml`.
   - Created `.github/workflows/security-governance.yml` combining `secret-scan` (Gitleaks), `sast-code-scan` (Semgrep), `adr-governance` (`adr_gatekeeper.py`), and `container-security-scan` (Trivy `config` + local Buildx image + Trivy `image`).
   - Removed unused `actions/setup-node` steps from 5 workflows, ignored `docs/**` and `*.md` on `docker-publish.yml` PR builds, and bound `${{ github.event.pull_request.title }}` via `env:` in `commit-lint.yml`.

## 5. Consequences & Trade-offs
### Positive Consequences
* **Restored Green CI**: Eliminates Maven Central `429` flakes and unblocks `Nightly Dependency Update`.
* **Leaner Runner & Cache Footprint**: Removing `update-nvd-cache.yml`, `auto-bump-version.yml`, and 5 unused Node.js setups cuts runner minutes and prevents GHA cache eviction.

### Negative Consequences & Operational Costs
* **Suppression Review**: The `httpcore5:5.5` false-positive suppression should be removed if OWASP/NVD corrects the pre-release CPE range or when `httpcore5` advances beyond `5.5`.

### Follow-on Actions & Migration Plan
- [x] Upgrade `httpcore5` to `5.5` and add version-pinned suppression in `config/dependency-check/suppressions.xml`.
- [x] Add GCS mirror and HTTP 429 retry configuration across `Dockerfile` and Maven workflows.
- [x] Prune 4 redundant workflows and consolidate security/ADR gates into `.github/workflows/security-governance.yml`.
- [x] Verify YAML syntax, `mvn test`, `docker build`, Trivy `config`, ADR Gatekeeper, and live GitHub Actions runs.
