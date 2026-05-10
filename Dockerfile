# Build stage
FROM --platform=$BUILDPLATFORM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies for offline caching
RUN mvn dependency:go-offline -B

COPY src ./src
# Build the application
RUN mvn package -DskipTests

# Run stage
FROM cgr.dev/chainguard/jre:latest
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose standard port (optional, depends on Spring Boot config, usually 8080)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
