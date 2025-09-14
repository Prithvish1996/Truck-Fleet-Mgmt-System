# Truck Fleet Management System - Project Structure

This document provides a comprehensive overview of our microservices-based truck fleet management platform, detailing the architectural decisions, service organization, and development guidelines.

## 🏗️ Architecture Overview

The system follows a **Domain-Driven Design (DDD)** approach with microservices architecture, organized into logical categories for better maintainability, scalability, and team ownership. The structure separates business logic, platform concerns, and shared utilities.

## 📁 Complete Directory Structure

```
├── microservices/                    # Business microservices (executable services)
│   ├── core-services/               # Core business domain services
│   │   ├── customer-service/        # Customer lifecycle management
│   │   │   ├── src/main/java/       # Service implementation
│   │   │   ├── src/test/java/       # Unit & integration tests
│   │   │   ├── Dockerfile           # Container configuration
│   │   │   ├── pom.xml              # Maven dependencies
│   │   │   └── application.yml      # Service configuration
│   │   ├── order-service/           # Order processing & lifecycle
│   │   ├── driver-service/          # Driver management & availability
│   │   ├── truck-service/           # Fleet management & maintenance
│   │   └── tracking-service/        # Real-time GPS tracking & routes
│   │
│   ├── business-services/           # Business support services
│   │   ├── billing-service/         # Invoicing & financial reporting
│   │   ├── payment-service/         # Payment processing & gateways
│   │   ├── assignment-service/      # Smart job assignment algorithms
│   │   └── document-mgmt-service/   # Document storage & compliance
│   │
│   ├── communication-services/      # Communication & messaging
│   │   ├── notification-service/    # Multi-channel notifications
│   │   │   ├── src/main/java/
│   │   │   │   └── com/saxion/proj/notification/
│   │   │   │       ├── controller/  # REST endpoints
│   │   │   │       ├── service/     # Business logic
│   │   │   │       ├── repository/  # Data access
│   │   │   │       └── config/      # Service configuration
│   │   │   ├── templates/           # Email/SMS templates
│   │   │   └── application.yml      # Service properties
│   │   ├── chat-service/            # Real-time messaging
│   │   └── call-service/            # Voice communication
│   │
│   └── infrastructure-services/     # Platform infrastructure
│       ├── auth-service/            # Authentication & authorization
│       │   ├── src/main/java/
│       │   │   └── com/saxion/proj/auth/
│       │   │       ├── security/    # Security configuration
│       │   │       ├── jwt/         # JWT token handling
│       │   │       └── oauth/       # OAuth2 integration
│       │   └── keystore/            # SSL certificates
│       ├── gateway-service/         # API Gateway & routing
│       │   ├── src/main/java/
│       │   │   └── com/saxion/proj/gateway/
│       │   │       ├── filter/      # Request filters
│       │   │       ├── config/      # Gateway configuration
│       │   │       └── security/    # Security filters
│       │   └── routes.yml           # Route definitions
│       └── discovery-service/       # Service registry (Eureka)
│
├── platform/                       # Platform microservices (executable services)
│   ├── monitoring/                  # Monitoring & observability
│   │   └── analytics-service/       # Business analytics & reporting
│   │       ├── src/main/java/
│   │       │   └── com/saxion/proj/analytics/
│   │       │       ├── dashboard/   # Dashboard controllers
│   │       │       ├── metrics/     # Metrics collection
│   │       │       ├── reports/     # Report generation
│   │       │       └── scheduler/   # Scheduled analytics
│   │       ├── dashboards/          # Grafana dashboards
│   │       └── queries/             # SQL queries & views
│   ├── logging/                     # Centralized logging
│   │   └── log-monitoring/          # Log aggregation (ELK stack)
│   │       ├── src/main/java/
│   │       ├── logstash/            # Log processing configs
│   │       ├── elasticsearch/       # Search configurations
│   │       └── kibana/              # Visualization configs
│   ├── maintenance/                 # System maintenance
│   │   └── maintenance-service/     # Scheduled tasks & health checks
│   │       ├── src/main/java/
│   │       │   └── com/saxion/proj/maintenance/
│   │       │       ├── scheduler/   # Cron jobs
│   │       │       ├── health/      # Health checks
│   │       │       └── cleanup/     # Data cleanup tasks
│   │       └── schedules/           # Maintenance schedules
│   └── security/                    # Security services (future)
│       └── threat-detection/        # Security monitoring (placeholder)
│
├── shared/                          # Shared libraries (JAR dependencies)
│   ├── common/                      # Common domain objects
│   │   ├── common-dto/              # Data Transfer Objects
│   │   │   ├── src/main/java/
│   │   │   │   └── com/saxion/proj/common/dto/
│   │   │   │       ├── customer/    # Customer-related DTOs
│   │   │   │       ├── order/       # Order-related DTOs
│   │   │   │       ├── driver/      # Driver-related DTOs
│   │   │   │       ├── truck/       # Truck-related DTOs
│   │   │   │       └── tracking/    # Tracking-related DTOs
│   │   │   └── pom.xml              # DTO dependencies
│   │   └── common-exceptions/       # Exception handling
│   │       ├── src/main/java/
│   │       │   └── com/saxion/proj/common/exception/
│   │       │       ├── business/    # Business exceptions
│   │       │       ├── technical/   # Technical exceptions
│   │       │       └── handler/     # Global exception handlers
│   │       └── pom.xml
│   ├── config/                      # Configuration modules
│   │   ├── config-server/           # Spring Cloud Config Server
│   │   │   ├── src/main/java/
│   │   │   ├── configs/             # Configuration files
│   │   │   │   ├── development/     # Dev environment configs
│   │   │   │   ├── staging/         # Staging environment configs
│   │   │   │   └── production/      # Production environment configs
│   │   │   └── application.yml
│   │   └── security-config/         # Security configuration
│   │       ├── src/main/java/
│   │       │   └── com/saxion/proj/security/
│   │       │       ├── jwt/         # JWT configuration
│   │       │       ├── oauth/       # OAuth2 configuration
│   │       │       └── cors/        # CORS configuration
│   │       └── pom.xml
│   └── utils/                       # Utility libraries
│       └── common-utilities/        # Common utility functions
│           ├── src/main/java/
│           │   └── com/saxion/proj/common/utils/
│           │       ├── date/        # Date/time utilities
│           │       ├── string/      # String manipulation
│           │       ├── validation/  # Validation helpers
│           │       ├── json/        # JSON processing
│           │       ├── file/        # File operations
│           │       ├── crypto/      # Encryption utilities
│           │       ├── http/        # HTTP utilities
│           │       └── constants/   # Application constants
│           ├── src/test/java/       # Utility tests
│           └── pom.xml
│
├── deployment/                      # Deployment & DevOps
│   ├── docker/                      # Docker configurations
│   │   ├── docker-compose.yml       # Local development stack
│   │   ├── docker-compose.prod.yml  # Production stack
│   │   ├── docker-compose.test.yml  # Testing environment
│   │   └── dockerfiles/             # Custom Dockerfiles
│   │       ├── analytics.Dockerfile
│   │       └── gateway.Dockerfile
│   ├── k8s/                         # Kubernetes manifests
│   │   ├── namespaces/              # Kubernetes namespaces
│   │   ├── services/                # Service definitions
│   │   │   ├── analytics-service.yaml
│   │   │   ├── driver-service.yaml
│   │   │   └── gateway-service.yaml
│   │   ├── deployments/             # Deployment configurations
│   │   ├── configmaps/              # Configuration maps
│   │   ├── secrets/                 # Secret management
│   │   └── ingress/                 # Ingress controllers
│   └── scripts/                     # Deployment scripts
│       ├── deploy-analytics.sh      # Analytics deployment
│       ├── deploy-all.sh            # Full system deployment
│       ├── rollback.sh              # Rollback script
│       └── health-check.sh          # Health verification
│
├── tests/                           # Testing infrastructure
│   ├── e2e/                         # End-to-end tests
│   │   ├── scenarios/               # Test scenarios
│   │   ├── fixtures/                # Test data
│   │   └── reports/                 # Test reports
│   └── integration/                 # Integration tests
│       ├── service-to-service/      # Inter-service tests
│       ├── database/                # Database integration
│       └── external-apis/           # External API tests
│
├── docs/                            # Documentation
│   ├── PROJECT_STRUCTURE.md         # This file
│   ├── API_DOCUMENTATION.md         # API specifications
│   ├── DEPLOYMENT_GUIDE.md          # Deployment instructions
│   ├── DEVELOPMENT_SETUP.md         # Development environment
│   ├── ARCHITECTURE.md              # System architecture
│   └── TROUBLESHOOTING.md           # Common issues & solutions
│
├── src/                             # Parent project source
│   ├── main/                        # Parent project main source
│   └── test/                        # Parent project tests
│
├── .gitlab-ci.yml                   # CI/CD pipeline configuration
├── pom.xml                          # Parent Maven configuration
├── mvnw & mvnw.cmd                  # Maven wrapper scripts
├── .gitignore                       # Git ignore rules
├── LICENSE                          # Project license
└── README.md                        # Project overview & quick start
```

## 🎯 Service Categories Detailed

### 🏢 Core Services (Business Domain)
These services represent the core business entities and their lifecycle management:

#### **Customer Service**
- **Purpose**: Complete customer lifecycle management
- **Responsibilities**: 
  - Customer registration and profile management
  - Customer preferences and settings
  - Customer history and analytics
  - Customer communication preferences
- **Database**: Customer profiles, preferences, communication logs
- **Key APIs**: `/api/customers`, `/api/customers/{id}/profile`

#### **Order Service**
- **Purpose**: End-to-end order processing and management
- **Responsibilities**:
  - Order creation and validation
  - Order lifecycle management (pending → assigned → in-transit → delivered)
  - Order modification and cancellation
  - Order history and tracking
- **Database**: Orders, order items, order status history
- **Key APIs**: `/api/orders`, `/api/orders/{id}/status`

#### **Driver Service**
- **Purpose**: Driver management and availability tracking
- **Responsibilities**:
  - Driver registration and profile management
  - Driver availability and schedule management
  - Driver performance tracking
  - Driver document and license management
- **Database**: Driver profiles, availability, performance metrics
- **Key APIs**: `/api/drivers`, `/api/drivers/{id}/availability`

#### **Truck Service**
- **Purpose**: Fleet management and vehicle operations
- **Responsibilities**:
  - Vehicle registration and specifications
  - Maintenance scheduling and tracking
  - Vehicle availability and assignment
  - Fuel and operational cost tracking
- **Database**: Vehicle inventory, maintenance records, operational costs
- **Key APIs**: `/api/trucks`, `/api/trucks/{id}/maintenance`

#### **Tracking Service**
- **Purpose**: Real-time location and route management
- **Responsibilities**:
  - GPS location tracking
  - Route optimization and planning
  - Real-time location updates
  - Geofencing and alerts
- **Database**: Location history, routes, geofences
- **Key APIs**: `/api/tracking/location`, `/api/tracking/routes`

### 💼 Business Services (Support Functions)

#### **Billing Service**
- **Purpose**: Financial operations and invoice management
- **Responsibilities**:
  - Invoice generation and management
  - Billing cycles and recurring charges
  - Financial reporting and analytics
  - Tax calculations and compliance
- **Database**: Invoices, billing history, tax configurations
- **Key APIs**: `/api/billing/invoices`, `/api/billing/reports`

#### **Payment Service**
- **Purpose**: Payment processing and transaction management
- **Responsibilities**:
  - Payment gateway integration
  - Transaction processing and validation
  - Payment method management
  - Refund and chargeback handling
- **Database**: Transactions, payment methods, refunds
- **Key APIs**: `/api/payments/process`, `/api/payments/methods`

#### **Assignment Service**
- **Purpose**: Intelligent job assignment and optimization
- **Responsibilities**:
  - Driver-truck-order assignment algorithms
  - Route optimization for assignments
  - Load balancing and capacity planning
  - Assignment conflict resolution
- **Database**: Assignment rules, optimization parameters
- **Key APIs**: `/api/assignments/assign`, `/api/assignments/optimize`

#### **Document Management Service**
- **Purpose**: Document storage and compliance management
- **Responsibilities**:
  - Document upload and storage
  - Document versioning and audit trails
  - Compliance document tracking
  - Document sharing and permissions
- **Database**: Document metadata, versions, permissions
- **Key APIs**: `/api/documents/upload`, `/api/documents/{id}/versions`

### 📱 Communication Services

#### **Notification Service**
- **Purpose**: Multi-channel communication delivery
- **Responsibilities**:
  - Push notifications (mobile, web)
  - Email delivery and templating
  - SMS messaging
  - Notification preferences and scheduling
- **Integrations**: Firebase, SendGrid, Twilio
- **Key APIs**: `/api/notifications/send`, `/api/notifications/templates`

#### **Chat Service**
- **Purpose**: Real-time messaging platform
- **Responsibilities**:
  - Real-time chat between users
  - Message history and search
  - File sharing in conversations
  - Chat rooms and group messaging
- **Technology**: WebSocket, Socket.IO
- **Key APIs**: `/api/chat/messages`, `/api/chat/rooms`

#### **Call Service**
- **Purpose**: Voice communication integration
- **Responsibilities**:
  - Voice call initiation and routing
  - Call recording and storage
  - Conference calling
  - Emergency call handling
- **Integrations**: Twilio Voice, WebRTC
- **Key APIs**: `/api/calls/initiate`, `/api/calls/conference`

### 🏗️ Infrastructure Services

#### **Auth Service**
- **Purpose**: Security and access management
- **Responsibilities**:
  - User authentication (login/logout)
  - JWT token management
  - Role-based access control (RBAC)
  - OAuth2 integration
  - Password policies and management
- **Security**: JWT, OAuth2, Spring Security
- **Key APIs**: `/api/auth/login`, `/api/auth/refresh`

#### **Gateway Service**
- **Purpose**: API Gateway and request routing
- **Responsibilities**:
  - Request routing to appropriate services
  - Rate limiting and throttling
  - Request/response transformation
  - CORS handling
  - API versioning
- **Technology**: Spring Cloud Gateway
- **Key Features**: Load balancing, circuit breaker

#### **Discovery Service**
- **Purpose**: Service registry and discovery
- **Responsibilities**:
  - Service registration and health checks
  - Service discovery for inter-service communication
  - Load balancing configuration
  - Service metadata management
- **Technology**: Eureka Server
- **Dashboard**: Service registry UI

### 🔧 Platform Services (Microservices)

#### **Analytics Service**
- **Purpose**: Business intelligence and reporting
- **Responsibilities**:
  - Data aggregation from all services
  - Business metrics calculation
  - Dashboard and report generation
  - Predictive analytics
  - KPI monitoring
- **Technology**: Spring Boot, scheduled jobs
- **Databases**: Time-series data, aggregated metrics
- **Key APIs**: `/api/analytics/reports`, `/api/analytics/dashboards`

#### **Log Monitoring Service**
- **Purpose**: Centralized logging and monitoring
- **Responsibilities**:
  - Log aggregation from all services
  - Log parsing and indexing
  - Alert generation based on log patterns
  - Log retention and archival
- **Technology**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Key Features**: Real-time log streaming, alerting

#### **Maintenance Service**
- **Purpose**: System maintenance and health management
- **Responsibilities**:
  - Scheduled maintenance tasks
  - System health monitoring
  - Database cleanup and optimization
  - Backup verification
  - Performance monitoring
- **Technology**: Spring Boot with scheduled tasks
- **Key Features**: Cron-based scheduling, health endpoints

## 📚 Shared Libraries (JAR Dependencies)

### **Common DTOs**
- **Purpose**: Standardized data transfer objects
- **Contains**: Customer, Order, Driver, Truck, Tracking DTOs
- **Usage**: Imported by services for consistent data structures

### **Common Exceptions**
- **Purpose**: Standardized exception handling
- **Contains**: Business exceptions, technical exceptions, global handlers
- **Usage**: Consistent error handling across all services

### **Common Utilities**
- **Purpose**: Reusable utility functions
- **Contains**: Date utilities, validation helpers, JSON processing, encryption
- **Usage**: Common functionality to avoid code duplication

### **Configuration Modules**
- **Purpose**: Shared configuration and security settings
- **Contains**: JWT config, OAuth2 setup, CORS configuration
- **Usage**: Consistent security and configuration across services

## 🚀 Development Guidelines

### **Service Development Standards**
1. **Each service must have**:
   - `@SpringBootApplication` main class
   - `application.yml` with service-specific configuration
   - Dockerfile for containerization
   - Unit and integration tests
   - API documentation

2. **Package Structure** (within each service):
   ```
   com.saxion.proj.{service-name}/
   ├── controller/     # REST controllers
   ├── service/        # Business logic
   ├── repository/     # Data access layer
   ├── model/          # Domain entities
   ├── dto/            # Service-specific DTOs
   ├── config/         # Service configuration
   └── exception/      # Service-specific exceptions
   ```

3. **Database per Service**:
   - Each microservice should have its own database
   - No direct database access between services
   - Use APIs for inter-service communication

### **Naming Conventions**
- **Services**: `{domain}-service` (e.g., `customer-service`)
- **Packages**: `com.saxion.proj.{domain}` (e.g., `com.saxion.proj.customer`)
- **APIs**: `/api/{domain}/{resource}` (e.g., `/api/customers/profile`)
- **Docker Images**: `truck-mgmt/{service-name}:version`

### **Inter-Service Communication**
1. **Synchronous**: REST APIs for immediate responses
2. **Asynchronous**: Message queues for decoupled communication
3. **Service Discovery**: Use Eureka for service location
4. **Circuit Breaker**: Implement resilience patterns

### **Configuration Management**
- Use Spring Cloud Config for centralized configuration
- Environment-specific configs in `deployment/config/`
- Sensitive data in Kubernetes secrets or environment variables

### **Testing Strategy**
1. **Unit Tests**: Test individual components
2. **Integration Tests**: Test service integration
3. **Contract Tests**: Test API contracts between services
4. **End-to-End Tests**: Test complete user journeys

### **Deployment Guidelines**
1. **Containerization**: Each service must be containerized
2. **Health Checks**: Implement health endpoints
3. **Graceful Shutdown**: Handle SIGTERM properly
4. **Resource Limits**: Define CPU and memory limits
5. **Rolling Updates**: Support zero-downtime deployments

## 🔄 CI/CD Pipeline

The `.gitlab-ci.yml` implements a multi-stage pipeline:

1. **Validate**: Project structure and dependencies
2. **Build**: Compile and package services
3. **Test**: Run unit and integration tests
4. **Package**: Create Docker images
5. **Deploy**: Deploy to target environments

## 📊 Benefits of This Architecture

### **🎯 Business Benefits**
- **Fast Time-to-Market**: Independent service development
- **Scalability**: Scale services based on demand
- **Reliability**: Fault isolation between services
- **Technology Flexibility**: Choose best tech for each service

### **👥 Team Benefits**
- **Clear Ownership**: Teams own specific service categories
- **Parallel Development**: Teams work independently
- **Specialized Skills**: Teams can specialize in domains
- **Reduced Conflicts**: Minimal merge conflicts

### **🔧 Technical Benefits**
- **Maintainability**: Clear separation of concerns
- **Testability**: Services can be tested in isolation
- **Deployability**: Independent deployment cycles
- **Monitoring**: Service-level monitoring and alerting

## 🚨 Important Notes

1. **Shared Libraries**: Only `shared/` contains libraries; everything else are microservices
2. **Platform Services**: All services in `platform/` are executable microservices
3. **Database Strategy**: Each service manages its own data
4. **API Versioning**: Use versioned APIs for backward compatibility
5. **Security**: All inter-service communication should be secured

This structure provides a solid foundation for building, deploying, and maintaining a robust truck fleet management system with clear boundaries, shared utilities, and scalable architecture.
