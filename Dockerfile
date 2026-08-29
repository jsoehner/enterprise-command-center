# Build stage
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies for offline caching
RUN mvn dependency:go-offline -B

COPY src ./src
# Build the application
RUN mvn package -DskipTests
# Trigger rebuild: updated build step timestamp
RUN echo "Build triggered at $(date)"

# --- SECURITY SCAN --- 
# It is recommended to run Trivy to scan the build artifacts:
# trivy fs . 

# Run stage
FROM cgr.dev/chainguard/jre:latest
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# --- SECURITY SCAN --- 
# It is recommended to run Trivy to scan the final image:
# trivy image --severity HIGH,CRITICAL <image_name>

# Expose standard port and WebSocket port
EXPOSE 8080
EXPOSE 8081

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
