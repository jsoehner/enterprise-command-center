import os
import re
import sys
import argparse
from vulnerability_patterns import PATTERNS

class SecurityAgent:
    def __init__(self, target_dir):
        self.target_dir = target_dir
        self.findings = []

    def scan_file(self, file_path):
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                lines = content.splitlines()
                
                for pattern in PATTERNS:
                    matches = re.finditer(pattern['regex'], content)
                    for match in matches:
                        line_no = content[:match.start()].count('\n') + 1
                        self.findings.append({
                            'file': file_path,
                            'line': line_no,
                            'name': pattern['name'],
                            'description': pattern['description'],
                            'severity': pattern['severity'],
                            'match': match.group(0),
                            'context': lines[max(0, line_no-3):min(len(lines), line_no+2)],
                            'pattern_obj': pattern
                        })
        except Exception as e:
            print(f"Error scanning {file_path}: {e}")

    def run_scan(self):
        print(f"[*] Starting security scan on {self.target_dir}...")
        for root, dirs, files in os.walk(self.target_dir):
            if any(d in root for d in ['.git', 'target', '.idea', '.vscode']):
                continue
            for file in files:
                if file.endswith(('.java', '.xml', '.properties', '.yml', '.yaml')):
                    self.scan_file(os.path.join(root, file))
        
        print(f"[*] Scan complete. Found {len(self.findings)} potential issues.")
        return self.findings

    def generate_report(self):
        if not self.findings:
            return "No vulnerabilities found."
        
        report = "# Security Scan Report\n\n"
        report += "| Severity | Vulnerability | File | Line | Description | Fix Suggestion |\n"
        report += "|----------|---------------|------|------|-------------|----------------|\n"
        
        # Sort by severity (High first)
        severity_map = {"CRITICAL": 0, "HIGH": 1, "MEDIUM": 2, "LOW": 3}
        sorted_findings = sorted(self.findings, key=lambda x: severity_map.get(x['severity'], 4))
        
        for f in sorted_findings:
            rel_path = os.path.relpath(f['file'], self.target_dir)
            fix = self.get_fix_suggestion(f)
            report += f"| {f['severity']} | {f['name']} | `{rel_path}` | {f['line']} | {f['description']} | {fix} |\n"
        
        return report

    def get_fix_suggestion(self, finding):
        suggestions = {
            "Hardcoded Secret": "Move secrets to environment variables or a secure vault (e.g., Spring Cloud Config, HashiCorp Vault).",
            "Insecure Randomness": "Replace `java.util.Random` with `java.security.SecureRandom`.",
            "SQL Injection Candidate": "Use `PreparedStatement` with parameterized queries instead of string concatenation.",
            "Insecure TLS/SSL": "Upgrade to `TLSv1.2` or `TLSv1.3`.",
            "Path Traversal Candidate": "Sanitize input using a whitelist or use `java.nio.file.Path` to normalize and validate paths.",
            "XXE Candidate": "Disable DTDs and external entities in `DocumentBuilderFactory`."
        }
        return suggestions.get(finding['name'], "Manual review required.")

    def apply_fixes(self):
        print(f"[*] Attempting to apply fixes for {len(self.findings)} findings...")
        files_to_fix = {}
        for f in self.findings:
            if 'fix_regex' in f['pattern_obj']:
                if f['file'] not in files_to_fix:
                    files_to_fix[f['file']] = []
                files_to_fix[f['file']].append(f)
        
        for file_path, findings in files_to_fix.items():
            try:
                with open(file_path, 'r') as f:
                    content = f.read()
                
                new_content = content
                fixed_count = 0
                for f in findings:
                    pattern = f['pattern_obj']
                    # We use a cautious replacement to avoid double-fixing or overlapping
                    # In a real agent, we'd use line numbers or AST, but for this demo regex is fine.
                    match_str = f['match']
                    fix_replacement = pattern['fix_replacement']
                    
                    # Very basic fix: replace the match if it's unique enough or by line
                    # For safety, we'll only replace if the exact match string is present
                    if match_str in new_content:
                        # Extract the part of the match that needs fixing
                        target = re.search(pattern['fix_regex'], match_str).group(0)
                        replacement = match_str.replace(target, fix_replacement)
                        new_content = new_content.replace(match_str, replacement, 1)
                        fixed_count += 1
                
                with open(file_path, 'w') as f:
                    f.write(new_content)
                print(f"[+] Applied {fixed_count} fixes to {file_path}")
            except Exception as e:
                print(f"[!] Error fixing {file_path}: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Security Agent - Find and Fix Bugs")
    parser.add_argument("target", nargs="?", default=".", help="Target directory to scan")
    parser.add_argument("--apply", action="store_true", help="Automatically apply simple fixes")
    args = parser.parse_args()
    
    agent = SecurityAgent(args.target)
    findings = agent.run_scan()
    
    if args.apply:
        agent.apply_fixes()
        # Re-scan after fixes
        agent.findings = []
        agent.run_scan()

    report = agent.generate_report()
    
    with open("security_report.md", "w") as f:
        f.write(report)
    
    print("[*] Report updated: security_report.md")
