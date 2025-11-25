package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.routing.model.ClusterResult;
import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.service.computation.helper.constants.Patterns;

import java.util.*;

public class HeuristicsClusterMakerTest {
    public static void main(String[] args) {

        // 1️⃣ Create mock warehouse and delivery coordinates
        Coordinates warehouse = new Coordinates(52.3702, 4.8952); // Amsterdam

        List<Coordinates> deliveries = List.of(
                new Coordinates(52.3676, 4.9041), // near Amsterdam
                new Coordinates(52.5200, 13.4050), // Berlin
                new Coordinates(51.9244, 4.4777),  // Rotterdam
                new Coordinates(52.0907, 5.1214),  // Utrecht
                new Coordinates(51.4416, 5.4697),  // Eindhoven
                new Coordinates(50.8503, 4.3517)   // Brussels
        );

        // 2️⃣ Compute distances warehouse → deliveries
        Map<Coordinates, Double> distanceMap =
                HeuristicsDistanceFinder.findStraightDistanceInkm(warehouse, deliveries);

        // 3️⃣ Define shift pattern (mock)
        List<Patterns.ShiftBlock> shiftBlocks = List.of(
                new Patterns.ShiftBlock(180, 15, 5),  // 3h work, 15min break, 5min traffic buffer/hour
                new Patterns.ShiftBlock(150, 10, 5)   // 2.5h work, 10min break
        );

        // 4️⃣ Cluster by shift
        ClusterResult result = HeuristicsClusterMaker.clusterByShift(
                distanceMap,
                warehouse,
                shiftBlocks,
                10,   // avg service time per delivery (minutes)
                50    // avg speed (km/h)
        );

        // 5️⃣ Print results
        System.out.println("=== Clustering Result ===");
        System.out.println(" Warehouse: " + formatCoordinate(warehouse));
        for (Map.Entry<Integer, List<Coordinates>> entry : result.getShiftClusters().entrySet()) {
            System.out.printf("Shift %d (%d deliveries):%n", entry.getKey(), entry.getValue().size());
            entry.getValue().forEach(c -> System.out.println("  - " + formatCoordinate(c)));
        }

        System.out.println("\nUndelivered parcels: " + result.getUndeliveredParcels().size());
        result.getUndeliveredParcels().forEach(c ->
                System.out.println("  ❌ " + formatCoordinate(c))
        );
    }

    private static String formatCoordinate(Coordinates c) {
        return String.format("(%.4f, %.4f)", c.getLatitude(), c.getLongitude());
    }
}
