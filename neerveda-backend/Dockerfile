# ============================================================
# NeerVeda Backend — Multi-stage Docker Build
# Java 21 + Spring Boot 3.2
# ============================================================

# ----- Stage 1: Build ----------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy dependency files first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ----- Stage 2: Runtime --------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S neerveda && adduser -S neerveda -G neerveda

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Ensure file is owned by app user
RUN chown neerveda:neerveda app.jar

USER neerveda

EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dio.grpc.netty.shaded.io.netty.tcnative.loadJni=false", \
  "-Dcom.google.cloud.firestore.grpc=false", \
  "-jar", "app.jar"]
