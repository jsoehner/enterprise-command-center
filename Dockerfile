# syntax=docker/dockerfile:1

# ==============================================================================
# Stage 1: Build & Layer Extraction (runs on native build host for speed)
# ==============================================================================
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25@sha256:93b8a14ea2f412782e4e842651273b4d903e35cc496284f178fbbe2d67d00976 AS build
WORKDIR /workspace

COPY <<'EOF' /tmp/ci-settings.xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <mirrors>
    <mirror>
      <id>gcs-maven-central</id>
      <name>Google Cloud Storage Maven Central Mirror</name>
      <url>https://maven-central.storage-download.googleapis.com/maven2</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

COPY pom.xml .
RUN set -eu; \
    MVN_OPTS="-B -Daether.remoteRepositoryFilter.prefixes=false -Daether.connector.basic.threads=2 -Daether.transport.http.retryHandler.count=6 -Daether.transport.http.retryHandler.interval=2000 -Daether.transport.http.retryHandler.intervalMax=15000 -Daether.transport.http.retryHandler.serviceUnavailable=429,500,502,503,504"; \
    mvn -s /tmp/ci-settings.xml dependency:go-offline ${MVN_OPTS} || mvn dependency:go-offline ${MVN_OPTS}

COPY src ./src
RUN set -eu; \
    MVN_OPTS="-B -Daether.remoteRepositoryFilter.prefixes=false -Daether.connector.basic.threads=2 -Daether.transport.http.retryHandler.count=6 -Daether.transport.http.retryHandler.interval=2000 -Daether.transport.http.retryHandler.intervalMax=15000 -Daether.transport.http.retryHandler.serviceUnavailable=429,500,502,503,504"; \
    mvn -s /tmp/ci-settings.xml package -DskipTests ${MVN_OPTS} || mvn package -DskipTests ${MVN_OPTS}; \
    rm -f /tmp/ci-settings.xml

# Extract Spring Boot 4 layered JAR and lock permissions read-only (dirs 0555, files 0444)
RUN set -eu; \
    JAR_FILE="$(ls target/rest-api-aggregator-*.jar | grep -v '\.original$' | head -n 1)"; \
    java -Djarmode=tools -jar "${JAR_FILE}" extract --layers --launcher --destination target/extracted; \
    mkdir -p target/extracted/dependencies target/extracted/spring-boot-loader target/extracted/snapshot-dependencies target/extracted/application; \
    chmod -R a-w target/extracted

# ==============================================================================
# Stage 2: Custom Minimal Java 25 LTS JRE via jlink (runs on target platform)
# ==============================================================================
# eclipse-temurin:25-jdk-noble
FROM eclipse-temurin:25-jdk-noble@sha256:4ddc1a8bdaf283421df5487411aff10b797ccf1e06a339041005114c8f489b5c AS jre-builder

RUN set -eu; \
    jlink \
      --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.naming.dns,jdk.unsupported,jdk.zipfs \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=zip-6 \
      --output /opt/java/jre; \
    chmod -R a-w /opt/java/jre

# ==============================================================================
# Stage 3: Distroless Hardened Runtime (Chainguard glibc-dynamic, ~16 MB base)
# ==============================================================================
# cgr.dev/chainguard/glibc-dynamic:latest pinned by immutable multi-arch digest
FROM cgr.dev/chainguard/glibc-dynamic@sha256:a96ba3c8d5ffc646e1092c03d72000b5e2ccd9f6cda695d6342a0e31ce4d52ee

LABEL org.opencontainers.image.title="Enterprise Command Center" \
      org.opencontainers.image.description="Hardened Apache Camel REST API Aggregator on minimal jlink Java 25 LTS and Chainguard glibc-dynamic" \
      org.opencontainers.image.source="https://github.com/jsoehner/enterprise-command-center" \
      org.opencontainers.image.vendor="jsoehner" \
      org.opencontainers.image.licenses="Apache-2.0"

ENV JAVA_HOME=/opt/java/jre \
    PATH="/opt/java/jre/bin:${PATH}"

WORKDIR /app

# Copy stripped Java 25 LTS runtime
COPY --from=jre-builder --chown=65532:65532 /opt/java/jre /opt/java/jre

# Copy Spring Boot layers ordered from least to most frequently changed
COPY --from=build --chown=65532:65532 /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=65532:65532 /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=65532:65532 /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=65532:65532 /workspace/target/extracted/application/ ./

# Explicit non-root UID/GID (Chainguard nonroot)
USER 65532:65532

EXPOSE 8080
EXPOSE 8081

STOPSIGNAL SIGTERM

# Distroless Java health check (no shell, curl, or wget required)
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD ["/opt/java/jre/bin/java", "-XX:TieredStopAtLevel=1", "-Xmx32m", "-cp", "/app/BOOT-INF/classes", "com.camel.aggregator.ContainerHealthCheck"]

ENTRYPOINT ["/opt/java/jre/bin/java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.awt.headless=true", \
  "-Djava.io.tmpdir=/tmp", \
  "-Dvertx.cacheDirBase=/tmp/vertx-cache", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]
