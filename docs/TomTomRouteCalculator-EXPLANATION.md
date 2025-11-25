# TomTomRouteCalculator Class - Complete Explanation

## Overview
This class is responsible for communicating with the TomTom Routing API to optimize delivery routes using real-time traffic data and TSP (Traveling Salesman Problem) optimization.

---

## Class Structure

### 1. **Configuration & Initialization**

```java
@Service
public class TomTomRouteCalculator {
    private static final String TOMTOM_API_BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute";
    
    @Value("${tomtom.api.key:GkSRasdpaBrnBwHN5aO5uhj2hFsR6YHy}")
    private String apiKey="GkSRasdpaBrnBwHN5aO5uhj2hFsR6YHy";
    
    private final RestTemplate restTemplate;
    
    public TomTomRouteCalculator() {
        this.restTemplate = new RestTemplate();
    }
}
```

**Purpose:**
- `@Service`: Marks this as a Spring-managed service bean
- `TOMTOM_API_BASE_URL`: Base endpoint for TomTom Routing API
- `apiKey`: API key for authentication (loaded from properties or default)
- `restTemplate`: HTTP client for making API calls
- Constructor initializes the RestTemplate

---

## Main Methods

### 2. **getOptimizedRoute() - Core Route Calculation**

```java
public TomTomRouteResponse getOptimizedRoute(Coordinates warehouse, List<Coordinates> deliveries)
```

**Purpose:** Makes a call to TomTom API to calculate the optimized route for a set of deliveries.

**Flow:**
1. **Input Validation**
   ```java
   if (deliveries == null || deliveries.isEmpty()) {
       return null;
   }
   ```
   - Returns null if no deliveries provided

2. **Build Location String**
   ```java
   StringBuilder locationsBuilder = new StringBuilder();
   locationsBuilder.append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());
   
   for (Coordinates c : deliveries) {
       locationsBuilder.append(":").append(c.getLatitude()).append(",").append(c.getLongitude());
   }
   
   locationsBuilder.append(":").append(warehouse.getLatitude()).append(",").append(warehouse.getLongitude());
   ```
   - Formats coordinates as: `warehouse:delivery1:delivery2:...:warehouse`
   - Creates a round trip: start at warehouse, visit all deliveries, return to warehouse
   - Format: `lat1,lon1:lat2,lon2:lat3,lon3`

3. **Build API URL with Parameters**
   ```java
   String uri = UriComponentsBuilder.fromUriString(baseUrl)
       .queryParam("key", apiKey)
       .queryParam("computeBestOrder", "true")      // TSP optimization
       .queryParam("routeRepresentation","summaryOnly")  // Minimal response
       .queryParam("traffic", "true")               // Use live traffic
       .queryParam("routeType", "fastest")          // Optimize for speed
       .toUriString();
   ```
   
   **Key Parameters:**
   - `computeBestOrder=true`: **CRITICAL** - Tells TomTom to optimize the waypoint order
   - `routeRepresentation=summaryOnly`: Returns minimal data (just summary and optimization)
   - `traffic=true`: Uses real-time traffic conditions
   - `routeType=fastest`: Prioritizes speed over distance

4. **Make API Call**
   ```java
   TomTomRouteResponse response = restTemplate.getForObject(uri, TomTomRouteResponse.class);
   ```
   - Sends GET request to TomTom
   - Deserializes JSON response into `TomTomRouteResponse` object

5. **Debug Output**
   - Prints route distance, travel time
   - Shows if `optimizedWaypoints` were returned
   - Displays the mapping of original indices to optimized positions

**Returns:** `TomTomRouteResponse` containing route details and optimization data

---

### 3. **getOptimizedSequence() - Extract Optimized Order**

```java
public List<Coordinates> getOptimizedSequence(TomTomRouteResponse response, List<Coordinates> originalDeliveries)
```

**Purpose:** Extracts the optimized delivery order from TomTom's response and reorders the coordinates accordingly.

**Flow (with multiple fallback strategies):**

#### **Strategy 1: Use optimizedWaypoints (PRIMARY - Most Reliable)**

```java
if (response.getOptimizedWaypoints() != null && !response.getOptimizedWaypoints().isEmpty()) {
    // Create array to store the optimized order
    TomTomRouteResponse.OptimizedWaypoint[] orderedWaypoints = 
        new TomTomRouteResponse.OptimizedWaypoint[response.getOptimizedWaypoints().size()];
    
    // Sort by optimizedIndex to get the delivery order
    for (TomTomRouteResponse.OptimizedWaypoint ow : response.getOptimizedWaypoints()) {
        orderedWaypoints[ow.getOptimizedIndex()] = ow;
    }
    
    // Build the optimized sequence
    for (int i = 0; i < orderedWaypoints.length; i++) {
        int providedIndex = orderedWaypoints[i].getProvidedIndex();
        Coordinates coordToAdd = originalDeliveries.get(providedIndex);
        optimizedSequence.add(coordToAdd);
    }
}
```

**How it works:**
- TomTom returns an array like:
  ```
  providedIndex=0 → optimizedIndex=2  (first delivery should be 3rd)
  providedIndex=1 → optimizedIndex=0  (second delivery should be 1st)
  providedIndex=2 → optimizedIndex=1  (third delivery should be 2nd)
  ```
- We create an array indexed by `optimizedIndex`
- Fill it with the corresponding deliveries from `providedIndex`
- Result: `[delivery1, delivery2, delivery0]` - OPTIMIZED ORDER!

**Example:**
- Original: [Amsterdam, Rotterdam, Utrecht]
- TomTom says: Visit Rotterdam first, then Utrecht, then Amsterdam
- optimizedWaypoints:
  - {providedIndex: 0, optimizedIndex: 2} → Amsterdam is 3rd
  - {providedIndex: 1, optimizedIndex: 0} → Rotterdam is 1st  
  - {providedIndex: 2, optimizedIndex: 1} → Utrecht is 2nd
- Result: [Rotterdam, Utrecht, Amsterdam] ✅

#### **Strategy 2: Use legacy waypoints field (BACKUP)**

```java
if (route.getWaypoints() != null && !route.getWaypoints().isEmpty()) {
    for (TomTomRouteResponse.Route.Waypoint wp : route.getWaypoints()) {
        int index = wp.getNumber();
        if (index > 0 && index <= originalDeliveries.size()) {
            Coordinates coordToAdd = originalDeliveries.get(index - 1);
            optimizedSequence.add(coordToAdd);
        }
    }
}
```
- Older TomTom API format
- Less commonly used now

#### **Strategy 3: Use legs to infer order (FALLBACK)**

```java
if (route.getLegs() != null && !route.getLegs().isEmpty()) {
    for (int i = 0; i < route.getLegs().size() - 1; i++) {
        TomTomRouteResponse.Route.Leg leg = route.getLegs().get(i);
        // Get destination point of each leg
        Point destination = leg.getPoints().get(leg.getPoints().size() - 1);
        
        // Find matching coordinate in original deliveries
        Coordinates match = findMatchingCoordinate(destination, originalDeliveries, optimizedSequence);
        optimizedSequence.add(match);
    }
}
```

**How legs work:**
- Each leg = travel from one point to another
- Leg 0: warehouse → first delivery
- Leg 1: first delivery → second delivery
- Leg 2: second delivery → third delivery
- Leg n-1: last delivery → warehouse

By examining the destination of each leg, we can reconstruct the optimized order.

#### **Strategy 4: Use route points (LAST RESORT)**
- Rarely used, extracts coordinates from the full route geometry

#### **Strategy 5: Return original order (ULTIMATE FALLBACK)**
```java
return new ArrayList<>(originalDeliveries);
```
- If all else fails, return the original order

**Returns:** `List<Coordinates>` in the optimized order

---

### 4. **findMatchingCoordinate() - Coordinate Matching Helper**

```java
private Coordinates findMatchingCoordinate(Coordinates target, List<Coordinates> originalList, List<Coordinates> alreadyUsed)
```

**Purpose:** Finds which original delivery coordinate matches a point from the route response.

**Why needed?** 
- TomTom returns route points with slightly different precision (e.g., 52.417860 instead of 52.417288)
- We need to match these back to our original delivery coordinates

**Flow:**
```java
double[] tolerances = {0.00001, 0.0001, 0.001, 0.01}; // ~1m, ~11m, ~111m, ~1.1km

for (double tolerance : tolerances) {
    for (Coordinates candidate : originalList) {
        if (alreadyUsed.contains(candidate)) continue;
        
        double latDiff = Math.abs(candidate.getLatitude() - target.getLatitude());
        double lonDiff = Math.abs(candidate.getLongitude() - target.getLongitude());
        
        if (latDiff < tolerance && lonDiff < tolerance) {
            return candidate;
        }
    }
}
```

**Strategy:**
1. Try very precise match (±1 meter)
2. If no match, try ±11 meters
3. Then ±111 meters
4. Finally ±1.1 km
5. Skip coordinates already used (prevents duplicates)

**Returns:** Matching `Coordinates` object or null

---

### 5. **formatTime() - Time Formatting Utility**

```java
private String formatTime(int seconds)
```

**Purpose:** Converts seconds to human-readable format.

**Examples:**
- 3661 seconds → "1h 1m 1s"
- 125 seconds → "2m 5s"
- 45 seconds → "45s"

---

### 6. **computeBestRoutePerCluster() - Batch Processing**

```java
public void computeBestRoutePerCluster(Coordinates warehouse, ClusterResult clusterResult)
```

**Purpose:** Calculates optimized routes for multiple clusters (shifts/trucks).

**Flow:**
```java
for (Map.Entry<Integer, List<Coordinates>> entry : clusters.entrySet()) {
    int shiftIndex = entry.getKey();
    List<Coordinates> deliveries = entry.getValue();
    
    TomTomRouteResponse response = getOptimizedRoute(warehouse, deliveries);
    
    // Print statistics
    System.out.println("Shift " + shiftIndex + ":");
    System.out.println("  Distance: " + totalDistanceMeters);
    System.out.println("  Time: " + totalTimeSeconds);
}
```

**Use case:** When you have multiple trucks/shifts, calculate optimal route for each one.

---

## Data Transfer Objects (DTOs)

### 7. **TomTomRouteResponse - Response Structure**

```java
public static class TomTomRouteResponse {
    private List<Route> routes;
    private List<OptimizedWaypoint> optimizedWaypoints;  // ⭐ KEY FIELD!
}
```

**Structure matches TomTom API JSON response:**
```json
{
  "routes": [{ 
    "summary": { "lengthInMeters": 12000, "travelTimeInSeconds": 900 },
    "legs": [...],
    "points": [...]
  }],
  "optimizedWaypoints": [
    { "providedIndex": 0, "optimizedIndex": 2 },
    { "providedIndex": 1, "optimizedIndex": 0 },
    { "providedIndex": 2, "optimizedIndex": 1 }
  ]
}
```

### **Inner Classes:**

#### **OptimizedWaypoint**
```java
public static class OptimizedWaypoint {
    private int providedIndex;   // Where we put it in the request
    private int optimizedIndex;  // Where TomTom says it should go
}
```

#### **Route**
- `summary`: Distance and time totals
- `legs`: Individual segments between waypoints
- `points`: GPS coordinates along the route
- `waypoints`: Legacy optimization format

#### **Summary**
```java
public static class Summary {
    private int lengthInMeters;      // Total distance
    private int travelTimeInSeconds; // Total time including traffic
}
```

#### **Leg**
```java
public static class Leg {
    private Summary summary;      // Distance/time for this leg
    private List<Point> points;   // GPS trace for this segment
    private List<Guidance> guidance; // Turn-by-turn instructions
}
```

#### **Point**
```java
public static class Point {
    private double latitude;
    private double longitude;
}
```

---

## Complete Flow Example

### Scenario: 3 deliveries in Amsterdam

**Step 1: Input**
```java
Coordinates warehouse = new Coordinates(52.4, 4.9);  // Amsterdam center
List<Coordinates> deliveries = [
    new Coordinates(52.45, 4.88),  // North
    new Coordinates(52.35, 4.92),  // South
    new Coordinates(52.42, 4.85)   // West
];
```

**Step 2: Call getOptimizedRoute()**
- Builds URL: `warehouse:north:south:west:warehouse`
- Sends to TomTom with `computeBestOrder=true`

**Step 3: TomTom Response**
```json
{
  "routes": [{
    "summary": { "lengthInMeters": 25000, "travelTimeInSeconds": 1800 }
  }],
  "optimizedWaypoints": [
    { "providedIndex": 0, "optimizedIndex": 1 },  // North → 2nd
    { "providedIndex": 1, "optimizedIndex": 2 },  // South → 3rd
    { "providedIndex": 2, "optimizedIndex": 0 }   // West → 1st
  ]
}
```

**Step 4: Call getOptimizedSequence()**
- Reads optimizedWaypoints
- Creates array: `[null, null, null]`
- Fills by optimizedIndex:
  - Position 0 ← providedIndex 2 (West)
  - Position 1 ← providedIndex 0 (North)
  - Position 2 ← providedIndex 1 (South)

**Step 5: Result**
```java
optimizedSequence = [
    new Coordinates(52.42, 4.85),  // West (was index 2)
    new Coordinates(52.45, 4.88),  // North (was index 0)
    new Coordinates(52.35, 4.92)   // South (was index 1)
];
```

**Step 6: Driver follows route**
- Warehouse → West → North → South → Warehouse
- Total: 25km in 30 minutes ✅
- Optimized route saves time and fuel!

---

## Key Design Decisions

### 1. **Multiple Fallback Strategies**
- Primary: optimizedWaypoints (most reliable)
- Backup: legacy waypoints
- Fallback: leg-based inference
- Last resort: original order

**Why?** TomTom API can return data in different formats depending on parameters and API version.

### 2. **Progressive Tolerance Matching**
- Start with tight tolerance (1m)
- Gradually increase if no match
- Prevents false matches while being flexible

### 3. **Round-trip Routes**
- Always include warehouse at start and end
- Matches real-world delivery scenarios
- TomTom optimizes the internal waypoint order

### 4. **Traffic Integration**
- `traffic=true` uses live traffic data
- Routes adapt to current conditions
- More realistic time estimates

---

## Common Issues & Solutions

### Issue 1: "No optimization data available"
**Cause:** TomTom didn't return optimizedWaypoints
**Solution:** Check API parameters, falls back to leg-based matching

### Issue 2: Incomplete leg-based ordering
**Cause:** Route points don't match original coordinates precisely
**Solution:** Uses progressive tolerance matching

### Issue 3: Order unchanged
**Cause:** Original order was already optimal
**Result:** TomTom returns same order (this is correct!)

---

## Performance Characteristics

- **API Call Time:** 1-3 seconds per route
- **Optimization Quality:** Near-optimal TSP solution
- **Max Waypoints:** 150 deliveries per request
- **Rate Limiting:** Depends on API plan

---

## Summary

This class is a **robust wrapper** around TomTom's Routing API that:
1. ✅ Sends delivery coordinates to TomTom
2. ✅ Gets back an optimized route order
3. ✅ Handles multiple response formats gracefully
4. ✅ Returns reordered deliveries for the driver to follow

The magic happens in `getOptimizedSequence()` where we decode TomTom's optimization data and reorder our delivery list accordingly!

