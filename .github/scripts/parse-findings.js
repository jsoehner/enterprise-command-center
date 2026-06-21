const fs = require('fs');

let gitleaksFindings = [];
if (fs.existsSync('gitleaks-report.json')) {
  try {
    const raw = fs.readFileSync('gitleaks-report.json', 'utf8');
    const data = JSON.parse(raw);
    if (Array.isArray(data)) {
      for (const item of data) {
        gitleaksFindings.push({
          tool: 'Gitleaks',
          file: `${item.File}:${item.StartLine}`,
          description: `Secret leaked: ${item.Description || item.RuleID}`,
          severity: 'CRITICAL'
        });
      }
    }
  } catch (e) {
    console.error('Error parsing Gitleaks report:', e);
  }
}

let spotbugsFindings = [];
if (fs.existsSync('target/spotbugsXml.xml')) {
  try {
    const raw = fs.readFileSync('target/spotbugsXml.xml', 'utf8');
    const bugInstanceRegex = /<BugInstance\s+([^>]+)>([\s\S]*?)<\/BugInstance>/g;
    let match;
    while ((match = bugInstanceRegex.exec(raw)) !== null) {
      const attrsStr = match[1];
      const inner = match[2];
      
      const type = (attrsStr.match(/type="([^"]+)"/) || [])[1] || 'Unknown';
      const priority = (attrsStr.match(/priority="([^"]+)"/) || [])[1] || 'Unknown';
      const category = (attrsStr.match(/category="([^"]+)"/) || [])[1] || 'Unknown';
      
      const sourceLineMatch = inner.match(/<SourceLine\s+[^>]*sourcepath="([^"]+)"\s+start="([^"]+)"/);
      let file = 'Unknown';
      if (sourceLineMatch) {
        file = `${sourceLineMatch[1]}:${sourceLineMatch[2]}`;
      } else {
        const classMatch = attrsStr.match(/class="([^"]+)"/);
        if (classMatch) file = classMatch[1];
      }
      
      let severity = 'LOW';
      if (priority === '1') severity = 'HIGH';
      else if (priority === '2') severity = 'MEDIUM';
      
      spotbugsFindings.push({
        tool: 'SpotBugs',
        file: file,
        description: `Bug: ${type} (Category: ${category})`,
        severity: severity
      });
    }
  } catch (e) {
    console.error('Error parsing SpotBugs report:', e);
  }
}

let dependencyFindings = [];
if (fs.existsSync('target/dependency-check-report.json')) {
  try {
    const raw = fs.readFileSync('target/dependency-check-report.json', 'utf8');
    const data = JSON.parse(raw);
    if (data.dependencies && Array.isArray(data.dependencies)) {
      for (const dep of data.dependencies) {
        if (dep.vulnerabilities && Array.isArray(dep.vulnerabilities)) {
          for (const vuln of dep.vulnerabilities) {
            dependencyFindings.push({
              tool: 'Dependency-Check',
              file: dep.fileName,
              description: `${vuln.name}: ${vuln.description ? vuln.description.substring(0, 150) + '...' : 'No description'}`,
              severity: vuln.severity || 'HIGH'
            });
          }
        }
      }
    }
  } catch (e) {
    console.error('Error parsing Dependency-Check report:', e);
  }
}

let trivyFindings = [];
if (fs.existsSync('trivy-results.json')) {
  try {
    const raw = fs.readFileSync('trivy-results.json', 'utf8');
    const data = JSON.parse(raw);
    if (data.Results && Array.isArray(data.Results)) {
      for (const result of data.Results) {
        if (result.Vulnerabilities && Array.isArray(result.Vulnerabilities)) {
          for (const vuln of result.Vulnerabilities) {
            trivyFindings.push({
              tool: 'Trivy',
              file: `${result.Target} (${vuln.PkgName})`,
              description: `${vuln.VulnerabilityID}: Installed version: ${vuln.InstalledVersion}. Fixed in: ${vuln.FixedVersion || 'N/A'}.`,
              severity: vuln.Severity || 'HIGH'
            });
          }
        }
      }
    }
  } catch (e) {
    console.error('Error parsing Trivy report:', e);
  }
}

const allFindings = [
  ...gitleaksFindings,
  ...spotbugsFindings,
  ...dependencyFindings,
  ...trivyFindings
];

let markdown = '';
if (allFindings.length > 0) {
  markdown = '### Identified Security Findings\n\n';
  markdown += '| Tool | Target/File | Finding / Description | Severity |\n';
  markdown += '| :--- | :--- | :--- | :--- |\n';
  for (const f of allFindings) {
    const cleanDesc = f.description
      .replace(/\|/g, '\\|')
      .replace(/\r?\n|\r/g, ' ')
      .trim();
    markdown += `| **${f.tool}** | \`${f.file}\` | ${cleanDesc} | \`${f.severity}\` |\n`;
  }
  fs.writeFileSync('findings-table.md', markdown);
}

console.log(`Successfully parsed ${allFindings.length} findings.`);
