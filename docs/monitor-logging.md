# TFMS Logging & Monitoring Guide

## Table of Contents
- [Overview](#overview)
- [Log File Structure](#log-file-structure)
- [How Logging Works](#how-logging-works)
- [Maven Dependencies Configuration](#maven-dependencies-configuration)
- [Using the Logger](#using-the-logger)
- [Available Logging Methods](#available-logging-methods)
- [Adding a New Module/Service](#adding-a-new-moduleservice)
- [Log Levels](#log-levels)
- [Configuration Files](#configuration-files)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)
- [Quick Reference](#quick-reference)

---

## Overview

The TFMS (Truck Fleet Management System) uses **Log4j2** as its logging framework with automatic service-based log file separation. This means:

- Each service/module automatically gets its own log file
- All logs are also collected in a master log file
- Errors are automatically separated into an error-specific log
- Logs are stored inside the project at `logs/` directory
- Automatic log rotation based on size and date
- **Full DEBUG support** with performance optimization features

### Key Features
- **Automatic Service Detection**: Service names are automatically extracted from your code
- **Multiple Log Files**: Separate files for each service, errors, and performance metrics
- **Environment-Specific**: Different configurations for dev and production
- **Thread Context**: Each log entry includes service name, operation, and environment
- **Debug Optimization**: Helper methods to avoid expensive operations when debug is disabled
- **Operation Tracking**: Track specific business operations across your application

### Debug Features Included
The logging system includes comprehensive DEBUG support:

1. **`debug()`** - Simple debug logging
2. **`debugOp()`** - Debug logging with operation tracking
3. **`isDebugEnabled()`** - Check if debug is enabled (performance optimization)
4. **Conditional Debug** - Only execute expensive debug operations when needed

Example:
```java
// Simple debug
logger.debug(ServiceName.ORDER_SERVICE, "Processing order: {}", orderId);

// Debug with operation tracking
logger.debugOp(ServiceName.PAYMENT_SERVICE, "CALCULATE_TAX", 
    "Tax calculated: base={}, rate={}, total={}", baseAmount, rate, total);

// Performance-optimized debug
if (logger.isDebugEnabled()) {
    String expensiveData = buildComplexDebugString(); // Only runs if DEBUG is ON
    logger.debug(ServiceName.ORDER_SERVICE, "Complex data: {}", expensiveData);
}
```

---

## Log File Structure

All log files are located in: `/home/prithvish/Documents/GitHub/02/logs/`

### Log Files Created:

| File Name | Purpose | When Created |
|-----------|---------|--------------|
| `tfms-truck-fleet-management-system.log` | Main application/framework logs | Always |
| `tfms-application.log` | Combined logs from all services | Always |
| `error.log` | All ERROR level logs from any service | When errors occur |
| `performance.log` | Performance metrics and monitoring | When using performance logger |
| `{service-name}.log` | Individual service logs (e.g., `user-service.log`) | When service logs something |

### Example Log Directory:
```
logs/
├── tfms-truck-fleet-management-system.log  (Main app logs)
├── tfms-application.log                    (All combined logs)
├── error.log                               (Errors only)
├── performance.log                         (Performance metrics)
├── common-service.log                      (Common module logs)
├── user-service.log                        (User service logs)
├── order-service.log                       (Order service logs)
├── security-service.log                    (Security logs)
├── driver-service.log                      (Driver module logs)
├── package-service.log                     (Package tracking logs)
└── notification-service.log                (Notification logs)
```

### Log File Rotation

Logs automatically rotate when:
- **Size limit reached**: 10MB in development, 50MB in production
- **Daily rotation**: New file created each day
- **Retention**: Last 10 files in dev, 30 files in production

Rotated files follow pattern: `{service-name}-{date}-{index}.log`  
Example: `user-service-2025-10-17-1.log`

---

## How Logging Works

### Architecture Overview

```
Application Start
       ↓
TfmsApplication.main() sets default service context
       ↓
LoggingConfig initializes with Spring application name
       ↓
ServiceLogger sets service-specific context per log call
       ↓
Log4j2 Routing Appender routes to appropriate log file
       ↓
{service-name}.log file created automatically
```

### Key Components

1. **TfmsApplication.java** (Entry Point)
   - Sets initial logging context before Spring starts
   - Ensures all startup logs have proper service name

2. **LoggingConfig.java** (Configuration)
   - Updates context with Spring application name
   - Runs after Spring container initializes
   - Sets environment and application metadata

3. **ServiceLogger.java** (Logging Utility)
   - Wrapper around Log4j2
   - Automatically sets service context
   - Provides clean API for logging

4. **ServiceName.java** (Service Registry)
   - Enum containing all known services
   - Used to identify which service is logging

5. **log4j2-{env}.xml** (Log4j2 Config)
   - Defines log file locations
   - Configures routing based on service context
   - Sets up appenders and patterns

---

## Maven Dependencies Configuration

### Important: Logging Dependency Exclusions

The TFMS project uses **Log4j2** as the logging framework. To prevent conflicts with Spring Boot's default logging (Logback), you must **exclude `spring-boot-starter-logging`** from any Spring Boot starter dependencies.

#### Why This Is Necessary

Spring Boot includes `spring-boot-starter-logging` by default, which contains:
- **Logback** (different logging implementation)
- **`log4j-to-slf4j`** (creates a circular routing conflict with our `log4j-slf4j2-impl`)

Having both `log4j-slf4j2-impl` (SLF4J → Log4j2) and `log4j-to-slf4j` (Log4j2 → SLF4J) in the classpath causes this error:

```
org.apache.logging.log4j.LoggingException: log4j-slf4j2-impl cannot be present with log4j-to-slf4j
```

#### Required Exclusions

**For modules that depend on `common-modules`:**

When adding the `common-modules` dependency, you don't need any exclusions as they're already configured in `common-modules` itself.

```xml
<dependency>
    <groupId>com.saxion.proj</groupId>
    <artifactId>common-modules</artifactId>
    <version>${project.version}</version>
</dependency>
```

**For any Spring Boot starter dependencies in your module:**

Exclude `spring-boot-starter-logging` from ALL Spring Boot starters:

```xml
<!-- Example: Spring Boot Starter Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Example: Spring Boot Starter Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Example: Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### Complete Example: auth-service pom.xml

Here's a complete example from the `auth-service` module:

```xml
<dependencies>
    <!-- Commons Module Dependency - No exclusions needed -->
    <dependency>
        <groupId>com.saxion.proj</groupId>
        <artifactId>common-modules</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Spring Dependencies - No exclusions for these -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>

    <!-- Spring Boot Starters - MUST exclude logging -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <!-- Note: common-modules already excludes log4j-to-slf4j from test dependencies -->
    </dependency>
    
    <!-- Mockito inline for testing (required for mocking final classes) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-inline</artifactId>
        <version>5.2.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Quick Rule of Thumb

**Exclude `spring-boot-starter-logging` from:**
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- Any other `spring-boot-starter-*` dependency

**Don't exclude from:**
- `spring-web`, `spring-webmvc` (these are not starters)
- `spring-security-core`, `spring-security-crypto` (not starters)
- `common-modules` (already configured)

#### Verification

After configuring dependencies, verify the build:

```bash
# Clean build
mvn clean install

# Check for logging conflicts
mvn dependency:tree | grep -i "log4j"
```

You should see only:
- `log4j-api`
- `log4j-core`
- `log4j-slf4j2-impl`
- `log4j-layout-template-json`

You should NOT see:
- `log4j-to-slf4j` (this causes conflicts)
- `logback-classic`
- `logback-core`

---

## Using the Logger

### Basic Usage

#### Step 1: Import the Logger
```java
import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
```

#### Step 2: Create Logger Instance
```java
public class YourClass {
    private static final ServiceLogger logger = ServiceLogger.getLogger(YourClass.class);
    
    // Your code here
}
```

#### Step 3: Log Messages

**Simple Logging:**
```java
// Info level
logger.info(ServiceName.USER_SERVICE, "User login successful for: {}", username);

// Debug level
logger.debug(ServiceName.USER_SERVICE, "Processing user data: {}", userData);

// Warning level
logger.warn(ServiceName.SECURITY_SERVICE, "Failed login attempt from IP: {}", ipAddress);

// Error level
logger.error(ServiceName.USER_SERVICE, "Failed to create user: {}", errorMessage);
```

**Logging with Operations (for tracking specific operations):**
```java
// With operation context
logger.infoOp(ServiceName.ORDER_SERVICE, "CREATE_ORDER", 
    "Order created successfully: {}", orderId);

logger.debugOp(ServiceName.PACKAGE_SERVICE, "TRACK_PACKAGE", 
    "Package location updated: {}", location);

logger.errorOp(ServiceName.NOTIFICATION_SERVICE, "SEND_EMAIL", 
    "Failed to send email to: {}", emailAddress);

logger.warnOp(ServiceName.SECURITY_SERVICE, "FAILED_LOGIN", 
    "Login attempt failed for user: {}", username);
```

**Advanced Debug Logging (with performance optimization):**
```java
// Check if debug is enabled before expensive operations
if (logger.isDebugEnabled()) {
    String expensiveDebugInfo = buildComplexDebugMessage(); // Only runs if DEBUG is ON
    logger.debug(ServiceName.ORDER_SERVICE, "Debug info: {}", expensiveDebugInfo);
}

// Debug with operation tracking
logger.debugOp(ServiceName.PAYMENT_SERVICE, "CALCULATE_TAX", 
    "Tax calculation: base={}, rate={}, total={}", baseAmount, taxRate, totalTax);

// Debug for method entry/exit
logger.debug(ServiceName.USER_SERVICE, "Entering validateUser() with userId: {}", userId);
// ... method logic
logger.debug(ServiceName.USER_SERVICE, "Exiting validateUser(), result: {}", isValid);
```

**Logging Exceptions:**
```java
try {
    // Your code
} catch (Exception e) {
    logger.error(ServiceName.USER_SERVICE, "Error processing request", e);
    // OR with operation
    logger.errorOp(ServiceName.USER_SERVICE, "PROCESS_REQUEST", 
        "Error during request processing", e);
}
```

### Log Output Format

Each log entry contains:
```
2025-10-17 20:00:21.358 [thread-name] LEVEL [service-name] [operation] fully.qualified.ClassName - Message
```

Example:
```
2025-10-17 20:00:18.725 [main] INFO [user-service] [USER_LOGIN] com.saxion.proj.tfms.service.UserService - User login successful for: john.doe
```

---

## Available Logging Methods

The `ServiceLogger` provides multiple methods for different logging needs:

### Simple Logging (without operation context)

| Method | Level | Usage |
|--------|-------|-------|
| `debug(service, message, params...)` | DEBUG | Detailed debug information |
| `info(service, message, params...)` | INFO | General information |
| `warn(service, message, params...)` | WARN | Warning messages |
| `error(service, message, params...)` | ERROR | Error messages |
| `error(service, message, throwable)` | ERROR | Error with exception |

### Operation-Based Logging (with operation tracking)

| Method | Level | Usage |
|--------|-------|-------|
| `debugOp(service, operation, message, params...)` | DEBUG | Debug with operation context |
| `infoOp(service, operation, message, params...)` | INFO | Info with operation context |
| `warnOp(service, operation, message, params...)` | WARN | Warning with operation context |
| `errorOp(service, operation, message, params...)` | ERROR | Error with operation context |
| `errorOp(service, operation, message, throwable)` | ERROR | Error with exception and operation |

### Helper Methods (performance optimization)

| Method | Returns | Usage |
|--------|---------|-------|
| `isDebugEnabled()` | boolean | Check if DEBUG is enabled |
| `isInfoEnabled()` | boolean | Check if INFO is enabled |
| `isWarnEnabled()` | boolean | Check if WARN is enabled |
| `isErrorEnabled()` | boolean | Check if ERROR is enabled |

### Usage Examples by Scenario

**1. Simple Debug Message:**
```java
logger.debug(ServiceName.USER_SERVICE, "Validating user credentials");
```

**2. Debug with Parameters:**
```java
logger.debug(ServiceName.ORDER_SERVICE, 
    "Found {} items for order {}", itemCount, orderId);
```

**3. Debug with Operation:**
```java
logger.debugOp(ServiceName.PAYMENT_SERVICE, "VALIDATE_CARD", 
    "Validating card ending in: {}", lastFourDigits);
```

**4. Conditional Debug (Performance Optimized):**
```java
if (logger.isDebugEnabled()) {
    String complexData = buildComplexDebugString(order); // Expensive operation
    logger.debugOp(ServiceName.ORDER_SERVICE, "PROCESS_ORDER", 
        "Complex order data: {}", complexData);
}
```

**5. Info with Operation:**
```java
logger.infoOp(ServiceName.USER_SERVICE, "CREATE_USER", 
    "User created successfully: {}", userId);
```

**6. Warning with Operation:**
```java
logger.warnOp(ServiceName.SECURITY_SERVICE, "RATE_LIMIT", 
    "Rate limit exceeded for IP: {}", ipAddress);
```

**7. Error with Exception:**
```java
try {
    processPayment(order);
} catch (PaymentException e) {
    logger.error(ServiceName.PAYMENT_SERVICE, 
        "Payment processing failed for order: {}", orderId, e);
}
```

**8. Error with Operation and Exception:**
```java
try {
    sendNotification(user);
} catch (Exception e) {
    logger.errorOp(ServiceName.NOTIFICATION_SERVICE, "SEND_EMAIL", 
        "Failed to send email to: {}", user.getEmail(), e);
}
```

---

## Adding a New Module/Service

When you create a new module or service, follow these steps:

### Step 1: Add Service to ServiceName Enum

**File:** `/tfms-modules/common-modules/src/main/java/com/saxion/proj/tfms/commons/logging/ServiceName.java`

```java
public enum ServiceName {
    USER_SERVICE("user-service"),
    ORDER_SERVICE("order-service"),
    SECURITY_SERVICE("security-service"),
    NOTIFICATION_SERVICE("notification-service"),
    DRIVER_SERVICE("driver-service"),
    COMMON_SERVICE("common-service"),
    PACKAGE_SERVICE("package-service"),
    
    // ADD YOUR NEW SERVICE HERE
    VEHICLE_SERVICE("vehicle-service"),        // Example: Vehicle management
    MAINTENANCE_SERVICE("maintenance-service"), // Example: Maintenance tracking
    INVOICE_SERVICE("invoice-service");        // Example: Billing/Invoicing

    private final String serviceName;

    ServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return serviceName;
    }
}
```

### Step 2: Use the Logger in Your New Service

**Example:** Creating a Vehicle Service

```java
package com.saxion.proj.tfms.vehicle.service;

import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    
    // Create logger instance
    private static final ServiceLogger logger = ServiceLogger.getLogger(VehicleService.class);
    
    public Vehicle createVehicle(VehicleDto vehicleDto) {
        logger.info(ServiceName.VEHICLE_SERVICE, "Creating new vehicle: {}", vehicleDto.getVehicleNumber());
        
        try {
            // Your business logic
            Vehicle vehicle = // ... save vehicle
            
            logger.infoOp(ServiceName.VEHICLE_SERVICE, "CREATE_VEHICLE", 
                "Vehicle created successfully with ID: {}", vehicle.getId());
            
            return vehicle;
            
        } catch (Exception e) {
            logger.error(ServiceName.VEHICLE_SERVICE, "Failed to create vehicle", e);
            throw e;
        }
    }
    
    public Vehicle updateVehicle(Long id, VehicleDto vehicleDto) {
        logger.debugOp(ServiceName.VEHICLE_SERVICE, "UPDATE_VEHICLE", 
            "Updating vehicle ID: {}", id);
        
        // Your update logic
        
        logger.info(ServiceName.VEHICLE_SERVICE, "Vehicle updated: {}", id);
        return updatedVehicle;
    }
}
```

### Step 3: Rebuild and Test

```bash
# Rebuild common-modules (contains ServiceName enum)
cd /home/prithvish/Documents/GitHub/02/tfms-modules/common-modules
mvn clean install -DskipTests

# Rebuild entire project
cd /home/prithvish/Documents/GitHub/02
mvn clean package -DskipTests

# Restart application
./scripts/start-dev.sh
```

### Step 4: Verify Log File Creation

After your service runs, check the logs directory:
```bash
ls -la /home/prithvish/Documents/GitHub/02/logs/
```

You should see your new service log file:
```
vehicle-service.log
```

---

## Log Levels

Use appropriate log levels for different scenarios:

| Level | When to Use | Example |
|-------|-------------|---------|
| **DEBUG** | Detailed diagnostic information for development | `logger.debug(ServiceName.ORDER_SERVICE, "Processing order items: {}", items)` |
| **INFO** | General informational messages | `logger.info(ServiceName.USER_SERVICE, "User created: {}", userId)` |
| **WARN** | Warning messages, potential issues | `logger.warn(ServiceName.SECURITY_SERVICE, "Multiple failed login attempts")` |
| **ERROR** | Error events, exceptions | `logger.error(ServiceName.PAYMENT_SERVICE, "Payment failed", exception)` |

### Log Level Hierarchy
```
ERROR > WARN > INFO > DEBUG

If level is set to INFO:
- ERROR - logged
- WARN  - logged
- INFO  - logged
- DEBUG - NOT logged
```

### Performance Optimization with Debug Checks

**Why use `isDebugEnabled()`?**

When DEBUG level is disabled in production, the log messages are still evaluated (expensive operations like string concatenation, object serialization). Use the check methods to avoid this:

```java
// BAD - Always executes expensive operation even if DEBUG is disabled
logger.debug(ServiceName.ORDER_SERVICE, 
    "Order details: {}", order.toDetailedString()); // toDetailedString() always runs!

// GOOD - Only executes expensive operation when DEBUG is enabled
if (logger.isDebugEnabled()) {
    logger.debug(ServiceName.ORDER_SERVICE, 
        "Order details: {}", order.toDetailedString()); // Only runs if DEBUG is ON
}
```

**Available Check Methods:**
```java
logger.isDebugEnabled()  // Check if DEBUG level is enabled
logger.isInfoEnabled()   // Check if INFO level is enabled
logger.isWarnEnabled()   // Check if WARN level is enabled
logger.isErrorEnabled()  // Check if ERROR level is enabled
```

**Real-World Example:**
```java
public void processComplexOrder(Order order) {
    logger.info(ServiceName.ORDER_SERVICE, "Processing order: {}", order.getId());
    
    // Expensive debug logging with guard
    if (logger.isDebugEnabled()) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("items", order.getItems().stream()
            .map(Item::toDetailedMap)
            .collect(Collectors.toList()));
        debugInfo.put("customer", order.getCustomer().toDebugString());
        debugInfo.put("pricing", calculateDetailedPricing(order));
        
        logger.debug(ServiceName.ORDER_SERVICE, 
            "Order debug info: {}", debugInfo);
    }
    
    // Process order...
}
```

### Environment-Specific Levels

**Development (dev profile):**
- Application logs: `DEBUG` level
- Framework logs: `INFO` level

**Production (prod profile):**
- Application logs: `INFO` level  
- Framework logs: `WARN` level

---

## Configuration Files

### Log4j2 Configuration Files

#### Development: `log4j2-dev.xml`
**Location:** `/tfms-starter/src/main/resources/log4j2-dev.xml`

```xml
<Configuration status="WARN">
    <Properties>
        <!-- Log pattern with service and operation context -->
        <Property name="LOG_PATTERN">
            %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level [%X{service}] [%X{operation}] %logger{36} - %msg%n
        </Property>
        
        <!-- Log directory: inside project -->
        <Property name="APP_LOG_ROOT">${sys:LOG_PATH:-${sys:user.dir}/logs}</Property>
    </Properties>
    
    <Appenders>
        <!-- Console output -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
        
        <!-- Service-based routing -->
        <Routing name="ServiceRoutingAppender">
            <Routes pattern="$${ctx:service}">
                <Route>
                    <RollingFile name="Rolling-${ctx:service}" 
                                 fileName="${APP_LOG_ROOT}/${ctx:service}.log"
                                 filePattern="${APP_LOG_ROOT}/${ctx:service}-%d{yyyy-MM-dd}-%i.log">
                        <PatternLayout pattern="${LOG_PATTERN}"/>
                        <Policies>
                            <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                            <SizeBasedTriggeringPolicy size="10MB"/>
                        </Policies>
                        <DefaultRolloverStrategy max="10"/>
                    </RollingFile>
                </Route>
            </Routes>
        </Routing>
    </Appenders>
</Configuration>
```

#### Production: `log4j2-prod.xml`
**Location:** `/tfms-starter/src/main/resources/log4j2-prod.xml`

- Uses JSON format for ELK stack integration
- Larger file sizes (50MB) before rotation
- More retention (30 files)
- Structured logging for better parsing

### Changing Log Configuration

#### Change Log Directory
Edit both `log4j2-dev.xml` and `log4j2-prod.xml`:

```xml
<Property name="APP_LOG_ROOT">/your/custom/path/logs</Property>
```

Or set environment variable:
```bash
export LOG_PATH=/your/custom/path/logs
```

#### Change Rotation Size
```xml
<SizeBasedTriggeringPolicy size="20MB"/>  <!-- Default: 10MB dev, 50MB prod -->
```

#### Change Retention Count
```xml
<DefaultRolloverStrategy max="20"/>  <!-- Default: 10 dev, 30 prod -->
```

---

## Troubleshooting

### Issue 1: Log Files Not Created

**Symptoms:**
- No log files in `/home/prithvish/Documents/GitHub/02/logs/`
- Logs only showing in console

**Solutions:**

1. **Check directory permissions:**
   ```bash
   ls -la /home/prithvish/Documents/GitHub/02/logs/
   chmod 755 /home/prithvish/Documents/GitHub/02/logs/
   ```

2. **Verify Log4j2 configuration is loaded:**
   ```bash
   # Check application startup logs
   tail -f /home/prithvish/Documents/GitHub/02/backend.log | grep -i "log"
   ```

3. **Ensure logging context is set:**
   - Check `TfmsApplication.java` has ThreadContext initialization
   - Check `LoggingConfig.java` is being loaded

### Issue 2: Wrong Service Name in Logs

**Symptoms:**
- Logs showing `[${ctx:service}]` instead of actual service name
- Logs going to wrong service file

**Solutions:**

1. **Rebuild common-modules:**
   ```bash
   cd /home/prithvish/Documents/GitHub/02/tfms-modules/common-modules
   mvn clean install -DskipTests
   ```

2. **Check ServiceLogger usage:**
   ```java
   // Wrong - missing service name
   logger.info("Message");
   
   // Correct
   logger.info(ServiceName.YOUR_SERVICE, "Message");
   ```

### Issue 3: Logs Not Rotating

**Symptoms:**
- Log file growing very large
- No rotated files with date suffix

**Solutions:**

1. **Check rotation policy in log4j2 config:**
   ```xml
   <Policies>
       <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
       <SizeBasedTriggeringPolicy size="10MB"/>
   </Policies>
   ```

2. **Verify file permissions:**
   ```bash
   ls -la /home/prithvish/Documents/GitHub/02/logs/
   ```

### Issue 4: Missing Logs in Production

**Symptoms:**
- DEBUG logs not appearing in production
- Only ERROR logs visible

**Solutions:**

1. **Check active profile:**
   ```bash
   # In application.properties
   spring.profiles.active=prod
   ```

2. **Review log levels in log4j2-prod.xml:**
   ```xml
   <!-- TFMS Application Loggers -->
   <Logger name="com.saxion.proj.tfms" level="info" additivity="false">
       <!-- Change to debug if needed -->
   </Logger>
   ```

### Issue 5: Duplicate Log Entries

**Symptoms:**
- Same log message appearing multiple times
- Logs appearing in multiple files

**Solutions:**

1. **Check additivity setting:**
   ```xml
   <Logger name="com.saxion.proj.tfms" level="debug" additivity="false">
       <!-- additivity="false" prevents propagation to parent -->
   </Logger>
   ```

2. **Remove duplicate appender references:**
   - Ensure each logger references appenders only once

---

## Best Practices

### 1. Always Use Appropriate Service Name
```java
// Bad - using wrong service
logger.info(ServiceName.COMMON_SERVICE, "Creating order...");

// Good - using correct service
logger.info(ServiceName.ORDER_SERVICE, "Creating order...");
```

### 2. Use Parameterized Logging
```java
// Bad - string concatenation
logger.info(ServiceName.USER_SERVICE, "User " + username + " logged in");

// Good - parameterized
logger.info(ServiceName.USER_SERVICE, "User {} logged in", username);
```

### 3. Log at Appropriate Levels
```java
// Bad - using ERROR for info
logger.error(ServiceName.USER_SERVICE, "User created successfully");

// Good - using INFO
logger.info(ServiceName.USER_SERVICE, "User created successfully");
```

### 4. Include Context in Error Logs
```java
// Bad - no context
logger.error(ServiceName.ORDER_SERVICE, "Error occurred", e);

// Good - includes context
logger.error(ServiceName.ORDER_SERVICE, 
    "Failed to create order for user: {} with items: {}", userId, itemCount, e);
```

### 5. Use Operations for Business Workflows
```java
// Track important operations
logger.infoOp(ServiceName.PAYMENT_SERVICE, "PROCESS_PAYMENT", 
    "Payment processed for order: {}, amount: {}", orderId, amount);

logger.infoOp(ServiceName.NOTIFICATION_SERVICE, "SEND_SMS", 
    "SMS sent to: {}, status: {}", phoneNumber, status);
```

### 6. Don't Log Sensitive Information
```java
// Bad - logging sensitive data
logger.info(ServiceName.SECURITY_SERVICE, "Password: {}", password);
logger.info(ServiceName.PAYMENT_SERVICE, "Credit card: {}", creditCard);

// Good - masking sensitive data
logger.info(ServiceName.SECURITY_SERVICE, "Password updated for user: {}", userId);
logger.info(ServiceName.PAYMENT_SERVICE, "Payment processed for card ending: {}", 
    cardNumber.substring(cardNumber.length() - 4));
```

### 7. Log Entry and Exit of Critical Methods
```java
public Order processOrder(OrderRequest request) {
    logger.info(ServiceName.ORDER_SERVICE, "Processing order request for user: {}", 
        request.getUserId());
    
    try {
        // Business logic
        Order order = createOrder(request);
        
        logger.info(ServiceName.ORDER_SERVICE, "Order processed successfully: {}", 
            order.getId());
        return order;
        
    } catch (Exception e) {
        logger.error(ServiceName.ORDER_SERVICE, 
            "Failed to process order for user: {}", request.getUserId(), e);
        throw e;
    }
}
```

### 8. Use Debug for Development Details
```java
public void calculateRoute(Vehicle vehicle, Location destination) {
    logger.info(ServiceName.DRIVER_SERVICE, "Calculating route to: {}", destination);
    
    // Detailed debug info
    logger.debug(ServiceName.DRIVER_SERVICE, 
        "Vehicle details - ID: {}, Current location: {}, Fuel: {}", 
        vehicle.getId(), vehicle.getLocation(), vehicle.getFuelLevel());
    
    // ... route calculation
}
```

### 9. Create Service-Specific Loggers
```java
// Each class has its own logger
public class UserService {
    private static final ServiceLogger logger = ServiceLogger.getLogger(UserService.class);
}

public class OrderService {
    private static final ServiceLogger logger = ServiceLogger.getLogger(OrderService.class);
}
```

### 10. Document Important Log Points
```java
/**
 * Processes the payment for an order.
 * 
 * Logging:
 * - INFO: Payment initiation and completion
 * - ERROR: Payment failures with reason
 * - DEBUG: Payment gateway request/response details
 */
public PaymentResult processPayment(Order order) {
    logger.infoOp(ServiceName.PAYMENT_SERVICE, "PROCESS_PAYMENT", 
        "Initiating payment for order: {}", order.getId());
    // ...
}
```

---

## Quick Reference

### Common Commands

```bash
# View all log files
ls -lh /home/prithvish/Documents/GitHub/02/logs/

# Monitor main application log
tail -f /home/prithvish/Documents/GitHub/02/logs/tfms-application.log

# Monitor specific service log
tail -f /home/prithvish/Documents/GitHub/02/logs/user-service.log

# Monitor error log only
tail -f /home/prithvish/Documents/GitHub/02/logs/error.log

# Search for specific text in logs
grep -r "error" /home/prithvish/Documents/GitHub/02/logs/

# Find logs from specific time
grep "2025-10-17 20:" /home/prithvish/Documents/GitHub/02/logs/tfms-application.log

# Count ERROR entries
grep -c "ERROR" /home/prithvish/Documents/GitHub/02/logs/error.log

# Clean old logs
find /home/prithvish/Documents/GitHub/02/logs/ -name "*.log" -mtime +30 -delete
```

### Service Names Reference

| Service Name | Purpose | Example Usage |
|--------------|---------|---------------|
| `COMMON_SERVICE` | Common utilities | `logger.info(ServiceName.COMMON_SERVICE, "...")` |
| `USER_SERVICE` | User management | `logger.info(ServiceName.USER_SERVICE, "...")` |
| `ORDER_SERVICE` | Order processing | `logger.info(ServiceName.ORDER_SERVICE, "...")` |
| `SECURITY_SERVICE` | Security/Auth | `logger.info(ServiceName.SECURITY_SERVICE, "...")` |
| `NOTIFICATION_SERVICE` | Notifications | `logger.info(ServiceName.NOTIFICATION_SERVICE, "...")` |
| `DRIVER_SERVICE` | Driver management | `logger.info(ServiceName.DRIVER_SERVICE, "...")` |
| `PACKAGE_SERVICE` | Package tracking | `logger.info(ServiceName.PACKAGE_SERVICE, "...")` |
