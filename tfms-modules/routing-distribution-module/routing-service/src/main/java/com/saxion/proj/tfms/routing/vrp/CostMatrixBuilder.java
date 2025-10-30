package com.saxion.proj.tfms.routing.vrp;


import com.saxion.proj.tfms.routing.dto.ParcelInfo;
import com.saxion.proj.tfms.routing.dto.TruckInfo;
import com.saxion.proj.tfms.routing.dto.VrpMetric;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import com.saxion.proj.tfms.routing.service.DistanceMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Builds cost matrices for OR-Tools optimization based on different metrics.
 * Single Responsibility Principle: Handles only cost matrix construction
 */
@Component
public class CostMatrixBuilder {

    @Autowired
    private CapacityCalculator capacityCalculator;

    public long[][] buildCostMatrix(VrpRequestDto request, DistanceMatrixService.DistanceMatrix distanceMatrix) {
        VrpMetric metric = request.getMetric() != null ? request.getMetric() : VrpMetric.DISTANCE;
        long[][] distanceArray = distanceMatrix.distanceMatrix;
        long[][] timeArray = distanceMatrix.durationMatrix;
        int n = distanceArray.length;

        switch (metric) {
            case DISTANCE:
                return buildDistanceCostMatrix(distanceArray, n);
            case TIME:
                return buildTimeCostMatrix(timeArray, n);
            case BOTH:
                return buildDistanceTimeCostMatrix(distanceArray, timeArray, n);
            case DISTANCE_CAPACITY:
                return buildCapacityAwareCostMatrix(request, distanceArray, null, n);
            case TIME_CAPACITY:
                return buildCapacityAwareCostMatrix(request, null, timeArray, n);
            case DISTANCE_TIME_CAPACITY:
                return buildTripleOptimizedCostMatrix(request, distanceArray, timeArray, n);
            default:
                return buildDistanceCostMatrix(distanceArray, n);
        }
    }

    private long[][] buildDistanceCostMatrix(long[][] distanceArray, int n) {
        long[][] costMatrix = new long[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(distanceArray[i], 0, costMatrix[i], 0, n);
        }
        return costMatrix;
    }

    private long[][] buildTimeCostMatrix(long[][] timeArray, int n) {
        long[][] costMatrix = new long[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(timeArray[i], 0, costMatrix[i], 0, n);
        }
        return costMatrix;
    }

    private long[][] buildDistanceTimeCostMatrix(long[][] distanceArray, long[][] timeArray, int n) {
        long[][] costMatrix = new long[n][n];
        long maxDistance = findMaxValue(distanceArray, n);
        long maxTime = findMaxValue(timeArray, n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double normalizedDistance = (double) distanceArray[i][j] / maxDistance;
                double normalizedTime = (double) timeArray[i][j] / maxTime;
                costMatrix[i][j] = (long) ((normalizedDistance + normalizedTime) * 10000);
            }
        }

        return costMatrix;
    }

    private long[][] buildCapacityAwareCostMatrix(VrpRequestDto request, long[][] distanceArray,
                                                   long[][] timeArray, int n) {
        long[][] costMatrix = new long[n][n];

        double totalParcelVolume = request.getParcels().stream()
                .mapToDouble(ParcelInfo::getVolume).sum();
        double totalTruckCapacity = request.getTrucks().stream()
                .mapToDouble(TruckInfo::getVolume).sum();
        double overallUtilization = capacityCalculator.calculateUtilization(totalParcelVolume, totalTruckCapacity);

        long[][] primaryArray = (distanceArray != null) ? distanceArray : timeArray;
        long maxPrimary = findMaxValue(primaryArray, n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double normalizedPrimary = (double) primaryArray[i][j] / maxPrimary;
                double capacityFactor = capacityCalculator.calculateCapacityFactor(overallUtilization);
                costMatrix[i][j] = (long) ((normalizedPrimary * capacityFactor) * 10000);
            }
        }

        return costMatrix;
    }

    private long[][] buildTripleOptimizedCostMatrix(VrpRequestDto request, long[][] distanceArray,
                                                    long[][] timeArray, int n) {
        long[][] costMatrix = new long[n][n];

        double totalParcelVolume = request.getParcels().stream()
                .mapToDouble(ParcelInfo::getVolume).sum();
        double totalTruckCapacity = request.getTrucks().stream()
                .mapToDouble(TruckInfo::getVolume).sum();
        double overallUtilization = capacityCalculator.calculateUtilization(totalParcelVolume, totalTruckCapacity);

        long maxDistance = findMaxValue(distanceArray, n);
        long maxTime = findMaxValue(timeArray, n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double normalizedDistance = (double) distanceArray[i][j] / maxDistance;
                double normalizedTime = (double) timeArray[i][j] / maxTime;
                double capacityWeight = capacityCalculator.calculateCapacityWeight(overallUtilization);
                double combinedCost = (0.4 * normalizedDistance) + (0.4 * normalizedTime) + (0.2 * capacityWeight);
                costMatrix[i][j] = (long) (combinedCost * 10000);
            }
        }

        return costMatrix;
    }

    private long findMaxValue(long[][] array, int n) {
        long max = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                max = Math.max(max, array[i][j]);
            }
        }
        return max;
    }
}
