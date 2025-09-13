#!/bin/bash

# GitLab CI Deploy Script for Analytics Service
set -e

SERVICE_NAME=${SERVICE_NAME:-"analytics-service"}
SERVICE_PORT=${SERVICE_PORT:-"8090"}

echo "=== Starting deployment of $SERVICE_NAME ==="

# Stop existing container
echo "Stopping existing containers..."
docker-compose down $SERVICE_NAME || true

# Build and start
echo "Building and starting $SERVICE_NAME..."
docker-compose build $SERVICE_NAME
docker-compose up -d $SERVICE_NAME

# Wait for startup
echo "Waiting for service to start..."
sleep 30

# Check status
echo "=== Container Status ==="
docker-compose ps $SERVICE_NAME

# Show logs
echo "=== Recent Logs ==="
docker-compose logs --tail=20 $SERVICE_NAME

# Health check
echo "=== Health Check ==="
CONTAINER_ID=$(docker-compose ps -q $SERVICE_NAME)

if [ -z "$CONTAINER_ID" ]; then
    echo "❌ Container not found"
    exit 1
fi

echo "Container ID: $CONTAINER_ID"
echo "Container Status: $(docker inspect --format='{{.State.Status}}' $CONTAINER_ID)"

# Wait for health check to pass
for i in $(seq 1 10); do
    if docker exec $CONTAINER_ID curl -f http://localhost:$SERVICE_PORT/actuator/health >/dev/null 2>&1; then
        echo "✅ Health check PASSED (attempt $i)"
        break
    else
        echo "⏳ Health check attempt $i failed, waiting 10s..."
        if [ $i -eq 10 ]; then
            echo "❌ Health check failed after 10 attempts"
            docker logs $CONTAINER_ID --tail=50
            exit 1
        fi
        sleep 10
    fi
done

# Final status
echo "=== Deployment Summary ==="
echo "Service: $SERVICE_NAME"
echo "Port: $SERVICE_PORT" 
echo "Status: ✅ HEALTHY"
echo "✅ Deployment completed successfully"
