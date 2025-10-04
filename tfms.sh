#!/bin/bash

# ===========================================
# TFMS Quick Launcher
# ===========================================

echo "🚀 TFMS - Truck Fleet Management System"
echo "======================================="
echo ""
echo "Choose deployment mode:"
echo ""
echo "1) 🔧 Development Mode"
echo "   - Frontend: http://localhost:3000 (React dev server)"
echo "   - Backend:  http://localhost:8080/api"
echo "   - Features: Hot reload, H2 console, CORS enabled"
echo ""
echo "2) 🏭 Production Mode"  
echo "   - Application: http://localhost:8080 (integrated)"
echo "   - Features: Single JAR, optimized build, production ready"
echo ""
echo "3) 🧪 Test Services"
echo "   - Run health checks on current services"
echo ""
echo "4) 🛑 Stop All Services"
echo "   - Stop all running TFMS services"
echo ""
echo "5) 📖 View Documentation"
echo "   - Open deployment guide"
echo ""

read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        echo "Starting Development Mode..."
        ./scripts/start-dev.sh
        ;;
    2)
        echo "Starting Production Mode..."
        ./scripts/start-prod.sh
        ;;
    3)
        echo "Running Service Tests..."
        ./scripts/test-services.sh
        ;;
    4)
        echo "Stopping All Services..."
        ./scripts/stop-all.sh
        ;;
    5)
        echo "Opening Documentation..."
        if command -v less &> /dev/null; then
            less scripts/DEPLOYMENT.md
        else
            cat scripts/DEPLOYMENT.md
        fi
        ;;
    *)
        echo "Invalid choice. Please run again and choose 1-5."
        exit 1
        ;;
esac
