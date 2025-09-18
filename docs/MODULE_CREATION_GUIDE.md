# Module Creation Guide - TFMS

## Table of Contents
- [Module Structure Overview](#module-structure-overview)
- [Module Categories](#module-categories)
- [Creating a New Module](#creating-a-new-module)
- [Module Integration](#module-integration)
- [Testing Your Module](#testing-your-module)
- [Best Practices](#best-practices)
- [Module Creation Checklist](#module-creation-checklist)

## Module Structure Overview

The TFMS follows a modular monolith architecture with the following structure:

```
modules/
├── shared-modules/         # Common utilities and shared code
├── core-modules/           # Core business entities
├── business-modules/       # Business logic and workflows  
├── communication-modules/  # External communications
├── auth-modules/           # Authentication and authorization
└── platform-modules/      # Infrastructure concerns
```

## Module Categories

### 1. **shared-modules**
Common utilities, shared DTOs, and cross-cutting concerns
- `common-utils` - Utility classes, constants, helpers

### 2. **core-modules** 
Core business entities and their basic CRUD operations
- `customer-service` - Customer management
- `driver-service` - Driver management
- `order-service` - Order management
- `truck-service` - Truck/vehicle management
- `tracking-service` - Location and tracking

### 3. **business-modules**
Complex business logic and workflows
- `assignment-module` - Order-driver-truck assignment logic
- `document-management-service` - Document handling workflows

### 4. **communication-modules**
External communications and integrations
- `notification-service` - Email, SMS, push notifications

### 5. **auth-modules**
Authentication and authorization
- `auth-service` - User authentication and JWT handling

### 6. **platform-modules**
Infrastructure and monitoring
- `analytics-service` - Business analytics and reporting
- `monitoring-module` - Health checks and metrics

## Creating a New Module

### Step 1: Choose Module Category

Determine where your module belongs:
- **core-modules**: Main business entity (e.g., `inventory-service`, `maintenance-service`)
- **business-modules**: Complex workflows (e.g., `route-optimization`, `billing-module`)
- **communication-modules**: External integrations (e.g., `gps-integration`, `payment-gateway`)
- **auth-modules**: Security features (e.g., `role-management`, `audit-service`)
- **platform-modules**: Infrastructure (e.g., `reporting-service`, `backup-service`)

### Step 2: Create Directory Structure

Example for creating `inventory-service` in core-modules:

```bash
mkdir -p modules/core-modules/inventory-service/src/main/java/com/saxion/proj/tfms/inventory/{model,repository,service,controller}
mkdir -p modules/core-modules/inventory-service/src/test/java/com/saxion/proj/tfms/inventory
```

### Step 3: Create Module POM

Create `modules/core-modules/inventory-service/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.saxion.proj</groupId>
        <artifactId>truck-management-monolith</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../../pom.xml</relativePath>
    </parent>

    <artifactId>inventory-service</artifactId>
    <name>TFMS Inventory Module</name>
    <description>Inventory management module for TFMS</description>
    <packaging>jar</packaging>

    <dependencies>
        <!-- Spring Web -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
        </dependency>

        <!-- JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Common utilities (if needed) -->
        <dependency>
            <groupId>com.saxion.proj</groupId>
            <artifactId>common-utils</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Test dependencies -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-deploy-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 4: Create Module Classes

#### 4.1 Entity Model
`src/main/java/com/saxion/proj/tfms/inventory/model/InventoryItem.java`:

```java
package com.saxion.proj.tfms.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public InventoryItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public InventoryItem(String name, String description, Integer quantity, Double unitPrice) {
        this();
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

#### 4.2 Repository
`src/main/java/com/saxion/proj/tfms/inventory/repository/InventoryRepository.java`:

```java
package com.saxion.proj.tfms.inventory.repository;

import com.saxion.proj.tfms.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    
    List<InventoryItem> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < :threshold")
    List<InventoryItem> findLowStockItems(@Param("threshold") Integer threshold);
}
```

#### 4.3 Service
`src/main/java/com/saxion/proj/tfms/inventory/service/InventoryService.java`:

```java
package com.saxion.proj.tfms.inventory.service;

import com.saxion.proj.tfms.inventory.model.InventoryItem;
import com.saxion.proj.tfms.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryRepository.findById(id);
    }

    public List<InventoryItem> searchItemsByName(String name) {
        return inventoryRepository.findByNameContainingIgnoreCase(name);
    }

    public List<InventoryItem> getLowStockItems(Integer threshold) {
        return inventoryRepository.findLowStockItems(threshold);
    }

    public InventoryItem createItem(InventoryItem item) {
        return inventoryRepository.save(item);
    }

    public InventoryItem updateItem(Long id, InventoryItem itemDetails) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));

        item.setName(itemDetails.getName());
        item.setDescription(itemDetails.getDescription());
        item.setQuantity(itemDetails.getQuantity());
        item.setUnitPrice(itemDetails.getUnitPrice());

        return inventoryRepository.save(item);
    }

    public void deleteItem(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new RuntimeException("Inventory item not found with id: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}
```

#### 4.4 Controller
`src/main/java/com/saxion/proj/tfms/inventory/controller/InventoryController.java`:

```java
package com.saxion.proj.tfms.inventory.controller;

import com.saxion.proj.tfms.inventory.model.InventoryItem;
import com.saxion.proj.tfms.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "inventory-service");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllItems() {
        List<InventoryItem> items = inventoryService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        Optional<InventoryItem> item = inventoryService.getItemById(id);
        return item.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<InventoryItem>> searchItems(@RequestParam String name) {
        List<InventoryItem> items = inventoryService.searchItemsByName(name);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems(@RequestParam(defaultValue = "10") Integer threshold) {
        List<InventoryItem> items = inventoryService.getLowStockItems(threshold);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItem item) {
        try {
            InventoryItem createdItem = inventoryService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem itemDetails) {
        try {
            InventoryItem updatedItem = inventoryService.updateItem(id, itemDetails);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## Module Registration and Integration

### Step 1: Register Module in Parent POM

**CRITICAL**: Add your module to the root `pom.xml` modules section:

```xml
<modules>
    <!-- Shared Modules (Built First) -->
    <module>modules/shared-modules/common-utils</module>
    
    <!-- Core Business Modules -->
    <module>modules/core-modules/customer-service</module>
    <module>modules/core-modules/driver-service</module>
    <module>modules/core-modules/order-service</module>
    <module>modules/core-modules/truck-service</module>
    <module>modules/core-modules/tracking-service</module>
    
    <!-- ADD YOUR NEW MODULE HERE -->
    <module>modules/core-modules/inventory-service</module>
    
    <!-- Business Logic Modules -->
    <module>modules/business-modules/assignment-module</module>
    <module>modules/business-modules/document-management-service</module>
    
    <!-- Communication Modules -->
    <module>modules/communication-modules/notification-service</module>
    
    <!-- Authentication & Security Modules -->
    <module>modules/auth-modules/auth-service</module>
    
    <!-- Platform & Monitoring Modules -->
    <module>modules/platform-modules/log-modules/monitoring-module/analytics-service</module>
    
    <!-- Main Application (Built Last) -->
    <module>tfms-starter</module>
</modules>
```

**Why this matters:**
- Maven builds modules in the order listed
- Shared modules must be built before modules that depend on them
- The starter application must be built last to include all modules

### Step 2: Add Module Dependency to Starter Application

Add your module as a dependency in `tfms-starter/pom.xml`:

```xml
<dependencies>
    <!-- Existing dependencies... -->
    
    <!-- ADD YOUR MODULE DEPENDENCY -->
    <dependency>
        <groupId>com.saxion.proj</groupId>
        <artifactId>inventory-service</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Other dependencies... -->
</dependencies>
```

**Important Notes:**
- Use `${project.version}` to match the parent version
- The `artifactId` must match your module's `artifactId` in its `pom.xml`
- Add dependencies in the same order as module categories for consistency

### Step 3: Verify Component Scanning

Ensure your package structure follows the convention:
- Base package: `com.saxion.proj.tfms`
- Module package: `com.saxion.proj.tfms.[module-name]`

Example package structure:
```
com.saxion.proj.tfms.inventory
├── controller/
├── service/
├── repository/
└── model/
```

The main application's `@ComponentScan(basePackages = "com.saxion.proj.tfms")` will automatically discover your components.

### Step 4: Verify Module Registration

After adding your module, verify it's properly registered:

```bash
# 1. Check if module is included in build
./mvnw clean compile

# 2. Verify module appears in dependency tree
./mvnw dependency:tree -pl tfms-starter | grep your-module-name

# 3. Check if module classes are included in the final JAR
./mvnw clean package -DskipTests
jar -tf tfms-starter/target/tfms-starter-1.0.0-SNAPSHOT.jar | grep -i your-module-name

# 4. Verify application starts successfully
./mvnw spring-boot:run -pl tfms-starter
```

### Step 5: Test Module Integration

Test that your module is properly integrated:

```bash
# Start the application
./mvnw spring-boot:run -pl tfms-starter

# In another terminal, test your module's health endpoint
curl -X GET "http://localhost:8080/api/inventory/health"

# Expected response:
# {
#   "status": "UP",
#   "service": "inventory-service",
#   "timestamp": "2025-09-18T10:30:00"
# }
```

### Common Registration Issues and Solutions

#### Issue 1: Module Not Built
**Symptom**: Module classes not found during compilation
**Solution**: Ensure module is added to parent `pom.xml` modules section

#### Issue 2: Dependency Not Resolved
**Symptom**: `Could not resolve dependencies` error
**Solution**: 
- Check `artifactId` matches between module and starter POMs
- Ensure module is built before starter (correct order in modules section)
- Run `./mvnw clean install` to rebuild all modules

#### Issue 3: Components Not Scanned
**Symptom**: `NoSuchBeanDefinitionException` at runtime
**Solution**:
- Verify package structure: `com.saxion.proj.tfms.[module-name]`
- Check component annotations (`@Service`, `@Repository`, `@Controller`)
- Ensure no typos in package names

#### Issue 4: Module JAR Not Included
**Symptom**: Classes not found in final application JAR
**Solution**:
- Verify dependency is added to `tfms-starter/pom.xml`
- Check the maven-dependency-plugin configuration in starter
- Run `./mvnw clean package -DskipTests` and inspect JAR contents

### Build Order Example

When you run `./mvnw clean install`, Maven builds in this order:

1. **shared-modules/common-utils** (foundation)
2. **core-modules/** (business entities)
3. **business-modules/** (business logic)
4. **communication-modules/** (external integrations)
5. **auth-modules/** (security)
6. **platform-modules/** (infrastructure)
7. **tfms-starter** (main application - includes all modules)

### Verification Checklist

- [ ] Module added to parent `pom.xml` modules section
- [ ] Module dependency added to `tfms-starter/pom.xml`
- [ ] Package structure follows `com.saxion.proj.tfms.[module-name]` convention
- [ ] Build succeeds: `./mvnw clean package -DskipTests`
- [ ] Module classes appear in final JAR
- [ ] Application starts successfully
- [ ] Module endpoints are accessible
- [ ] Health check returns expected response

## Testing Your Module

### Unit Test Example

Create `src/test/java/com/saxion/proj/tfms/inventory/service/InventoryServiceTest.java`:

```java
package com.saxion.proj.tfms.inventory.service;

import com.saxion.proj.tfms.inventory.model.InventoryItem;
import com.saxion.proj.tfms.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllItems_ReturnsAllItems() {
        // Given
        List<InventoryItem> items = Arrays.asList(
            new InventoryItem("Item 1", "Description 1", 10, 100.0),
            new InventoryItem("Item 2", "Description 2", 20, 200.0)
        );
        when(inventoryRepository.findAll()).thenReturn(items);

        // When
        List<InventoryItem> result = inventoryService.getAllItems();

        // Then
        assertEquals(2, result.size());
        verify(inventoryRepository).findAll();
    }

    @Test
    void getItemById_ExistingId_ReturnsItem() {
        // Given
        Long id = 1L;
        InventoryItem item = new InventoryItem("Item 1", "Description 1", 10, 100.0);
        when(inventoryRepository.findById(id)).thenReturn(Optional.of(item));

        // When
        Optional<InventoryItem> result = inventoryService.getItemById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Item 1", result.get().getName());
    }
}
```

### Testing Commands

```bash
# Test specific module
./mvnw test -pl modules/core-modules/inventory-service

# Build and test module
./mvnw clean package -pl modules/core-modules/inventory-service

# Build entire project
./mvnw clean package -DskipTests

# Run application and test
./mvnw spring-boot:run -pl tfms-starter
curl -X GET "http://localhost:8080/api/inventory/health"
```

## Best Practices

### 1. Package Structure
- Follow convention: `com.saxion.proj.tfms.[module-name]`
- Organize by feature: `model`, `repository`, `service`, `controller`
- Keep related classes together

### 2. Naming Conventions
- Module names: descriptive and consistent (kebab-case for directories)
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE

### 3. Dependencies
- Only include what you need
- Prefer Spring's dependency injection
- Use interfaces for service contracts
- Avoid circular dependencies between modules

### 4. Error Handling
- Use appropriate HTTP status codes
- Provide meaningful error messages
- Use consistent exception handling patterns
- Log errors appropriately

### 5. API Design
- Follow RESTful conventions
- Include health check endpoints
- Use consistent response formats
- Implement proper validation

### 6. Testing
- Write unit tests for services
- Include integration tests for controllers
- Test error scenarios
- Maintain good test coverage

## Module Creation Checklist

- [ ] **Planning**
  - [ ] Determine module category
  - [ ] Define module responsibilities
  - [ ] Identify dependencies

- [ ] **Setup**
  - [ ] Create directory structure
  - [ ] Create module `pom.xml`
  - [ ] Add module to parent `pom.xml`

- [ ] **Implementation**
  - [ ] Create entity models with JPA annotations
  - [ ] Create repository interfaces
  - [ ] Implement service classes
  - [ ] Create REST controllers
  - [ ] Add health check endpoint

- [ ] **Registration & Integration**
  - [ ] Add module to parent `pom.xml` modules section (in correct build order)
  - [ ] Add module dependency to `tfms-starter/pom.xml`
  - [ ] Verify package structure follows `com.saxion.proj.tfms.[module-name]` convention
  - [ ] Build and verify module is included: `./mvnw clean package -DskipTests`
  - [ ] Check module classes in final JAR: `jar -tf tfms-starter/target/*.jar | grep module-name`
  - [ ] Test application startup and module integration

- [ ] **Testing**
  - [ ] Write unit tests for services
  - [ ] Write integration tests for controllers
  - [ ] Test all endpoints manually
  - [ ] Verify health check works

- [ ] **Documentation**
  - [ ] Add module to this guide (if creating new patterns)
  - [ ] Document any special configuration
  - [ ] Update API documentation

### Quick Commands

```bash
# Create module structure (replace 'inventory' with your module name)
MODULE_NAME="inventory"
MODULE_CATEGORY="core-modules"  # or business-modules, communication-modules, etc.

mkdir -p modules/${MODULE_CATEGORY}/${MODULE_NAME}-service/src/main/java/com/saxion/proj/tfms/${MODULE_NAME}/{model,repository,service,controller}
mkdir -p modules/${MODULE_CATEGORY}/${MODULE_NAME}-service/src/test/java/com/saxion/proj/tfms/${MODULE_NAME}

# Test module
./mvnw test -pl modules/${MODULE_CATEGORY}/${MODULE_NAME}-service

# Build module
./mvnw clean package -pl modules/${MODULE_CATEGORY}/${MODULE_NAME}-service

# Build entire project
./mvnw clean package -DskipTests
```

---

**Ready to create your module? Follow this guide step by step and refer to existing modules for examples! 🚀**
