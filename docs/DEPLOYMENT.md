# TFMS Deployment Guide

**Truck Fleet Management System - Complete Deployment & Testing Guide**

## Quick Start

### Prerequisites
- Java 17+ installed
- Node.js 16+ installed (optional - will be auto-downloaded in production)
- Maven (optional - uses included wrapper)

### One-Command Start

**Development Mode (Separate Frontend & Backend):**
```bash
./scripts/start-dev.sh
```

**Production Mode (Single Integrated App):**
```bash
./scripts/start-prod.sh
```

---

## Architecture Overview

### Development Mode
```
┌─────────────────┐    ┌──────────────────┐
│   Frontend      │    │    Backend       │
│ localhost:3000  │◄──►│ localhost:8443   │
│ (React Dev)     │    │ (Spring Boot)    │
└─────────────────┘    └──────────────────┘
        │                       │
        ▼                       ▼
   React HMR            HTTPS + H2 Console
   Fast Reload         CORS Enabled
```

### Production Mode
```
┌─────────────────────────────────────┐
│        Single Application           │
│         localhost:8443              │
│  ┌─────────────┐ ┌─────────────────┐│
│  │  Frontend   │ │    Backend      ││
│  │ (React SPA) │ │ (Spring Boot)   ││
│  │ /static/*   │ │    /api/*       ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
```

---

## Development Mode

### How to Start
```bash
# Method 1: Use convenience script
./scripts/start-dev.sh

# Method 2: Manual startup
cd tfms-starter
../mvnw spring-boot:run -Dspring.profiles.active=dev

# In another terminal
cd frontend
npm start
```

### What Happens
- **Backend** starts on port 8080 with development profile
- **Frontend** starts on port 3000 with hot reloading
- **CORS** enabled for cross-origin requests
- **H2 Console** available for database inspection
- **Fast startup** - no frontend build overhead

### Access URLs
- **Backend API**: https://localhost:8443/api
- **Frontend**: http://localhost:3000  
- **H2 Console**: https://localhost:8443/h2-console
- **Health Check**: https://localhost:8443/actuator/health
- **Swagger UI**: https://localhost:8443/swagger-ui/index.html

### Development Features
- **Hot Reload**: Frontend changes reload instantly
- **Debug Mode**: Detailed logging enabled
- **H2 Database**: In-memory database with web console
- **CORS**: Cross-origin requests allowed from localhost:3000
- **HTTPS**: Self-signed certificate for secure development
- **SSL Certificate**: Located at `classpath:keystore.p12` (password: changeit)

---

## Production Mode

### How to Start
```bash
# Method 1: Use convenience script (recommended)
./scripts/start-prod.sh

# Method 2: Manual build and run
cd tfms-starter
../mvnw clean package -Pprod
java -jar target/tfms-1.0.0-dev.jar --spring.profiles.active=prod
```

### What Happens
1. **Downloads** Node.js and npm automatically
2. **Installs** frontend dependencies (`npm install`)
3. **Builds** React production bundle (`npm run build`)
4. **Copies** React files to Spring Boot `/static` folder
5. **Creates** single executable JAR file
6. **Starts** integrated application

### Access URLs
- **Application**: https://localhost:8443 (Frontend + Backend)
- **API Endpoints**: https://localhost:8443/api
- **Health Check**: https://localhost:8443/actuator/health
- **Actuator**: https://localhost:8443/actuator
- **Swagger UI**: https://localhost:8443/swagger-ui/index.html

### Production Features
- **Single JAR**: Easy deployment artifact
- **Optimized**: Minified React build
- **No CORS**: Same origin, no cross-origin issues
- **Production Logging**: Structured logs to file
- **Security**: H2 console disabled
- **HTTPS**: SSL/TLS encryption enabled
- **SSL Certificate**: Configurable via environment variables

---

## Testing & Validation

### Quick Health Check
```bash
./scripts/test-services.sh
```

**This script automatically:**
- Detects running mode (dev/prod)
- Tests all service endpoints
- Shows service status
- Lists available URLs

### Manual Testing

**Test Backend API:**
```bash
curl -k https://localhost:8443/actuator/health
# Expected: {"status":"UP","components":{...}}

curl -k https://localhost:8443/api/test/health
# Expected: {"service":"tfms-starter","status":"UP",...}
```

**Test Frontend (Dev Mode):**
```bash
curl http://localhost:3000
# Expected: HTML with React app
```

**Test Frontend (Prod Mode):**
```bash
curl -k https://localhost:8443
# Expected: HTML with React app served from /static
```

### API Endpoints to Test
```bash
# Health and Monitoring
curl -k https://localhost:8443/actuator/health
curl -k https://localhost:8443/actuator/info
curl -k https://localhost:8443/health

# Available API Services
curl -k https://localhost:8443/api/test/health
curl -k https://localhost:8443/api/simple/test

# Authentication (when implemented)
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

---

## Stopping Services

### Stop All Services
```bash
./scripts/stop-all.sh
```

### Manual Stop
```bash
# Stop development services
pkill -f "spring-boot:run"
pkill -f "react-scripts"

# Stop production service
pkill -f "tfms-1.0.0-dev.jar"
```

---

## Project Structure

```
TFMS/
├── scripts/                    # Deployment scripts
│   ├── start-dev.sh           # Development mode starter
│   ├── start-prod.sh          # Production mode starter  
│   ├── test-services.sh       # Health check script
│   └── stop-all.sh            # Stop all services
├── tfms-starter/              # Main Spring Boot app
│   ├── src/main/resources/
│   │   ├── application.properties        # Base config
│   │   ├── application-dev.properties    # Dev config
│   │   └── application-prod.properties   # Prod config
│   └── target/
│       └── tfms-1.0.0-dev.jar          # Production JAR
├── frontend/                  # React application
│   ├── src/
│   ├── build/                 # Production build output
│   └── package.json
└── modules/                   # Business modules
    ├── core-modules/
    ├── business-modules/
    └── shared-modules/
```

---

## SSL/HTTPS Configuration

### Development SSL Certificate
- **Location**: `src/main/resources/keystore.p12`
- **Password**: `changeit`
- **Alias**: `tfms-dev`
- **Type**: PKCS12

### Production SSL Certificate
- **Location**: Configurable via `SSL_KEYSTORE_PATH` environment variable
- **Password**: Configurable via `SSL_KEYSTORE_PASSWORD` environment variable
- **Default**: `/app/ssl/keystore.p12`
- **Alias**: `tfms-prod`

### Browser Security Warning
When accessing HTTPS URLs in development, you'll see a security warning about the self-signed certificate. This is normal - click "Advanced" and "Proceed to localhost" to continue.

### Using curl with Self-Signed Certificates
Always use the `-k` flag with curl when testing HTTPS endpoints:
```bash
curl -k https://localhost:8443/actuator/health
```

---

## Configuration Profiles

### application.properties (Base)
```properties
spring.application.name=TFMS - Truck Fleet Management System
server.port=8080
production.isEnable=false
```

### application-dev.properties
```properties
production.isEnable=false
spring.h2.console.enabled=true
logging.level.com.saxion.proj.tfms=DEBUG
```

### application-prod.properties  
```properties
production.isEnable=true
spring.h2.console.enabled=false
logging.level.root=WARN
logging.file.name=/app/logs/tfms.log
```

---

## Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8443
lsof -i :3000

# Kill specific processes
pkill -f "spring-boot"
pkill -f "react-scripts"
```

### Frontend Build Issues
```bash
# Clear npm cache
cd frontend
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Production Build Fails
```bash
# Clean build
cd tfms-starter
../mvnw clean
rm -rf target/
../mvnw clean package -Pprod
```

### Database Issues
```bash
# Access H2 console (dev mode only)
# URL: https://localhost:8443/h2-console
# JDBC URL: jdbc:h2:mem:tfmsdb
# Username: sa
# Password: password
# Note: Accept the security warning for self-signed certificate
```

---

## Common Use Cases

### 1. **Developer Workflow**
```bash
# Start development environment
./scripts/start-dev.sh

# Make changes to code
# Frontend auto-reloads on port 3000
# Backend needs restart for Java changes

# Test changes
./scripts/test-services.sh

# Stop when done
./scripts/stop-all.sh
```

### 2. **Production Deployment**
```bash
# Build and deploy
./scripts/start-prod.sh

# The script will:
# - Build React app
# - Package everything in JAR
# - Start production server
# - Serve everything from port 8080
```

### 3. **Demo/Testing**
```bash
# Quick production demo
./scripts/start-prod.sh

# Wait for startup, then open browser to:
# http://localhost:8080
```

---

## Performance Notes

### Development Mode
- **Fast startup**: ~10-15 seconds
- **Hot reload**: Instant frontend changes
- **Memory usage**: ~200-300MB
- **Debug info**: Full logging enabled

### Production Mode  
- **Build time**: ~30-60 seconds (first time)
- **Startup**: ~15-20 seconds
- **Memory usage**: ~150-250MB
- **Performance**: Optimized React bundle

---

## Logs Location

### Development Mode
- **Backend**: `tfms-starter/backend.log`
- **Frontend**: `frontend/frontend.log`

### Production Mode
- **Application**: `logs/tfms.log`
- **Startup**: `logs/startup.log`

---

## Success Indicators

**Development Mode Started Successfully:**
```
Backend started successfully!
Frontend started successfully!
Backend API: https://localhost:8443/api
Frontend:   http://localhost:3000
```

**Production Mode Started Successfully:**
```
Production build completed successfully!
Production application started successfully!
Backend API working
Frontend React app working
Application URL: https://localhost:8443
```

---

## Support

If you encounter issues:

1. **Run health check**: `./scripts/test-services.sh`
2. **Stop all services**: `./scripts/stop-all.sh`  
3. **Try again**: `./scripts/start-dev.sh` or `./scripts/start-prod.sh`
4. **Check logs** in the locations mentioned above
5. **Check this guide** for troubleshooting steps

---

*Happy coding!*
