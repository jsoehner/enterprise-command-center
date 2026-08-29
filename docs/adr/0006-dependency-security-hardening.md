# ADR 0006: Dependency Security Hardening

## Context
During a security audit of the `enterprise-command-center` project, several dependencies were identified as having non-optimal versions or potentially outdated security profiles. To maintain a secure and stable production environment, it is necessary to standardize and harden the dependency tree.

## Decision
We have decided to update the following core dependencies in the `pom.xml` to more recent and secure versions:
- `postgresql`: Updated to `42.7.13`
- `commons-io`: Updated to `2.18.0`
- `commons-lang3`: Updated to `3.17.0`
- `httpclient5`: Updated to `5.3.1`
- `httpcore5`: Updated to `5.2.2`
- `jackson-databind` & `jackson-core`: Updated to `2.18.2`
- `log4j-api` & `log4j-to-slf4j`: Updated to `2.24.1`
- `error_prone_annotations`: Updated to `2.33.0`
- `mapstruct`: Updated to `1.6.3`

Additionally, the `spotbugs-maven-plugin` version was downgraded to `4.8.0` to maintain compatibility with the current Maven 3.8.4 environment while still providing critical static analysis.

## Consequences
- **Positive**: Reduced attack surface by mitigating known vulnerabilities in common libraries.
- **Positive**: Improved compatibility with modern Java 25 features and Spring Boot 3.5.x.
- **Negative**: Requires verification of all integrations using updated library APIs (though these are mostly minor version bumps).
- **Neutral**: Increased build time slightly due to updated dependency resolution.

## Status
Accepted
