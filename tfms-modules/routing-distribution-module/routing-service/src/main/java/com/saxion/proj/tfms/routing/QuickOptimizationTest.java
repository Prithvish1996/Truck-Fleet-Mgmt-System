package com.saxion.proj.tfms.routing;

import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.service.computation.helper.tomtom.TomTomRouteCalculator;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick test to verify TomTom optimization is working
 */
public class QuickOptimizationTest {

    public static void main(String[] args) {
        System.out.println("🧪 Quick Optimization Test");
        System.out.println("Testing TomTom route optimization with 5 deliveries\n");

        // Create calculator
        TomTomRouteCalculator calculator = new TomTomRouteCalculator();

        // Warehouse in Amsterdam
        Coordinates warehouse = new Coordinates(52.4, 4.9);

        // 5 delivery locations around Amsterdam
        List<Coordinates> deliveries = new ArrayList<>();
        deliveries.add(new Coordinates(52.417288, 4.924497));  // 0
        deliveries.add(new Coordinates(52.363774, 4.899642));  // 1
        deliveries.add(new Coordinates(52.361920, 4.876255));  // 2
        deliveries.add(new Coordinates(52.368162, 4.965557));  // 3
        deliveries.add(new Coordinates(52.447264, 4.932809));  // 4

        System.out.println("Original delivery order:");
        for (int i = 0; i < deliveries.size(); i++) {
            Coordinates c = deliveries.get(i);
            System.out.printf("  [%d] %.6f, %.6f%n", i, c.getLatitude(), c.getLongitude());
        }
        System.out.println();

        // Get optimized route
        com.fasterxml.jackson.databind.JsonNode response = calculator.getOptimizedRoute(warehouse, deliveries);

        if (response != null && response.has("routes") && response.get("routes").size() > 0) {
            System.out.println("\n✅ Route calculated successfully!");
            
            // Get optimized sequence
            List<Coordinates> optimizedSequence = calculator.getOptimizedSequence(response, deliveries);
            
            System.out.println("\n🎯 Final optimized delivery order:");
            for (int i = 0; i < optimizedSequence.size(); i++) {
                Coordinates c = optimizedSequence.get(i);
                System.out.printf("  [%d] %.6f, %.6f", i, c.getLatitude(), c.getLongitude());
                
                // Find which original index this was
                for (int j = 0; j < deliveries.size(); j++) {
                    if (Math.abs(deliveries.get(j).getLatitude() - c.getLatitude()) < 0.000001 &&
                        Math.abs(deliveries.get(j).getLongitude() - c.getLongitude()) < 0.000001) {
                        System.out.printf(" (was index %d)", j);
                        break;
                    }
                }
                System.out.println();
            }
            
            // Check if order changed
            boolean changed = false;
            for (int i = 0; i < optimizedSequence.size(); i++) {
                if (Math.abs(optimizedSequence.get(i).getLatitude() - deliveries.get(i).getLatitude()) > 0.000001 ||
                    Math.abs(optimizedSequence.get(i).getLongitude() - deliveries.get(i).getLongitude()) > 0.000001) {
                    changed = true;
                    break;
                }
            }
            
            System.out.println("\n" + (changed ? "✅ ORDER WAS OPTIMIZED!" : "⚠️  Order unchanged"));
        } else {
            System.out.println("❌ Failed to get route from TomTom");
        }
    }
}

