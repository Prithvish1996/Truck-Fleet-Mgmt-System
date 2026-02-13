#!/bin/bash

# ===========================================
# TFMS Stop All Services Script
# ===========================================
# This script stops all TFMS services safely
# ===========================================

echo "Stopping All TFMS Services..."
echo "============================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to stop processes
stop_process() {
    local pattern=$1
    local description=$2
    
    local pids=$(pgrep -f "$pattern" 2>/dev/null || true)
    
    if [ -n "$pids" ]; then
        echo -e "${BLUE}Stopping ${description}...${NC}"
        pkill -f "$pattern" 2>/dev/null || true
        sleep 2
        
        # Check if still running
        local remaining=$(pgrep -f "$pattern" 2>/dev/null || true)
        if [ -n "$remaining" ]; then
            echo -e "${YELLOW}Force killing ${description}...${NC}"
            pkill -9 -f "$pattern" 2>/dev/null || true
        fi
        echo -e "${GREEN}${description} stopped${NC}"
    else
        echo -e "${YELLOW}No ${description} processes found${NC}"
    fi
}

# Stop all TFMS services
stop_process "spring-boot:run" "Spring Boot Development Server"
stop_process "tfms-1.0.0-dev.jar" "Production JAR Application"
stop_process "react-scripts" "React Development Server"
stop_process "npm.*start" "NPM Start Process"

# Check final status
echo ""
echo -e "${BLUE}Final Status Check:${NC}"

BACKEND_8443=$(lsof -i :8443 2>/dev/null | grep -v COMMAND | wc -l)
FRONTEND_3000=$(lsof -i :3000 2>/dev/null | grep -v COMMAND | wc -l)

if [ $BACKEND_8443 -eq 0 ] && [ $FRONTEND_3000 -eq 0 ]; then
    echo -e "${GREEN}All TFMS services stopped successfully${NC}"
    echo -e "Port 8443: Available"
    echo -e "Port 3000: Available"
else
    echo -e "${YELLOW}Some services may still be running:${NC}"
    if [ $BACKEND_8443 -gt 0 ]; then
        echo -e "Port 8443: $BACKEND_8443 process(es) still running"
        lsof -i :8443 2>/dev/null | grep -v COMMAND || true
    fi
    if [ $FRONTEND_3000 -gt 0 ]; then
        echo -e "Port 3000: $FRONTEND_3000 process(es) still running"
        lsof -i :3000 2>/dev/null | grep -v COMMAND || true
    fi
fi

echo ""
echo -e "${GREEN}Cleanup completed!${NC}"
