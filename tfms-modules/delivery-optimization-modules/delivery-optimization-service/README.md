# Delivery Optimization Service

This module provides intelligent delivery route optimization for the Truck Fleet Management System (TFMS). It processes large datasets of packages and warehouses to create efficient delivery routes that respect truck capacity constraints.

## Features

- **Data Processing**: Supports both JSON and CSV input formats
- **Route Optimization**: Implements TSP-based algorithms with capacity constraints
- **Truck Assignment**: Intelligent assignment of packages to trucks based on weight limits
- **Distance Calculation**: Uses Haversine formula for accurate geographical distance calculations
- **2-Opt Improvement**: Applies route optimization techniques to minimize travel distance
- **REST API**: Comprehensive REST endpoints for integration

## Architecture

### Core Components

1. **Data Models**
   - `Package`: Represents individual packages with weight, size, and location
   - `Warehouse`: Contains packages and warehouse location information
   - `Truck`: Defines truck capacity and availability
   - `DeliveryRoute`: Optimized route with assigned packages

2. **Services**
   - `DataParsingService`: Handles JSON/CSV data parsing
   - `RouteOptimizationService`: Implements TSP-based optimization algorithms
   - `DeliveryOptimizationService`: Main orchestration service

3. **Controllers**
   - `DeliveryOptimizationController`: REST API endpoints

## API Endpoints

### POST /api/delivery-optimization/optimize/json
Optimize delivery routes from JSON data.

**Request Body:**
```json
{
  "warehouses": [
    {
      "name": "Central Warehouse",
      "latitude": 52.3676,
      "longitude": 4.9041,
      "delivery_date": "2024-01-15",
      "packages": [
        {
          "name": "Package 1",
          "weight": 25.5,
          "size": 0.8,
          "latitude": 52.3700,
          "longitude": 4.9100
        }
      ]
    }
  ],
  "trucks": [
    {
      "truck_id": "TRUCK-001",
      "weight_limit": 1000.0
    }
  ]
}
```

### POST /api/delivery-optimization/optimize/csv
Optimize delivery routes from CSV file upload.

**Expected CSV Format:**
```csv
warehouse_name,warehouse_lat,warehouse_lng,delivery_date,package_name,package_weight,package_size,package_lat,package_lng
Central Warehouse,52.3676,4.9041,2024-01-15,Electronics Package 1,25.5,0.8,52.3700,4.9100
```

### POST /api/delivery-optimization/optimize/csv-content
Optimize delivery routes from CSV content (text).

### GET /api/delivery-optimization/health
Health check endpoint.

## Response Format

```json
{
  "optimization_id": "uuid",
  "total_routes": 3,
  "total_packages": 12,
  "total_distance": 45.67,
  "optimization_status": "COMPLETED",
  "routes": [
    {
      "route_id": 1,
      "truck_id": "TRUCK-001",
      "total_weight": 850.5,
      "package_count": 5,
      "estimated_distance": 15.23,
      "estimated_duration_minutes": 45,
      "packages": [...],
      "route_sequence": [...]
    }
  ],
  "unassigned_packages": [...]
}
```

## Algorithm Details

### Route Optimization
The service uses a combination of algorithms:

1. **Nearest Neighbor**: Initial route construction
2. **2-Opt Improvement**: Route optimization to minimize distance
3. **Capacity Constraints**: Ensures truck weight limits are respected
4. **No Duplicates**: Prevents packages from being assigned to multiple routes

### Distance Calculation
Uses the Haversine formula for accurate geographical distance calculations:
- Accounts for Earth's curvature
- Returns distance in kilometers
- Suitable for delivery route optimization

### Truck Assignment
- Trucks are sorted by capacity (largest first)
- Packages are assigned based on proximity and weight constraints
- Each package can only be assigned to one route

## Usage Examples

### JSON Optimization
```bash
curl -X POST http://localhost:8080/api/delivery-optimization/optimize/json \
  -H "Content-Type: application/json" \
  -d @sample-data.json
```

### CSV File Upload
```bash
curl -X POST http://localhost:8080/api/delivery-optimization/optimize/csv \
  -F "file=@sample-data.csv"
```

## Testing

The module includes comprehensive tests:
- Unit tests for all services
- Integration tests for API endpoints
- Sample data files for testing

Run tests with:
```bash
mvn test
```

## Dependencies

- Spring Boot 3.5.5
- Spring Data JPA
- OpenCSV for CSV processing
- Jackson for JSON processing
- Lombok for boilerplate reduction
- H2 Database for testing

## Configuration

The service integrates with the main TFMS application and uses the same database configuration. No additional configuration is required.
