FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the jar
COPY target/analytics-service-1.0.0-SNAPSHOT.jar ./analytics-service.jar

# Expose the port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1

# Run the jar
ENTRYPOINT ["java", "-jar", "analytics-service.jar"]
