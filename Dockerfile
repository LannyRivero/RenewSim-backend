# ═══════════════════════════════════════════════════════════════
# Stage 1: Build
# ═══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven Wrapper (for reproducible builds)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer - only re-runs if pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (run tests for production builds)
RUN ./mvnw clean package -DskipTests

# ═══════════════════════════════════════════════════════════════
# Stage 2: Runtime
# ═══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S renewsim && \
    adduser -S renewsim -G renewsim && \
    chown -R renewsim:renewsim /app

# Switch to non-root user
USER renewsim

# Copy JAR from build stage
COPY --from=build --chown=renewsim:renewsim /app/target/backend-*.jar app.jar

# Health check (Spring Boot Actuator)
HEALTHCHECK --interval=30s \
            --timeout=3s \
            --start-period=40s \
            --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Production JVM settings
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
