#!/usr/bin/env python3
"""
ADR Gatekeeper Analysis Engine
Enterprise Command Center

Analyzes codebase changes, PRs, diffs, and descriptions against Architectural
Significance Rules (ASRs). Enforces ADR authoring, lifecycle management,
and decision indexing across the repository.
"""

import argparse
import fnmatch
import json
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple

DEFAULT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_CONFIG_PATH = DEFAULT_ROOT / "tools" / "adr_analyst_config.json"
DEFAULT_INDEX_PATH = DEFAULT_ROOT / "tools" / "adr_index.json"
DEFAULT_ADR_DIR = DEFAULT_ROOT / "docs" / "adr"
DEFAULT_PROMPT_PATH = DEFAULT_ROOT / "tools" / "adr_analyst_prompt.txt"


def load_json(path: Path) -> Dict[str, Any]:
    if not path.is_file():
        return {}
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"[WARN] Failed to load JSON from {path}: {e}", file=sys.stderr)
        return {}


def save_json(path: Path, data: Dict[str, Any]) -> bool:
    try:
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        return True
    except Exception as e:
        print(f"[ERROR] Failed to write JSON to {path}: {e}", file=sys.stderr)
        return False


def get_git_staged_files(repo_root: Path) -> List[str]:
    try:
        res = subprocess.run(
            ["git", "diff", "--cached", "--name-only"],
            cwd=str(repo_root),
            capture_output=True,
            text=True,
            check=True
        )
        return [line.strip() for line in res.stdout.strip().splitlines() if line.strip()]
    except Exception as e:
        print(f"[WARN] Failed to retrieve git staged files: {e}", file=sys.stderr)
        return []


def get_git_staged_diff(repo_root: Path) -> str:
    try:
        res = subprocess.run(
            ["git", "diff", "--cached"],
            cwd=str(repo_root),
            capture_output=True,
            text=True,
            check=True
        )
        return res.stdout
    except Exception as e:
        print(f"[WARN] Failed to retrieve git staged diff: {e}", file=sys.stderr)
        return ""


def get_git_diff_files(repo_root: Path, base_ref: str = "origin/main") -> Tuple[List[str], str]:
    try:
        res_files = subprocess.run(
            ["git", "diff", f"{base_ref}...HEAD", "--name-only"],
            cwd=str(repo_root),
            capture_output=True,
            text=True
        )
        files = [line.strip() for line in res_files.stdout.strip().splitlines() if line.strip()]
        
        res_diff = subprocess.run(
            ["git", "diff", f"{base_ref}...HEAD"],
            cwd=str(repo_root),
            capture_output=True,
            text=True
        )
        return files, res_diff.stdout
    except Exception as e:
        print(f"[WARN] Failed to retrieve git diff against {base_ref}: {e}", file=sys.stderr)
        return [], ""


class ADRGatekeeper:
    def __init__(
        self,
        repo_root: Optional[Path] = None,
        config_path: Optional[Path] = None,
        index_path: Optional[Path] = None,
        adr_dir: Optional[Path] = None
    ):
        self.repo_root = repo_root or DEFAULT_ROOT
        self.config_path = config_path or (self.repo_root / "tools" / "adr_analyst_config.json")
        self.index_path = index_path or (self.repo_root / "tools" / "adr_index.json")
        self.adr_dir = adr_dir or (self.repo_root / "docs" / "adr")
        self.config = load_json(self.config_path)
        self.index = load_json(self.index_path)

    def evaluate_significance(
        self,
        files: Optional[List[str]] = None,
        content_text: str = "",
        description: str = ""
    ) -> Dict[str, Any]:
        files = files or []
        thresholds = self.config.get("thresholds", {
            "comprehensive_score": 70,
            "standard_score": 40,
            "minimum_score": 20
        })

        file_patterns = self.config.get("file_patterns", [])
        keyword_rules = self.config.get("content_keywords", [])

        score = 0
        matched_rules = []
        categories_triggered = set()

        # Check if an ADR itself is in the files
        adr_files_present = [f for f in files if "docs/adr/" in f and f.endswith(".md") and not f.endswith("README.md")]

        # 1. Match File Patterns
        for f in files:
            # Normalize path relative to repo root if needed
            norm_f = f.replace("\\", "/")
            if norm_f.startswith("./"):
                norm_f = norm_f[2:]

            for fp in file_patterns:
                pattern = fp.get("pattern", "")
                weight = fp.get("weight", 0)
                category = fp.get("category", "general")

                # Match glob pattern
                if fnmatch.fnmatch(norm_f, pattern) or fnmatch.fnmatch(os.path.basename(norm_f), pattern):
                    matched_rules.append({
                        "type": "file_pattern",
                        "target": norm_f,
                        "pattern": pattern,
                        "weight": weight,
                        "category": category,
                        "description": fp.get("description", "")
                    })
                    score += weight
                    categories_triggered.add(category)

        # 2. Match Content / Description Keywords
        combined_text = f"{description}\n{content_text}"
        for kr in keyword_rules:
            regex_pat = kr.get("regex", "")
            weight = kr.get("weight", 0)
            category = kr.get("category", "general")

            if regex_pat:
                matches = re.findall(regex_pat, combined_text, re.IGNORECASE)
                if matches:
                    unique_matches = sorted(list(set(m if isinstance(m, str) else m[0] for m in matches)))
                    matched_rules.append({
                        "type": "keyword",
                        "pattern": regex_pat,
                        "matches": unique_matches,
                        "weight": weight,
                        "category": category,
                        "description": kr.get("description", "")
                    })
                    score += weight
                    categories_triggered.add(category)

        # Cap or determine recommendation
        template_rec = "none"
        if score >= thresholds.get("comprehensive_score", 70):
            template_rec = "comprehensive"
            adr_required = True
        elif score >= thresholds.get("standard_score", 40):
            template_rec = "standard"
            adr_required = True
        elif score >= thresholds.get("minimum_score", 20):
            template_rec = "minimum"
            adr_required = True
        else:
            adr_required = False

        # If an ADR is already included in the change set, gate status is PASSED
        if adr_files_present:
            verdict = "PASS_WITH_ADR"
            passed = True
            message = f"Architecturally significant changes detected (score={score}), accompanied by ADR(s): {', '.join(adr_files_present)}"
        elif not adr_required:
            verdict = "PASS_LOW_IMPACT"
            passed = True
            message = f"Changes evaluated as low architectural significance (score={score}). No ADR required."
        else:
            verdict = "ADR_REQUIRED"
            passed = False
            message = (
                f"Architectural change detected with score {score} (threshold={thresholds.get('minimum_score', 20)}). "
                f"Recommended Template: {template_rec.upper()} ADR. Please add or update an ADR in {self.adr_dir.relative_to(self.repo_root)}."
            )

        return {
            "score": score,
            "thresholds": thresholds,
            "verdict": verdict,
            "passed": passed,
            "adr_required": adr_required,
            "recommended_template": template_rec,
            "adr_files_in_change": adr_files_present,
            "categories_triggered": sorted(list(categories_triggered)),
            "matched_rules_count": len(matched_rules),
            "matched_rules": matched_rules,
            "message": message
        }

    def scan_adr_directory(self) -> List[Dict[str, Any]]:
        """Parses all ADR markdown files in docs/adr/ and returns metadata records."""
        if not self.adr_dir.exists():
            return []

        records = []
        for file_path in sorted(self.adr_dir.glob("*.md")):
            if file_path.name == "README.md" or file_path.name.startswith("template"):
                continue

            rel_path = str(file_path.relative_to(self.repo_root)).replace("\\", "/")
            record = self._parse_adr_file(file_path, rel_path)
            records.append(record)

        return records

    def _parse_adr_file(self, file_path: Path, rel_path: str) -> Dict[str, Any]:
        text = file_path.read_text(encoding="utf-8")
        
        # Extract ID and Title from filename or title line
        # e.g., 0001-production-security-hardening.md
        match_fn = re.match(r"^(\d+)-(.+)\.md$", file_path.name)
        file_id = match_fn.group(1) if match_fn else "UNKNOWN"
        
        # Title
        title = ""
        title_m = re.search(r"^#\s+(?:ADR\s*\d*:\s*)?(.+)$", text, re.MULTILINE)
        if title_m:
            title = title_m.group(1).strip()
        else:
            title = file_path.stem

        # Status
        status = "Proposed"
        status_m = re.search(r"\*?\*?Status:?\*?\*?\s*([A-Za-z0-9_\- ]+)", text, re.IGNORECASE)
        if status_m:
            status = status_m.group(1).strip().capitalize()

        # Date
        date = ""
        date_m = re.search(r"\*?\*?Date:?\*?\*?\s*(\d{4}-\d{2}-\d{2})", text, re.IGNORECASE)
        if date_m:
            date = date_m.group(1).strip()

        # Deciders
        deciders = ""
        dec_m = re.search(r"\*?\*?Deciders:?\*?\*?\s*(.+)$", text, re.MULTILINE | re.IGNORECASE)
        if dec_m:
            deciders = dec_m.group(1).strip().strip("[]")

        # Supersedes / Superseded by
        supersedes = None
        sup_m = re.search(r"Supersedes\s+\[?(?:ADR-?)?(\d{4})\]?", text, re.IGNORECASE)
        if sup_m:
            supersedes = sup_m.group(1)

        superseded_by = None
        supby_m = re.search(r"Superseded by\s+\[?(?:ADR-?)?(\d{4})\]?", text, re.IGNORECASE)
        if supby_m:
            superseded_by = supby_m.group(1)

        # Extract Summary/Context snippet
        summary = ""
        context_m = re.search(r"## (?:Context|1\.\s*Context)[^\n]*\n+([^#\n]+)", text)
        if context_m:
            summary = context_m.group(1).strip()[:200]

        return {
            "id": file_id,
            "title": title,
            "status": status,
            "date": date,
            "deciders": deciders or "Architecture Team",
            "file": rel_path,
            "supersedes": supersedes,
            "superseded_by": superseded_by,
            "summary": summary
        }

    def reindex(self) -> Dict[str, Any]:
        """Scans docs/adr/, updates tools/adr_index.json, and refreshes docs/adr/README.md."""
        records = self.scan_adr_directory()
        
        index_data = {
            "version": "1.0.0",
            "last_updated": subprocess.getoutput("date +%Y-%m-%d").strip(),
            "project": "Enterprise Command Center",
            "adr_directory": str(self.adr_dir.relative_to(self.repo_root)).replace("\\", "/"),
            "total_records": len(records),
            "records": records
        }

        save_json(self.index_path, index_data)
        self.index = index_data

        # Update docs/adr/README.md index table
        readme_path = self.adr_dir / "README.md"
        if self.adr_dir.exists():
            table_lines = [
                "# Architectural Decision Records (ADRs)",
                "",
                "This directory maintains the Architectural Decision Records (ADRs) for the **Enterprise Command Center**.",
                "",
                "## Index",
                "",
                "| ADR | Title | Status | Date |",
                "| :--- | :--- | :--- | :--- |"
            ]
            for r in records:
                table_lines.append(f"| [{r['id']}]({r['file'].split('/')[-1]}) | {r['title']} | {r['status']} | {r['date']} |")
            
            table_lines.append("")
            readme_path.write_text("\n".join(table_lines), encoding="utf-8")

        return index_data

    def verify_adrs(self) -> Tuple[bool, List[str]]:
        """Verifies integrity, mandatory sections, and links across all ADRs."""
        records = self.scan_adr_directory()
        errors = []
        
        id_set = {r["id"] for r in records}
        required_sections = self.config.get("required_sections", ["Context", "Decision", "Consequences"])

        for r in records:
            file_p = self.repo_root / r["file"]
            if not file_p.exists():
                errors.append(f"ADR record {r['id']} points to missing file: {r['file']}")
                continue

            text = file_p.read_text(encoding="utf-8")
            
            # Check required sections
            for sec in required_sections:
                if not re.search(rf"##\s+.*{sec}", text, re.IGNORECASE):
                    errors.append(f"ADR {r['id']} ({file_p.name}) is missing required section: '{sec}'")

            # Check bidirectional supersedes
            if r.get("supersedes"):
                target_id = r["supersedes"]
                if target_id not in id_set:
                    errors.append(f"ADR {r['id']} claims to supersede non-existent ADR {target_id}")
                else:
                    target_rec = next((x for x in records if x["id"] == target_id), None)
                    if target_rec and target_rec.get("superseded_by") != r["id"]:
                        errors.append(f"ADR {target_id} is superseded by {r['id']}, but missing matching 'Superseded by {r['id']}' status link")

        passed = (len(errors) == 0)
        return passed, errors


def format_report_markdown(result: Dict[str, Any]) -> str:
    lines = [
        "## ADR Gatekeeper Evaluation Report",
        "",
        f"- **Verdict**: `{result['verdict']}`",
        f"- **Architectural Significance Score**: `{result['score']}`",
        f"- **ADR Required**: `{result['adr_required']}`",
        f"- **Recommended Template**: `{result['recommended_template'].upper()}`",
        f"- **Message**: {result['message']}",
        ""
    ]

    if result.get("categories_triggered"):
        lines.append(f"**Triggered Architectural Categories**: {', '.join(result['categories_triggered'])}")
        lines.append("")

    if result.get("matched_rules"):
        lines.append("### Matched Significance Indicators")
        lines.append("| Type | Rule / Target | Weight | Category | Description |")
        lines.append("| :--- | :--- | :--- | :--- | :--- |")
        for m in result["matched_rules"]:
            tgt = m.get("target") or (", ".join(m.get("matches", [])) if m.get("matches") else m.get("pattern", ""))
            lines.append(f"| {m['type']} | `{tgt}` | +{m['weight']} | {m['category']} | {m['description']} |")
        lines.append("")

    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="ADR Gatekeeper - Architectural Decision Governance Engine")
    parser.add_argument("description", nargs="*", help="Optional text description or change proposal to evaluate")
    parser.add_argument("--scan-staged", action="store_true", help="Analyze git staged changes")
    parser.add_argument("--scan-diff", metavar="BASE_REF", nargs="?", const="origin/main", help="Analyze git diff against base ref (default: origin/main)")
    parser.add_argument("--files", nargs="+", help="Specific files to evaluate for architectural significance")
    parser.add_argument("--reindex", action="store_true", help="Scan docs/adr/, refresh tools/adr_index.json and docs/adr/README.md")
    parser.add_argument("--verify", action="store_true", help="Verify integrity and required sections across all ADRs")
    parser.add_argument("--json", action="store_true", help="Output results as JSON")
    parser.add_argument("--advisory", "--non-blocking", action="store_true", help="Exit with 0 even if an ADR is recommended")
    parser.add_argument("--root", type=Path, default=DEFAULT_ROOT, help="Project repository root path")

    args = parser.parse_args()
    gatekeeper = ADRGatekeeper(repo_root=args.root)

    # 1. Reindex Mode
    if args.reindex:
        idx = gatekeeper.reindex()
        if args.json:
            print(json.dumps(idx, indent=2))
        else:
            print(f"[OK] Reindexed {idx['total_records']} ADRs successfully. Updated tools/adr_index.json and docs/adr/README.md.")
        sys.exit(0)

    # 2. Verify Mode
    if args.verify:
        passed, errors = gatekeeper.verify_adrs()
        if args.json:
            print(json.dumps({"passed": passed, "errors": errors}, indent=2))
        else:
            if passed:
                print("[OK] All ADRs passed integrity and section verification.")
            else:
                print(f"[FAILED] Found {len(errors)} ADR verification errors:")
                for err in errors:
                    print(f"  - {err}")
        sys.exit(0 if passed else 1)

    # 3. Analysis Mode
    files_to_check = []
    diff_content = ""
    description_text = " ".join(args.description) if args.description else ""

    if args.scan_staged:
        files_to_check = get_git_staged_files(gatekeeper.repo_root)
        diff_content = get_git_staged_diff(gatekeeper.repo_root)
    elif args.scan_diff:
        base = args.scan_diff if isinstance(args.scan_diff, str) else "origin/main"
        files_to_check, diff_content = get_git_diff_files(gatekeeper.repo_root, base)
    elif args.files:
        files_to_check = args.files

    result = gatekeeper.evaluate_significance(
        files=files_to_check,
        content_text=diff_content,
        description=description_text
    )

    if args.json:
        print(json.dumps(result, indent=2))
    else:
        print(format_report_markdown(result))

    if result["passed"] or args.advisory:
        sys.exit(0)
    else:
        sys.exit(1)


if __name__ == "__main__":
    main()
