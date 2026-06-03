# ── Build Stage ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime Stage ─────────────────────────────────────────────────────────────
# eclipse-temurin:21-jre-alpine — lightweight Java 21 JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S lifeguard && adduser -S lifeguard -G lifeguard
USER lifeguard

COPY --from=builder /app/target/lifeguard-insurance-1.0.0.jar app.jar

EXPOSE 8080

# Java 21: virtual threads handle concurrency; no extra GC flags needed for most workloads.
# -XX:+UseZGC uses the low-latency Z Garbage Collector (generational ZGC stable in 21).
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-XX:+ZGenerational", \
            "-Dspring.profiles.active=prod", \
            "-jar", "app.jar"]
