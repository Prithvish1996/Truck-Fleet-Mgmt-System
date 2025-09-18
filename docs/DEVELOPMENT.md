# TFMS Development Documentation

## Quick Start

This project uses a modular monolith architecture. For detailed instructions on creating new modules, see the [Developer Startup Guide](./DEVELOPER_STARTUP_GUIDE.md).

## Project Overview

- **Architecture**: Modular Monolith
- **Framework**: Spring Boot 3.5.5
- **Java Version**: 17
- **Build Tool**: Maven
- **Database**: H2 (development), configurable for production

## Key Commands

```bash
# Build entire project
./mvnw clean install

# Run application
./mvnw spring-boot:run -pl tfms-starter

# Run tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests
```

## Module Structure

```
modules/
├── shared-modules/     # Common utilities
├── core-modules/       # Core business entities
├── business-modules/   # Business logic
├── communication-modules/  # External communications
├── auth-modules/       # Authentication
└── platform-modules/  # Infrastructure
```

## For New Developers

**Read the [Module Creation Guide](./MODULE_CREATION_GUIDE.md) for:**
- Complete module creation walkthrough
- Code templates and examples
- Integration steps
- Testing strategies
- Best practices and conventions

## Application URLs

- **Base URL**: http://localhost:8080
- **Health Checks**: http://localhost:8080/api/{service}/health
- **H2 Console**: http://localhost:8080/h2-console (if enabled)

## Environment Profiles

- `development` - Local development with H2 database
- `test` - Testing environment
- `production` - Production environment (configure external database)

Use: `--spring.profiles.active=development`