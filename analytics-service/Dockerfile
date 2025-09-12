FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the jar
COPY target/analytics-service-1.0.0-SNAPSHOT.jar ./analytics-service.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "analytics-service.jar"]
