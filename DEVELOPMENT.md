# Development Guide

This document provides detailed technical information for developing and running the microservices project.

## Architecture Overview

### Module Structure
```
root/
├── docker-compose.yml
├── pom.xml (parent pom)
└── services/
    ├── service1/
    │   ├── Dockerfile
    │   └── pom.xml
    ├── service2/
    │   ├── Dockerfile
    │   └── pom.xml
    └── ...
```

## Spring Boot Microservices Setup

### Parent POM Configuration
The parent `pom.xml` manages:
- Common dependencies
- Spring Boot/Cloud versions
- Java version and compiler settings
- Common plugins
- Module definitions

### Module Management
Each microservice module should:
1. Extend the parent POM
2. Define its own dependencies
3. Have its own Spring Boot application class
4. Include service-specific configurations

Example module `pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>your.group.id</groupId>
        <artifactId>parent-project</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>service-name</artifactId>
    <name>Service Name</name>
    
    <dependencies>
        <!-- Service-specific dependencies -->
    </dependencies>
</project>
```

## Docker Configuration

### Docker Compose Setup
The `docker-compose.yml` file orchestrates all services:
```yaml
version: '3.8'
services:
  service1:
    build: ./service1
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - microservices-network

  service2:
    build: ./service2
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge
```

### Dockerfile Template
Each service should have a Dockerfile:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

## Development Workflow

### Local Development Setup
1. Build all modules:
```bash
mvn clean install
```

2. Start all services:
```bash
docker-compose up --build
```

3. Start individual service:
```bash
docker-compose up service-name
```

### Spring Profiles
- `default`: Local development
- `docker`: Docker environment
- `test`: Testing environment

### Configuration Management
Each service should have environment-specific configurations:
```
src/main/resources/
├── application.yml
├── application-docker.yml
└── application-test.yml
```

## Adding New Services

1. Create new directory for the service
2. Add service to parent POM modules
3. Create service POM file
4. Create Dockerfile
5. Add service to docker-compose.yml
6. Implement service code
7. Update Spring configurations

## Best Practices

### Service Independence
- Each service should have its own database
- Use event-driven communication where possible
- Implement circuit breakers for service calls
- Use service discovery for inter-service communication

### Configuration
- Use environment variables for sensitive data
- Externalize configurations
- Use Spring Cloud Config for distributed configuration

### Monitoring and Logging
- Implement health check endpoints
- Use consistent logging format
- Include trace IDs in logs
- Monitor service metrics

## Troubleshooting

### Common Issues
1. Port conflicts
   - Solution: Check docker-compose port mappings
   
2. Service discovery issues
   - Solution: Verify network settings in docker-compose

3. Build failures
   - Solution: Check parent POM and module dependencies

### Docker Commands
```bash
# View logs
docker-compose logs service-name

# Rebuild specific service
docker-compose up --build service-name

# Remove all containers and volumes
docker-compose down -v
```
