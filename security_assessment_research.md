# Open Source Security Scanning Tools Research

## 1. SAST (Static Application Security Testing)
*Analyzes source code, byte code, or binaries without executing the program.*

| Tool | Language/Focus | Description |
| :--- | :--- | :--- |
| **Semgrep** | Multi-language | A fast, lightweight, and highly customizable static analysis tool using pattern matching. |
| **SonarQube (Community Edition)** | Multi-language | Provides deep analysis of code quality and security vulnerabilities. |
| **SpotBugs** | Java | Uses bytecode analysis to find bugs in Java programs (successor to FindBugs). |
| **Bandit** | Python | Specifically for finding common security issues in Python code. |

## 2. SCA (Software Composition Analysis)
*Identifies open-source components and their known vulnerabilities.*

| Tool | Focus | Description |
| :--- | :--- | :--- |
| **OWASP Dependency-Check** | Java/Maven/Gradle/etc. | Identifies project dependencies and checks if there are any known, publicly disclosed vulnerabilities. |
| **Trivy (FS mode)** | Multi-language/Files | Can scan filesystem for dependency vulnerabilities in various manifest files. |

## 3. DAST (Dynamic Application Security Testing)
*Tests the application while it is running to find security vulnerabilities.*

| Tool | Focus | Description |
| :--- | :--- | :--- |
| **OWASP ZAP (Zaproxy)** | Web Applications | An easy-to-use, open-source tool for finding vulnerabilities in web applications during development and testing. |
| **Nikto** | Web Servers | A command-line tool that scans web servers for dangerous files, outdated software, and other problems. |

## 4. Container Scanning
*Scans Docker images and container layers for vulnerabilities.*

| Tool | Focus | Description |
| :--- | :--- | :--- |
| **Trivy** | Containers/Images | A comprehensive security scanner that can scan container images, filesystems, and Git repositories. |
| **Clair** | Container Images | An API-based vulnerability scanner for container images, often used with registries. |

## 5. Secret Scanning
*Detects hardcoded secrets like API keys, passwords, and tokens.*

| Tool | Focus | Description |
| :--- | :--- | :--- |
| **Gitleaks** | Git Repositories | Scans git repositories for secrets that might have been accidentally committed. |
| **TruffleHog** | Git/Filesystems | Searches through git repositories for high-entropy strings and secrets. |