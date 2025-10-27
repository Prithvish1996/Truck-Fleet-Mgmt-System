## Quick Start Guide - Testing with Your GraphHopper API Key

### Step 1: Add Your API Key

Open the file:
```
tfms-modules/common-modules/src/main/java/com/saxion/proj/tfms/commons/utility/depotwarehouse/MainTest.java
```

Replace line 24:
```java
private static final String GRAPHHOPPER_API_KEY = "YOUR_API_KEY_HERE";
```

With your actual API key:
```java
private static final String GRAPHHOPPER_API_KEY = "your-actual-api-key-from-graphhopper";
```

### Step 2: Run the Test

**Option A: Using Maven from terminal**
```bash
cd /home/prithvish/Documents/GitHub/02

mvn -pl tfms-modules/common-modules compile exec:java \
  -Dexec.mainClass="com.saxion.proj.tfms.commons.utility.depotwarehouse.MainTest"
```

**Option B: Using IDE**
- Right-click on `MainTest.java`
- Select "Run MainTest.main()"

### Expected Output

```
=== Truck-Warehouse Assignment Test (GraphHopper) ===

Initializing GraphHopper routing...
✓ GraphHopper ready (using hosted API)

Creating test data (Monaco coordinates)...
Testing single route...
  Depot A -> Warehouse X: 0.62 km, 1.34 min

Computing assignments with real routing...

Assigned: Truck-1 | Route: Depot A -> Warehouse X
Assigned: Truck-2 | Route: Depot A -> Warehouse Y

=== Results ===
Depot: Depot A, Warehouse: Warehouse X, Truck: Truck-1, Distance: 0.62 km, Time: 1.34 min
Depot: Depot A, Warehouse: Warehouse Y, Truck: Truck-2, Distance: 0.58 km, Time: 1.28 min

✓ Test completed successfully!
Note: Using GraphHopper hosted API (500 requests/day free tier)
```

### What the Test Does

1. ✅ Validates your API key is set
2. ✅ Connects to GraphHopper hosted API
3. ✅ Tests a single route calculation
4. ✅ Assigns 2 trucks to 2 warehouses based on distance/time
5. ✅ Shows detailed routing results

### Troubleshooting

**If you see: "Please set your GraphHopper API key!"**
- You forgot to replace `YOUR_API_KEY_HERE` with your actual key

**If you see: "GraphHopper API error: 401"**
- Your API key is invalid or expired
- Double-check you copied it correctly

**If you see: "GraphHopper API error: 429"**
- You've exceeded the 500 requests/day free tier limit
- Wait until tomorrow or upgrade your plan

**If you see: Connection error**
- Check your internet connection
- Verify firewall isn't blocking GraphHopper API

### Next Steps

Once the test works:
1. ✅ Try different profiles: change `"car"` to `"truck"` on line 44
2. ✅ Test with your own coordinates
3. ✅ Adjust thresholds in `AssignmentService` (currently 10km, 30min)
4. ✅ Integrate into your application

### API Key Security Tips

⚠️ **Don't commit your API key to Git!**

Better approach for production:
```java
// Read from environment variable
private static final String GRAPHHOPPER_API_KEY = 
    System.getenv("GRAPHHOPPER_API_KEY");
```

Then set environment variable:
```bash
export GRAPHHOPPER_API_KEY="your-key-here"
```

Or use application.properties:
```properties
graphhopper.api.key=your-key-here
```

