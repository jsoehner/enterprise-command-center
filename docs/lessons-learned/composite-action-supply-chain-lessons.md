# Lessons Learned: Transitive Supply Chain Risks in Composite GitHub Actions

## Overview
During automated CI/CD pipeline execution for security assessments (`security-scan.yml`), our Trivy security scan failed with:
```text
Error: Unable to resolve action `aquasecurity/setup-trivy@v0.2.2`, unable to find version `v0.2.2`
```
Although our workflow pinned the parent action `aquasecurity/trivy-action` to a 40-character immutable commit SHA (`6c175e9c4083a92bbca2f9724c8a5e33bc2d97a5`), the job still failed due to a transient failure within a nested composite action.

This document details the root cause, architectural takeaways, and operational guidelines for composite GitHub Actions.

---

## Key Findings & Root Cause

### 1. The Anatomy of Composite Actions
A composite GitHub Action encapsulates multiple workflow steps inside an `action.yaml` manifest. When a workflow references a composite action:
1. The GitHub Actions runner checks out the composite action repository at the specified commit or tag.
2. The runner parses the composite action's `action.yaml` and executes its internal steps.
3. If an internal step calls another external action (such as `uses: aquasecurity/setup-trivy@v0.2.2`), the runner must resolve and fetch that nested action from GitHub.

### 2. The March 2026 Supply-Chain Incident
In March 2026, threat actors targeted Aqua Security repositories by force-pushing malicious commits into version tags (`v0.0.1` through `v0.34.2`). 
- In response, Aqua Security and GitHub revoked and deleted compromised mutable tags to prevent malicious execution.
- The deleted tags included `aquasecurity/setup-trivy@v0.2.2`.
- When older releases of `aquasecurity/trivy-action` (like `v0.30.0`) were invoked, their internal manifest still requested `setup-trivy@v0.2.2`.
- Because that tag no longer existed, GitHub Actions failed at runtime during action resolution.

### 3. False Sense of Security from Parent SHA Pinning
Pinning a parent action to an immutable commit SHA (as mandated by ADR-0004) guarantees that the parent action's code and manifest cannot be mutated. However, if the composite action's internal manifest references child actions via **mutable tags** rather than immutable SHAs, the pipeline remains vulnerable to:
- Tag deletion (causing build breaks).
- Upstream tag mutation / supply-chain poisoning.

---

## Remediations & Engineering Guidelines

1. **Immediate Remediation**:
   - Upgraded `aquasecurity/trivy-action` to release `v0.36.0` (commit SHA `ed142fd0673e97e23eac54620cfb913e5ce36c25`), which has been verified clean post-incident and points to valid dependencies.
   - Documented the architectural decision in **ADR-0012** and cross-referenced with ADR-0004.

2. **Guidelines for Composite Actions in CI/CD**:
   - **Audit Nested Manifests**: When adopting a third-party composite action, inspect its `action.yaml` or `action.yml` to see whether its internal steps invoke secondary actions by tag or by SHA.
   - **Prefer Standalone or Self-Contained Actions**: Where practical, utilize runner-native tooling or scripts (e.g. `curl -sfL https://raw.githubusercontent.com/...` with checksum verification, or native apt/binary packages) if nested composite action resolution proves brittle.
   - **Monitor Security Advisories**: When a vendor reports a supply-chain incident, immediately audit all pinned SHAs and versions across all workflow files, even if builds were previously passing.
