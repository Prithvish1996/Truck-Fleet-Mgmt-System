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
    
    # Use -k flag for HTTPS with self-signed certificates
    if [[ $url == https* ]]; then
        response=$(curl -k -s "$url" 2>/dev/null || echo "ERROR")
    else
        response=$(curl -s "$url" 2>/dev/null || echo "ERROR")
    fi
    
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

BACKEND_8443=$(lsof -i :8443 2>/dev/null | grep -v COMMAND | wc -l)
FRONTEND_3000=$(lsof -i :3000 2>/dev/null | grep -v COMMAND | wc -l)

echo -e "Port 8443 (Backend HTTPS): $BACKEND_8443 process(es)"
echo -e "Port 3000 (Frontend): $FRONTEND_3000 process(es)"

# Determine mode
if [ $BACKEND_8443 -gt 0 ] && [ $FRONTEND_3000 -gt 0 ]; then
    echo -e "${GREEN}Detected: DEVELOPMENT MODE${NC}"
    MODE="dev"
elif [ $BACKEND_8443 -gt 0 ] && [ $FRONTEND_3000 -eq 0 ]; then
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
    
    test_url "https://localhost:8443/actuator/health" "Backend Health" "UP"
    test_url "http://localhost:3000" "Frontend React App" "React App"
    
    echo ""
    echo -e "${GREEN}Development URLs:${NC}"
    echo -e "   Backend API: https://localhost:8443/api"
    echo -e "   Frontend:    http://localhost:3000"
    echo -e "   Database:    PostgreSQL localhost:5432/tfmsdb"

elif [ "$MODE" = "prod" ]; then
    echo -e "${BLUE}Testing Production Mode Services${NC}"
    echo "==============================="
    
    test_url "https://localhost:8443/actuator/health" "Backend Health" "UP"
    test_url "https://localhost:8443" "Frontend React App" "React App"
    test_url "https://localhost:8443/actuator/info" "Actuator Info" "tfms"
    
    echo ""
    echo -e "${GREEN}Production URLs:${NC}"
    echo -e "   Application: https://localhost:8443"
    echo -e "   API:         https://localhost:8443/api"
    echo -e "   Health:      https://localhost:8443/actuator/health"
fi

# Additional API tests
echo ""
echo -e "${BLUE}Testing Available API Endpoints${NC}"
echo "=============================="

test_url "https://localhost:8443/api/test/health" "Test API Health" "tfms-starter"
test_url "https://localhost:8443/api/simple/test" "Simple Test API" "Simple test endpoint"
test_url "https://localhost:8443/health" "Custom Health Check" "TFMS"

echo ""
echo -e "${GREEN}Health check completed!${NC}"
