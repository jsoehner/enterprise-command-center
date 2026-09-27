# ADR 0016: Automated Nightly Dependency Updates and Unattended Auto-Merge Governance

* **Status:** Accepted
* **Deciders:** Enterprise Command Center Architecture & Operations Governance Team
* **Date:** 2026-09-27

---

## 1. Context & Business Problem Statement

The repository runs an automated nightly dependency update pipeline (`.github/workflows/nightly-dependency-update.yml`) scheduled at 02:00 UTC (approximately 03:00 AM local time). The workflow executes `mvn versions:update-properties` and `mvn versions:use-latest-versions`, verifies build integrity using `mvn clean verify -Ddependency-check.skip=true -Dspotbugs.skip=true`, and creates a pull request via `peter-evans/create-pull-request`.

In recent runs (such as run [36302521027](https://github.com/jsoehner/enterprise-command-center/actions/runs/36302521027) on PR #178), all secondary pull request workflows (`auto-manage-prs.yml`, `pr-validation.yml`, `adr-gatekeeper.yml`, `security-scan.yml`, etc.) immediately failed with 0 jobs and the error message *"This run likely failed because of a workflow file issue."* Furthermore, because the pull request requested manual review from `@jsoehner`, the PR remained open until midday (~12:00 PM), preventing unattended 3:00 AM dependency updates.

Root cause analysis determined two core failure mechanisms:
1. **GitHub Actions Token Anti-Recursion Policy**: `nightly-dependency-update.yml` used `token: ${{ secrets.GITHUB_TOKEN }}` to create the pull request. GitHub Actions deliberately suppresses workflow triggers (`pull_request`) created by `GITHUB_TOKEN` to prevent infinite recursion, terminating downstream job execution immediately.
2. **Blocking Human Reviewer Assignment**: `nightly-dependency-update.yml` specified `reviewers: "jsoehner"`. This placed an unresolved required reviewer lock on the pull request, preventing GitHub's auto-merge engine from completing the merge until manual approval at noon.

---

## 2. Decision Drivers

1. **Unattended Execution**: Automated dependency updates verified by full Maven builds (`mvn clean verify`) must auto-merge cleanly at 3:00 AM without requiring manual approval at noon.
2. **Downstream CI Workflow Execution**: Pull request creation must use an authorized token (`PERSONAL_ACCESS_TOKEN`) so that GitHub Actions does not suppress PR validation and auto-management workflows.
3. **Elimination of Blocking Review Requests**: Remove human review requests (`reviewers: "jsoehner"`) on pre-verified automated dependency updates, while retaining `assignees: "jsoehner"` for operational notifications.
4. **Direct Auto-Merge Invocation**: Trigger `gh pr merge --auto --squash --delete-branch` immediately after pull request creation, with automatic fallback to direct squash merge where branch protection permits.

---

## 3. Decision Outcome

1. **Token Modernization**: Updated `peter-evans/create-pull-request` in `.github/workflows/nightly-dependency-update.yml` to authenticate using `token: ${{ secrets.PERSONAL_ACCESS_TOKEN || secrets.GITHUB_TOKEN }}`.
2. **Reviewer Decoupling**: Removed `reviewers: "jsoehner"` from automated pull request creation while retaining `assignees: "jsoehner"`.
3. **Automated Auto-Merge Pipeline**: Added an `Auto-merge Pull Request` step in `nightly-dependency-update.yml` that invokes `gh pr merge "$PR_URL" --auto --squash --delete-branch || gh pr merge "$PR_URL" --squash --delete-branch`.
4. **Auto-Manage PR Harmonization**: Updated `.github/workflows/auto-manage-prs.yml` to ensure `--delete-branch` is passed during automated squash merges and reinforced PAT fallback authentication.

---

## 4. Consequences & Verification

- **Positive**: Nightly dependency updates that pass full build and test verification are automatically approved and squash-merged at 3:00 AM without waiting for user approval at noon.
- **Positive**: Pull requests created with `PERSONAL_ACCESS_TOKEN` trigger CI workflows properly without triggering GitHub's anti-recursion job abortion.
- **Verification**: Verified ADR log integrity using `python3 scripts/adr_gatekeeper.py --verify`. All checks passed.
