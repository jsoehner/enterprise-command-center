# Security Scanning Tool Assessment Report

## 1. Executive Summary
This report provides an assessment of open-source security scanning tools tailored for the **REST API Aggregator** project. The goal is to implement a multi-layered security approach covering source code, dependencies, container images, and runtime behavior.

##  2. Project Context & Requirements
The project uses the following stack:
- **Core**: Java 21, Spring Boot, Apache Camel.
- **Build/Dependency Management**: Maven.
- **Deployment**: Docker (Multi-stage builds).
- **Architecture**: RESTful API / Backend-for-Frontend (BFF).

**Key Security Objectives:**
- Prevent introduction of vulnerable third-party libraries (SCA).
- Detect insecure coding patterns in Java/Camel logic (SAST).
- Ensure container images are free from known vulnerabilities (Container Scanning).
- Prevent credential leakage in the repository (Secret Scanning).
- Identify runtime vulnerabilities in the exposed API (DAST).

## 3. Tool Evaluation & Recommendations

### 3.1 Static Application Security Testing (SAST)
| Tool | Suitability | Pros | Cons | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **SpotBugs** | **High** | Native Java support; integrates via Maven plugin; low overhead. | Limited to Java/Bytecode; less "modern" than Semgrep. | **Primary choice for deep Java analysis.** |
| **Semgrep** | **High** | Extremely fast; easy-to-write custom rules; great for CI/CD. | Requires learning pattern syntax (though simple). | **Use for rapid, multi-language linting in CI.** |

### 3.2 Software Composition Analysis (SCA)
| Tool | Suitability | Pros | Cons | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **OWASP Dependency-Check**| **High** | Deep Maven integration; identifies CVEs in `pom.xml`. | Can be slow on large dependency trees; high false positive rate occasionally. | **Essential for Maven-based dependency auditing.** |
| **Trivy (FS mode)** | **Medium** | Very fast; single tool for multiple purposes. | Less specialized for deep Java logic than Dependency-Check. | **Use as a secondary, faster check in CI pipelines.** |

### 3.3 Container Scanning
| Tool | Suitability | Pros | Cons | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **Trivy** | **High** | Scans images, filesystems, and Git repos; extremely easy to use in Docker workflows. | Can produce large amounts of data if not filtered. | **Primary tool for scanning the `Dockerfile` output.** |
| **Clair** | **Low** | Robust; industry standard for registries. | More complex to set up (requires registry integration). | **Avoid unless implementing a private registry.** |

### 3.4 Secret Scanning
| Tool | Suitability | Pros | Cons | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **Gitleaks** | **High** | Lightweight; can be run as a pre-commit hook or in CI. | Requires configuration for custom patterns. | **Mandatory for preventing credential leaks.** |
| **TruffleHog** | **Medium** | Excellent at finding high-entropy strings. | Can be more resource-intensive on large histories. | **Use as a secondary check if Gitle Rex is insufficient.** |

### 3.5 Dynamic Application Security Testing (DAST)
| Tool | Suitability | Pros | Cons | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **OWASP ZAP** | **High** | Industry standard; can be automated via API/Docker; great for REST APIs. | Can be slow; requires a running instance of the app. | **Use in integration testing phase.** |

## 4. Proposed Security Pipeline Strategy

To achieve maximum coverage with minimum friction, I recommend the following pipeline integration:

1.  **Developer Machine (Pre-commit)**:
    *   **Gitleaks**: Run via `pre-commit` hooks to catch secrets before they reach the remote repo.
2.  **CI Pipeline (Build Phase - Maven/Java)**:
    *   **SpotBugs**: Integrated as a Maven plugin during the `verify` stage.
    *   **OWASP Dependency-Check**: Run during the build to fail the pipeline if high-severity CVEs are found in `pom.xml`.
3.  **CI Pipeline (Container Phase - Docker)**:
    *   **Trivy**: Scan the built image immediately after the `docker build` step.
4.  **CD/Staging Phase (Runtime/Integration)**:
    *   **OWASP ZAP**: Run automated baseline scans against the deployed container in a staging environment to detect API-level vulnerabilities.

## 5. Conclusion
By implementing this layered approach, the project will benefit from "shifting left" (catching bugs early with SAST/SCA) while maintaining robust runtime protection (DAST). The recommended tools are all open-source, highly compatible with the existing Maven/Docker stack, and can be fully automated within a modern CI/CD workflow.