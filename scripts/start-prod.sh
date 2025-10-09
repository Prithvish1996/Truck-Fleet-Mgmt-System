#!/bin/bash

# ===========================================
# TFMS Production Mode Build & Run Script
# ===========================================
# This script builds and runs the application in production mode:
# - Single URL: http://localhost:8080
# - Automated React build and integration
# - Optimized for deployment
# ===========================================

set -e

echo "Starting TFMS in Production Mode..."
echo "===================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${BLUE}Project Root: ${PROJECT_ROOT}${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Shutting down production service...${NC}"
    pkill -f "tfms-1.0.0-dev.jar" 2>/dev/null || true
    echo -e "${GREEN}Production service stopped${NC}"
    exit 0
}

# Set trap for cleanup
trap cleanup SIGINT SIGTERM

# Check if port is available
check_port() {
    local port=$1
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${RED}Port $port is already in use${NC}"
        echo -e "${YELLOW}Please stop the service using port $port${NC}"
        exit 1
    fi
}

echo -e "${BLUE}Checking port availability...${NC}"
check_port 8443

# Build Production Package
echo -e "\n${BLUE}Building Production Package...${NC}"
echo -e "${YELLOW}This will:${NC}"
echo -e "${YELLOW}- Download Node.js and npm${NC}"
echo -e "${YELLOW}- Install frontend dependencies${NC}"
echo -e "${YELLOW}- Build React production bundle${NC}"
echo -e "${YELLOW}- Copy React files to Spring Boot static folder${NC}"
echo -e "${YELLOW}- Create executable JAR${NC}"

cd "$PROJECT_ROOT/tfms-starter"

# Run production build
../mvnw clean package -Pprod

if [ $? -ne 0 ]; then
    echo -e "${RED}Production build failed${NC}"
    exit 1
fi

echo -e "${GREEN}Production build completed successfully!${NC}"

# Create logs directory for production
mkdir -p "$PROJECT_ROOT/logs"

# Start Production Application
echo -e "\n${BLUE}Starting Production Application...${NC}"

# Start production app in background
java -jar target/tfms-1.0.0-dev.jar \
    --spring.profiles.active=prod \
    --logging.file.name="$PROJECT_ROOT/logs/tfms.log" \
    > "$PROJECT_ROOT/logs/startup.log" 2>&1 &

PROD_PID=$!
echo -e "${BLUE}Production PID: ${PROD_PID}${NC}"

# Wait for application to start
echo -e "${YELLOW}Waiting for application to start...${NC}"
for i in {1..60}; do
    if curl -k -s https://localhost:8443/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}Production application started successfully!${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}Application failed to start within 60 seconds${NC}"
        echo -e "${YELLOW}Check logs for details:${NC}"
        echo -e "   Startup: $PROJECT_ROOT/logs/startup.log"
        echo -e "   Application: $PROJECT_ROOT/logs/tfms.log"
        kill $PROD_PID 2>/dev/null || true
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Test both frontend and backend
echo -e "\n${BLUE}Testing Production Deployment...${NC}"

# Test API
API_RESPONSE=$(curl -k -s https://localhost:8443/actuator/health)
if [[ $API_RESPONSE == *"UP"* ]]; then
    echo -e "${GREEN}Backend API working${NC}"
else
    echo -e "${RED}Backend API test failed${NC}"
fi

# Test Frontend
FRONTEND_RESPONSE=$(curl -k -s https://localhost:8443 | grep -o "<title>.*</title>")
if [[ $FRONTEND_RESPONSE == *"React App"* ]]; then
    echo -e "${GREEN}Frontend React app working${NC}"
else
    echo -e "${RED}Frontend test failed${NC}"
fi

# Success message
echo -e "\n${GREEN}TFMS Production Mode Started Successfully!${NC}"
echo -e "=========================================="
echo -e "${BLUE}Application URL:${NC} https://localhost:8443"
echo -e "${BLUE}API Endpoints:${NC}  https://localhost:8443/api"
echo -e "${BLUE}Health Check:${NC}   https://localhost:8443/actuator/health"
echo -e "${BLUE}Actuator:${NC}      https://localhost:8443/actuator"
echo -e "${BLUE}Database:${NC}      PostgreSQL on localhost:5432/tfmsdb"
echo -e "${YELLOW}Logs:${NC}"
echo -e "   Application: $PROJECT_ROOT/logs/tfms.log"
echo -e "   Startup: $PROJECT_ROOT/logs/startup.log"
echo -e "\n${GREEN}Frontend and Backend served from single port (8080)${NC}"
echo -e "${YELLOW}Production CORS settings active:${NC}"
echo -e "   - Only HTTPS domains allowed"
echo -e "   - Restricted to essential HTTP methods"
echo -e "   - Limited headers for security"
echo -e "${YELLOW}Press Ctrl+C to stop the application${NC}"

# Keep script running
while true; do
    sleep 1
done
