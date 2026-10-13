# ==============================================================================
# FLOWRA BACKEND DOCKERFILE (Spring Boot 3 + Java 21)
# Multi-stage build for optimal image size, security, and performance
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: BUILD STAGE
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source code and package JAR (skipping test suite for container build)
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# STAGE 2: RUNTIME STAGE
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 1. Create a dedicated non-root user and group for security
RUN addgroup -S flowra && adduser -S flowra -G flowra

# 2. Copy compiled JAR from builder stage
COPY --from=builder /app/target/flowra-backend-0.0.1-SNAPSHOT.jar app.jar

# 3. Set proper ownership
RUN chown -R flowra:flowra /app

# 4. Switch to non-root user
USER flowra:flowra

# 5. Expose default port (Render automatically sets $PORT environment variable at runtime)
EXPOSE 8080

# 6. Container-optimized JVM memory & GC settings
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 7. Start application with dynamic Render port binding and 0.0.0.0 network interface
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dserver.address=0.0.0.0 -jar app.jar"]
