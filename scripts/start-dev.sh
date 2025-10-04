#!/bin/bash

# ===========================================
# TFMS Development Mode Startup Script
# ===========================================
# This script starts the application in development mode:
# - Backend: http://localhost:8080/api
# - Frontend: http://localhost:3000 (separate React dev server)
# - CORS enabled for cross-origin requests
# ===========================================

set -e

echo "🚀 Starting TFMS in Development Mode..."
echo "==========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${BLUE}📁 Project Root: ${PROJECT_ROOT}${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}🛑 Shutting down services...${NC}"
    
    # Kill Spring Boot
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    # Kill React dev server
    pkill -f "react-scripts" 2>/dev/null || true
    pkill -f "npm.*start" 2>/dev/null || true
    
    echo -e "${GREEN}✅ Services stopped${NC}"
    exit 0
}

# Set trap for cleanup
trap cleanup SIGINT SIGTERM

# Check if ports are available
check_port() {
    local port=$1
    local service=$2
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${RED}❌ Port $port is already in use (required for $service)${NC}"
        echo -e "${YELLOW}💡 Please stop the service using port $port or run: pkill -f '$service'${NC}"
        exit 1
    fi
}

echo -e "${BLUE}🔍 Checking port availability...${NC}"
check_port 8080 "Backend"
check_port 3000 "Frontend"

# Start Backend in Development Mode
echo -e "\n${BLUE}🔧 Starting Backend (Development Mode)...${NC}"
cd "$PROJECT_ROOT/tfms-starter"

# Start backend in background
../mvnw spring-boot:run -Dspring-boot.run.profiles=dev > backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
echo -e "${YELLOW}⏳ Waiting for backend to start...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/api/test/health >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Backend started successfully!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Backend failed to start within 30 seconds${NC}"
        echo -e "${YELLOW}📝 Check backend.log for details${NC}"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# Start Frontend
echo -e "\n${BLUE}📱 Starting Frontend (React Dev Server)...${NC}"
cd "$PROJECT_ROOT/frontend"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📦 Installing frontend dependencies...${NC}"
    npm install
fi

# Start frontend in background
BROWSER=none npm start > frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for frontend to start
echo -e "${YELLOW}⏳ Waiting for frontend to start...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:3000 >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend started successfully!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Frontend failed to start within 30 seconds${NC}"
        echo -e "${YELLOW}📝 Check frontend.log for details${NC}"
        kill $FRONTEND_PID 2>/dev/null || true
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# Success message
echo -e "\n${GREEN}🎉 TFMS Development Mode Started Successfully!${NC}"
echo -e "==========================================="
echo -e "${BLUE}🔗 Backend API:${NC} http://localhost:8080/api"
echo -e "${BLUE}📱 Frontend:${NC}   http://localhost:3000"
echo -e "${BLUE}🗄️  H2 Console:${NC} http://localhost:8080/h2-console"
echo -e "${YELLOW}📝 Logs:${NC}"
echo -e "   Backend: $PROJECT_ROOT/tfms-starter/backend.log"
echo -e "   Frontend: $PROJECT_ROOT/frontend/frontend.log"
echo -e "\n${YELLOW}Press Ctrl+C to stop all services${NC}"

# Keep script running
while true; do
    sleep 1
done
