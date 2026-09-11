<!-- OKF Decision: Policy / Architecture Standard -->

# ADR 0012: Remediation of Trivy Action Dependency and SHA Pinning to Clean Release v0.36.0

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & DevSecOps Team
* **Date:** 2026-09-07

---

## 1. Context & Business Problem Statement

During execution of the automated CI/CD security assessment pipeline ([`.github/workflows/security-scan.yml`](../../.github/workflows/security-scan.yml)), the `Container & FS Scanning (Trivy)` step failed with the following resolution error:

```text
Error: Unable to resolve action `aquasecurity/setup-trivy@v0.2.2`, unable to find version `v0.2.2`
```

Root-cause analysis determined that:
1. The security scanning workflow was previously pinned to commit `6c175e9c4083a92bbca2f9724c8a5e33bc2d97a5` (version `v0.30.0`), in conformance with ADR-0004.
2. `aquasecurity/trivy-action` is implemented as a composite GitHub Action. Within its internal `action.yaml`, it invokes a nested action `aquasecurity/setup-trivy@v0.2.2` to download and install the Trivy CLI binary.
3. Following a supply-chain attack against Aqua Security's actions in March 2026 where tags `v0.0.1` through `v0.34.2` were compromised with credential exfiltration code, Aqua Security invalidated and removed those mutable version tags (including `setup-trivy@v0.2.2`).
4. When the GitHub Actions runner resolved `aquasecurity/trivy-action` at commit `6c175e9c4083a92bbca2f9724c8a5e33bc2d97a5`, it failed while attempting to look up the deleted upstream composite dependency `aquasecurity/setup-trivy@v0.2.2`.

This failure halted automated vulnerability and container scanning across all repository pull requests and scheduled nightly runs.

---

## 2. Decision Drivers

1. **Pipeline Reliability & Continuity**: Restore unblocked, automated security vulnerability scanning in CI/CD without disabling vulnerability gates.
2. **Supply Chain Security & Immutability**: Strictly adhere to ADR-0004's zero-trust mandate of pinning third-party GitHub Actions to verified, immutable 40-character commit SHAs.
3. **Defense Against Upstream Compromise**: Ensure the action version chosen is explicitly clear of the March 2026 supply-chain attack vectors (release `v0.35.0` or higher).
4. **Format Compatibility**: Maintain full backwards compatibility with downstream report parsers ([`.github/scripts/parse-findings.js`](../../.github/scripts/parse-findings.js)), which consume `trivy-results.json`.

---

## 3. Decision Outcome

Chosen Strategy: **Upgrade `aquasecurity/trivy-action` to verified release `v0.36.0` and pin to immutable commit SHA `ed142fd0673e97e23eac54620cfb913e5ce36c25`, amending the pin established in ADR-0004.**

### Pinned Action Reference

| Action | Version | Commit SHA | Verification Status |
| :--- | :--- | :--- | :--- |
| `aquasecurity/trivy-action` | `v0.36.0` | `ed142fd0673e97e23eac54620cfb913e5ce36c25` | Verified Clean (Post-Incident Release) |

### Configuration Alignment in `security-scan.yml`

```yaml
      - name: Container & FS Scanning (Trivy)
        id: trivy
        uses: aquasecurity/trivy-action@ed142fd0673e97e23eac54620cfb913e5ce36c25 # v0.36.0
        continue-on-error: true
        env:
          TRIVY_DB_REPOSITORY: ghcr.io/aquasecurity/trivy-db:2
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'json'
          output: 'trivy-results.json'
          exit-code: '1'
          severity: 'CRITICAL,HIGH'
```

---

## 4. Consequences & Trade-Offs

### Positive Consequences
* **Resolution of Action Lookup Failure**: The runner resolves all composite steps cleanly, as `v0.36.0` points to existing, verified internal setup dependencies.
* **Hardened Supply-Chain Posture**: Upgrading past the compromised tag boundary (`v0.34.2`) eliminates potential latent vulnerabilities from the March 2026 compromise.
* **Preserved Reporting Integration**: Output format, severity filtering, and JSON artifact structure remain identical, allowing seamless aggregation by `parse-findings.js`.

### Negative Consequences & Operational Costs
* **Composite Action Transitive Risk**: Even when parent actions are pinned by commit SHA, composite actions can internally reference external actions by mutable tags. Regular audit of nested action manifests is required.

---

## 5. Next Steps & Validation

- [x] Update `.github/workflows/security-scan.yml` to commit SHA `ed142fd0673e97e23eac54620cfb913e5ce36c25` (`# v0.36.0`).
- [x] Update `docs/security-pipeline-audit.md` to reflect the updated SHA and version.
- [x] Author ADR 0012 to formalize the dependency remediation.
- [x] Reindex ADRs using `python3 scripts/adr_gatekeeper.py --reindex`.
- [x] Verify ADR log and section integrity via `python3 scripts/adr_gatekeeper.py --verify`.
- [x] Sync ADR record and architectural lessons learned to the local memory system.
