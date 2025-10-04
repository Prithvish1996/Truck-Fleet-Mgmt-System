#!/bin/bash

# ===========================================
# TFMS Quick Test Script
# ===========================================
# This script performs quick health checks on running services
# ===========================================

set -e

echo "TFMS Quick Health Check"
echo "======================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to test a URL
test_url() {
    local url=$1
    local description=$2
    local expected=$3
    
    echo -n -e "${BLUE}Testing ${description}...${NC} "
    
    response=$(curl -s "$url" 2>/dev/null || echo "ERROR")
    
    if [[ $response == *"$expected"* ]]; then
        echo -e "${GREEN}PASS${NC}"
        return 0
    else
        echo -e "${RED}FAIL${NC}"
        echo -e "${YELLOW}   Expected: $expected${NC}"
        echo -e "${YELLOW}   Got: ${response:0:100}...${NC}"
        return 1
    fi
}

# Check what's running
echo -e "${BLUE}Checking running services...${NC}"

BACKEND_8080=$(lsof -i :8080 2>/dev/null | grep -v COMMAND | wc -l)
FRONTEND_3000=$(lsof -i :3000 2>/dev/null | grep -v COMMAND | wc -l)

echo -e "Port 8080 (Backend): $BACKEND_8080 process(es)"
echo -e "Port 3000 (Frontend): $FRONTEND_3000 process(es)"

# Determine mode
if [ $BACKEND_8080 -gt 0 ] && [ $FRONTEND_3000 -gt 0 ]; then
    echo -e "${GREEN}Detected: DEVELOPMENT MODE${NC}"
    MODE="dev"
elif [ $BACKEND_8080 -gt 0 ] && [ $FRONTEND_3000 -eq 0 ]; then
    echo -e "${GREEN}Detected: PRODUCTION MODE${NC}"
    MODE="prod"
else
    echo -e "${RED}No TFMS services detected${NC}"
    echo -e "${YELLOW}Run either:${NC}"
    echo -e "   ./scripts/start-dev.sh   (for development)"
    echo -e "   ./scripts/start-prod.sh  (for production)"
    exit 1
fi

echo ""

# Test based on detected mode
if [ "$MODE" = "dev" ]; then
    echo -e "${BLUE}Testing Development Mode Services${NC}"
    echo "================================="
    
    test_url "http://localhost:8080/api/test/health" "Backend Health" "UP"
    test_url "http://localhost:8080/h2-console" "H2 Console" "H2 Console"
    test_url "http://localhost:3000" "Frontend React App" "React App"
    
    echo ""
    echo -e "${GREEN}Development URLs:${NC}"
    echo -e "   Backend API: http://localhost:8080/api"
    echo -e "   Frontend:    http://localhost:3000"
    echo -e "   H2 Console:  http://localhost:8080/h2-console"

elif [ "$MODE" = "prod" ]; then
    echo -e "${BLUE}Testing Production Mode Services${NC}"
    echo "==============================="
    
    test_url "http://localhost:8080/api/test/health" "Backend Health" "UP"
    test_url "http://localhost:8080" "Frontend React App" "React App"
    test_url "http://localhost:8080/actuator/health" "Actuator Health" "UP"
    
    echo ""
    echo -e "${GREEN}Production URLs:${NC}"
    echo -e "   Application: http://localhost:8080"
    echo -e "   API:         http://localhost:8080/api"
    echo -e "   Health:      http://localhost:8080/actuator/health"
fi

# Additional API tests
echo ""
echo -e "${BLUE}Testing Core API Endpoints${NC}"
echo "=========================="

test_url "http://localhost:8080/api/customers/health" "Customer Service" "customer-service"
test_url "http://localhost:8080/api/drivers/health" "Driver Service" "driver-service"
test_url "http://localhost:8080/api/orders/health" "Order Service" "order-service"

echo ""
echo -e "${GREEN}Health check completed!${NC}"
