# TFMS Spring Boot Application

## Project Overview

This is a **multi-module Spring Boot project** with the following structure:


- `tfms-starter` – Main Spring Boot backend application.
- `common-modules` – Shared modules and utilities.
- `auth-service` – Authentication service module.
- `frontend` – Frontend application (if applicable).
- `scripts/start-dev.sh` – Script to build and start the backend in dev mode.

---

## Prerequisites

Before running the application, make sure you have the following installed:

- **Java JDK 17+**
- **Maven 3.8+**
- **PostgreSQL 15**
- **Git Bash** (for running shell scripts on Windows)
- **IntelliJ IDEA** (recommended)

---

## Step 1: Enable Git Bash in IntelliJ (Windows)

1. Open **File → Settings → Tools → Terminal**.
2. Set **Shell path** to your Git Bash executable:
3. Click **Apply → OK**.  
4. Open the IntelliJ terminal (`Alt+F12`) to use Git Bash.  
5. You can now run scripts like:

```
bash scripts/start-dev.sh
```

## Step 2: Setup PostgreSQL Database

1. Open psql or pgAdmin and run:
```
CREATE DATABASE tfmsdb;
CREATE USER tfms_user WITH PASSWORD 'tfms_pass';
GRANT ALL PRIVILEGES ON DATABASE tfms_db TO tfms_user;
```

2. Verify the connection:
```
psql -U tfms_user -d tfms_db -h localhost -p 5432
```

## Step 3: Configure Spring Boot
Edit tfms-starter/src/main/resources/application-dev.properties:
```
# PostgreSQL 15 connection
spring.datasource.url=jdbc:postgresql://localhost:5432/tfms_db?ssl=false
spring.datasource.username=tfms_user
spring.datasource.password=tfms_pass
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate / JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Dev server
server.port=8080
server.ssl.enabled=false
```

## Step 4: Install Maven Dependencies
From the project root:
```
# Download all dependencies
mvn dependency:go-offline

# Build and install all modules locally (skip tests if necessary)
mvn clean install -DskipTests

Ensures common-modules and auth-service are available to tfms-starter.
```

## Step 5: Run the Dev Script

From IntelliJ terminal (Git Bash) or any Git Bash window:
```
bash scripts/start-dev.sh
```

1. Builds and starts the backend.
2. Backend connects to PostgreSQL 15. 
3. Access APIs at:
```
http://localhost:8080/api/
```
## Step 6: Verify Backend

1. Check backend.log for startup errors.
2. Verify database tables are created by Hibernate:
```
psql -U tfms_user -d tfms_db -h localhost -p 5432
\dt
```
3. In your browser, access the APIs:
```
http://localhost:8080/api/
```
## Step 7: Notes and Troubleshooting

1. Maven build errors due to tests: use -DskipTests to bypass.
2. Database connection issues: verify spring.datasource.url, username, password, and that PostgreSQL is running.
3. SSL warnings in browser: disabled for dev with server.ssl.enabled=false.
4. Backend startup timeout: increase the timeout variable in start-dev.sh if PostgreSQL is slow.
5. IntelliJ driver warning: safe to ignore if PostgreSQL dependency is in Maven and build succeeds.

## ✅ Result
1. All modules are built.
2. Backend (tfms-starter) is running locally.
3. PostgreSQL 15 is connected.
4. APIs are accessible via HTTP without SSL warnings.
5. start-dev.sh can be run directly from IntelliJ terminal (Git Bash enabled).


## Modules/ Services

- [Parcel Service](tfms-modules/planner-modules/PARCEL-README.md) – Handles parcel operations such as create, retrieve by ID, and retrieve by warehouse.