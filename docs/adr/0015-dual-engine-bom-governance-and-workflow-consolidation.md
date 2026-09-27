# ADR 0015: Dual-Engine BOM Governance and Workflow Consolidation

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Security Governance Team
* **Date:** 2026-09-27

---

## 1. Context & Business Problem Statement

To prevent workflow duplication and enforce cryptographic and software supply chain security, CI/CD pipelines must avoid running overlapping security jobs while ensuring comprehensive Software Bill of Materials (SBOM) and Cryptographic Bill of Materials (CBOM) capabilities.

Previously, the repository maintained both `security-scan.yml` and `security-testing.yml`, creating redundant execution overhead for SAST, secret leak detection, and vulnerability scanning. Furthermore, the legacy `sbom-cbom.yml` lacked multi-language semantic AST call-site discovery and automated verification test harnesses.

---

## 2. Decision Drivers

1. **Workflow Non-Duplication**: Consolidate redundant security scanning into a single standardized workflow (`security-testing.yml`) and remove legacy duplicate workflows.
2. **Dual-Engine BOM Governance**: Deploy the standardized `sbom.yml` workflow with Syft SPDX/CycloneDX SBOMs, cdxgen CBOMs, multi-language semantic AST call-site reconciliation (`scan_crypto_ast.py`), and Post-Quantum Cryptography (PQC) readiness scoring (`analyze_cbom.py`).
3. **Automated Verification Harness**: Enforce pre-flight validation of BOM artifacts using `scripts/test_boms.sh` to ensure schema conformity and non-empty crypto inventories.
4. **Immutable Node 24 SHA Pinning**: Enforce 40-character commit SHA pinning on all GitHub Actions steps.

---

## 3. Decision Outcome

1. **Consolidated Security Workflows**: Removed legacy `security-scan.yml`; retained `security-testing.yml` as the sole authoritative scanner for Gitleaks, Semgrep, and Trivy.
2. **Standardized Dual-Engine BOM Pipeline**: Replaced legacy `sbom-cbom.yml` with `.github/workflows/sbom.yml` and installed `generate_boms.sh`, `scan_crypto_ast.py`, `analyze_cbom.py`, and `test_boms.sh` in `scripts/`.
3. **Automated CI Test Gate**: Integrated `scripts/test_boms.sh oss` into CI build verification.

---

## 4. Consequences & Verification

- **Positive**: Eliminated duplicate security scanning in CI runs, saving runner minutes and preventing race conditions in SARIF reporting.
- **Positive**: Every release and pull request produces validated SPDX and CycloneDX SBOMs and CycloneDX 1.7 CBOMs with AST-discovered cryptographic primitives.
- **Verification**: Executed `bash scripts/test_boms.sh oss` locally; all verification checks passed successfully.
