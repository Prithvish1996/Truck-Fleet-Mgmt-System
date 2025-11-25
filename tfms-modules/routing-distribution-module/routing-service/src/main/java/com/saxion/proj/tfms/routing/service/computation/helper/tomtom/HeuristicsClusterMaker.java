package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.routing.model.ClusterResult;
import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.service.computation.helper.constants.Patterns;

import java.util.*;
import java.util.stream.Collectors;

public class HeuristicsClusterMaker {

    /**
     * Cluster deliveries into shift blocks based on available working time,
     * precomputed distances, and service time per delivery.
     */
    public static ClusterResult clusterByShift(
            Map<Coordinates, Double> distanceMap,
            Coordinates warehouse,
            List<Patterns.ShiftBlock> shiftBlocks,
            int avgServiceTimeMinutes,
            double avgSpeedKmPerHour
    ) {
        List<Coordinates> sortedDeliveries = sortDeliveriesByDistance(distanceMap);
        Map<Integer, List<Coordinates>> clusters = new HashMap<>();
        List<Coordinates> remainingDeliveries = new ArrayList<>(sortedDeliveries);

        for (int i = 0; i < shiftBlocks.size(); i++) {
            Patterns.ShiftBlock shift = shiftBlocks.get(i);
            List<Coordinates> cluster = assignDeliveriesToShift(
                    remainingDeliveries,
                    distanceMap,
                    shift,
                    avgServiceTimeMinutes,
                    avgSpeedKmPerHour
            );
            clusters.put(i, cluster);
        }

        // Remaining deliveries = could not fit in any shift
        List<Coordinates> undeliveredParcels = new ArrayList<>(remainingDeliveries);

        return new ClusterResult(clusters, undeliveredParcels);
    }

    // ------------------------
    // 🔹 Step 1: Sorting Phase
    // ------------------------
    private static List<Coordinates> sortDeliveriesByDistance(Map<Coordinates, Double> distanceMap) {
        return distanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // ------------------------
    // 🔹 Step 2: Assignment Phase
    // ------------------------
    private static List<Coordinates> assignDeliveriesToShift(
            List<Coordinates> remainingDeliveries,
            Map<Coordinates, Double> distanceMap,
            Patterns.ShiftBlock shift,
            int avgServiceTimeMinutes,
            double avgSpeedKmPerHour
    ) {
        List<Coordinates> cluster = new ArrayList<>();
        int availableMinutes = computeAvailableMinutes(shift);
        int accumulatedTime = 0;

        Iterator<Coordinates> iterator = remainingDeliveries.iterator();
        while (iterator.hasNext()) {
            Coordinates delivery = iterator.next();
            int timeNeeded = estimateDeliveryTime(distanceMap.get(delivery), avgServiceTimeMinutes, avgSpeedKmPerHour);

            if (accumulatedTime + timeNeeded <= availableMinutes) {
                cluster.add(delivery);
                accumulatedTime += timeNeeded;
                iterator.remove();
            } else {
                break;
            }
        }

        return cluster;
    }

    // ------------------------
    // 🔹 Utility Methods
    // ------------------------
    private static int estimateDeliveryTime(double distanceKm, int avgServiceTimeMinutes, double avgSpeedKmPerHour) {
        double travelTimeMinutes = (distanceKm / avgSpeedKmPerHour) * 60;
        return (int) Math.ceil(travelTimeMinutes + avgServiceTimeMinutes);
    }

    private static int computeAvailableMinutes(Patterns.ShiftBlock shift) {
        int trafficBuffer = (shift.workMinutes / 60) * shift.trafficBufferPerHour;
        return shift.workMinutes - shift.breakMinutes + trafficBuffer;
    }
}
