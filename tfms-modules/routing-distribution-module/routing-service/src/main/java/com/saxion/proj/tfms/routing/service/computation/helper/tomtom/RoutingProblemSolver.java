package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.service.computation.helper.RoutingProvider;
import com.saxion.proj.tfms.routing.service.computation.helper.constants.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("RoutingProblemSolver")
public class RoutingProblemSolver implements RoutingProvider {

    private static final Logger log = LoggerFactory.getLogger(RoutingProblemSolver.class);

    @Autowired
    private TomTomRouteCalculator tomTomRouteCalculator;


    @Override
    public List<Stop> calculateRoute(RouteCoordinatesGroup routeCoordinatesGroup) {

        Coordinates depotCoordinate = routeCoordinatesGroup.getDepot();
        Coordinates warehouseCoordinates = routeCoordinatesGroup.getWarehouse();
        List<Coordinates> parcels = routeCoordinatesGroup.getParcels();
        List<Parcel> parcelList = routeCoordinatesGroup.getParcelList();


        // Step 1: Initial heuristic clustering by shift time
        Map<Coordinates, Double> coordinatesDistanceMap = HeuristicsDistanceFinder.findStraightDistanceInkm(warehouseCoordinates, parcels);

        ClusterResult heuristicClusters = HeuristicsClusterMaker.clusterByShift(
                coordinatesDistanceMap,
                warehouseCoordinates,
                Patterns.PATTERN_HEURISTIC,
                8,
                40
        );

        // Step 2: Optimize each cluster using TomTom and rebalance if needed
        ClusterResult optimizedClusters = optimizeClustersWithTomTom(
                warehouseCoordinates,
                heuristicClusters,
                Patterns.PATTERN_REALISTIC
        );


        // Step 3: Convert optimized clusters to stops
        List<Stop> stops = convertClustersToStops(
                depotCoordinate,
                warehouseCoordinates,
                optimizedClusters,
                parcelList
        );

        return stops;
    }



    /**
     * Optimize each cluster using TomTom API and rebalance deliveries if time exceeds 2.5 hours
     * Also stores route metrics (distance, time) for each truck/shift
     * IMPORTANT: Deduplicates identical coordinates before sending to TomTom to avoid duplicate waypoints
     */
    private ClusterResult optimizeClustersWithTomTom(
            Coordinates warehouse,
            ClusterResult heuristicClusters,
            List<Patterns.ShiftBlock> shiftBlocks
    ) {
        Map<Integer, List<Coordinates>> clusters = heuristicClusters.getShiftClusters();
        List<Coordinates> undelivered = new ArrayList<>(heuristicClusters.getUndeliveredParcels());
        Map<Integer, TruckRouteMetrics> truckMetricsMap = new java.util.HashMap<>();

        log.debug("Starting optimization with {} clusters", clusters.size());

        // Process each cluster and validate with TomTom
        for (int shiftIndex = 0; shiftIndex < shiftBlocks.size(); shiftIndex++) {
            log.debug("Processing Shift {}", shiftIndex);

            List<Coordinates> clusterDeliveries = clusters.get(shiftIndex);

            if (clusterDeliveries == null || clusterDeliveries.isEmpty()) {
                log.debug("Skipping empty cluster for shift {}", shiftIndex);
                continue;
            }

            log.debug("Original cluster deliveries: {}", clusterDeliveries.size());

            Patterns.ShiftBlock shift = shiftBlocks.get(shiftIndex);
            int maxTimeSeconds = calculateMaxTimeSeconds(shift);

            // Deduplicate identical coordinates (multiple parcels at same location = 1 stop for TomTom)
            List<Coordinates> uniqueCoordinates = deduplicateCoordinates(clusterDeliveries);
            log.debug("After deduplication: {} unique coordinates", uniqueCoordinates.size());

            // Get TomTom optimized route and time using UNIQUE coordinates only
            com.fasterxml.jackson.databind.JsonNode routeResponse =
                    tomTomRouteCalculator.getOptimizedRoute(warehouse, uniqueCoordinates);

            if (routeResponse != null && routeResponse.has("routes") && !routeResponse.get("routes").isEmpty()) {
                log.debug("TomTom response received for shift {}", shiftIndex);

                com.fasterxml.jackson.databind.JsonNode summary =
                    routeResponse.get("routes").get(0).get("summary");

                int actualTimeSeconds = summary.get("travelTimeInSeconds").asInt();
                int actualDistanceMeters = summary.get("lengthInMeters").asInt();

                // Extract the REARRANGED coordinates from TomTom response
                List<Coordinates> optimizedSequence = tomTomRouteCalculator.getOptimizedSequence(routeResponse, uniqueCoordinates);

                log.debug("Optimized sequence size: {}", optimizedSequence != null ? optimizedSequence.size() : 0);

                // Ensure we never store empty list - optimizedSequence should have coordinates
                if (optimizedSequence == null || optimizedSequence.isEmpty()) {
                    log.warn("Optimized sequence empty for shift {}, using {} unique coordinates as fallback",
                        shiftIndex, uniqueCoordinates.size());
                    clusters.put(shiftIndex, uniqueCoordinates);
                } else {
                    log.debug("Shift {}: Storing {} optimized coordinates", shiftIndex, optimizedSequence.size());
                    clusters.put(shiftIndex, optimizedSequence);
                }

                // Store metrics for this truck/shift
                TruckRouteMetrics metrics = new TruckRouteMetrics(
                    shiftIndex,
                    actualDistanceMeters,
                    actualTimeSeconds,
                    optimizedSequence == null || optimizedSequence.isEmpty() ? uniqueCoordinates.size() : optimizedSequence.size()
                );

                // If time exceeds limit, move excess deliveries to next cluster or undelivered
                if (actualTimeSeconds > maxTimeSeconds) {
                    log.info("Time exceeded for shift {} ({}s > {}s), rebalancing",
                        shiftIndex, actualTimeSeconds, maxTimeSeconds);

                    List<Coordinates> toRebalance = (optimizedSequence == null || optimizedSequence.isEmpty()) ? uniqueCoordinates : optimizedSequence;

                    List<Coordinates> rebalanced = rebalanceCluster(
                            warehouse,
                            toRebalance,
                            maxTimeSeconds,
                            shiftIndex,
                            clusters,
                            undelivered
                    );

                    log.debug("After rebalancing: {} coordinates", rebalanced.size());

                    // Deduplicate rebalanced list before storing
                    List<Coordinates> deduplicatedRebalanced = deduplicateCoordinates(rebalanced);
                    log.debug("After deduplication: {} coordinates", deduplicatedRebalanced.size());

                    clusters.put(shiftIndex, deduplicatedRebalanced);

                }
            } else {
                // FALLBACK: TomTom API failed or returned empty response
                // Keep the original deduplicated coordinates instead of losing them
                log.warn("TomTom API failed for shift {}, using {} fallback coordinates",
                    shiftIndex, uniqueCoordinates.size());
                clusters.put(shiftIndex, uniqueCoordinates);

                // Try to estimate metrics manually
                int estimatedTime = (uniqueCoordinates.size() - 1) * 600; // ~10 min per stop estimate
                TruckRouteMetrics metrics = new TruckRouteMetrics(
                    shiftIndex,
                    0,  // Distance unknown
                    estimatedTime,
                    uniqueCoordinates.size()
                );

            }

            // Verify what's actually in the cluster now
            List<Coordinates> verifyCluster = clusters.get(shiftIndex);
            log.debug("Cluster {} now has {} coordinates", shiftIndex,
                verifyCluster != null ? verifyCluster.size() : 0);
        }

        log.debug("Final cluster status: {} clusters processed", shiftBlocks.size());



        return new ClusterResult(clusters, undelivered);
    }


    /**
     * Calculate maximum time in seconds for a shift (work time - breaks + buffer)
     */
    private int calculateMaxTimeSeconds(Patterns.ShiftBlock shift) {
        int trafficBuffer = (shift.workMinutes / 60) * shift.trafficBufferPerHour;
        int availableMinutes = shift.workMinutes - shift.breakMinutes + trafficBuffer;
        return availableMinutes * 60; // Convert to seconds
    }

    /**
     * Remove duplicate coordinates (multiple parcels at same location)
     * Keeps first occurrence, removes subsequent identical coordinates
     * @param coordinates List that may contain duplicates
     * @return List with only unique coordinates (by lat/lon)
     */
    private List<Coordinates> deduplicateCoordinates(List<Coordinates> coordinates) {
        List<Coordinates> unique = new ArrayList<>();

        for (Coordinates coord : coordinates) {
            boolean isDuplicate = false;

            // Check if this coordinate already exists in unique list
            for (Coordinates existing : unique) {
                if (Math.abs(existing.getLatitude() - coord.getLatitude()) < 0.000001 &&
                    Math.abs(existing.getLongitude() - coord.getLongitude()) < 0.000001) {
                    isDuplicate = true;
                    break;
                }
            }

            // Add only if not a duplicate
            if (!isDuplicate) {
                unique.add(coord);
            }
        }

        return unique;
    }

    /**
     * Rebalance cluster by removing deliveries until time constraint is met
     * If even a single delivery exceeds time, move all to next shift/undelivered
     */
    private List<Coordinates> rebalanceCluster(
            Coordinates warehouse,
            List<Coordinates> clusterDeliveries,
            int maxTimeSeconds,
            int currentShiftIndex,
            Map<Integer, List<Coordinates>> allClusters,
            List<Coordinates> undelivered
    ) {
        List<Coordinates> balanced = new ArrayList<>(clusterDeliveries);

        log.debug("Rebalancing: Starting with {} deliveries, max time: {}s", balanced.size(), maxTimeSeconds);

        // Remove deliveries from the end until time fits
        while (!balanced.isEmpty()) {
            com.fasterxml.jackson.databind.JsonNode response =
                    tomTomRouteCalculator.getOptimizedRoute(warehouse, balanced);

            if (response != null && response.has("routes") && !response.get("routes").isEmpty()) {
                int time = response.get("routes").get(0).get("summary").get("travelTimeInSeconds").asInt();

                log.debug("Testing with {} deliveries: {}s", balanced.size(), time);

                if (time <= maxTimeSeconds) {
                    // Time fits! Now move excess deliveries to next cluster or undelivered
                    // Important: collect removed items before modifying balanced list
                    List<Coordinates> removed = new ArrayList<>();
                    for (int i = balanced.size(); i < clusterDeliveries.size(); i++) {
                        removed.add(clusterDeliveries.get(i));
                    }

                    if (!removed.isEmpty()) {
                        log.info("Moving {} deliveries to next shift/undelivered", removed.size());

                        // Try to add to next cluster
                        int nextShift = currentShiftIndex + 1;
                        if (allClusters.containsKey(nextShift)) {
                            List<Coordinates> nextCluster = allClusters.get(nextShift);
                            nextCluster.addAll(0, removed); // Add at beginning
                            log.debug("Added {} deliveries to shift {}", removed.size(), nextShift);
                        } else {
                            undelivered.addAll(removed);
                            log.debug("Added {} deliveries to undelivered list", removed.size());
                        }
                    }

                    log.debug("Rebalancing complete: keeping {} deliveries", balanced.size());
                    return balanced;
                }
            }

            // Remove last delivery and try again
            if (!balanced.isEmpty()) {
                log.debug("Removing delivery from end, trying with {}", balanced.size() - 1);
                balanced.remove(balanced.size() - 1);
            }
        }

        // If we get here, even a single delivery exceeds time limit!
        // Move ALL deliveries to next shift or undelivered
        log.error("Even 0 deliveries exceed time limit! Moving ALL {} deliveries", clusterDeliveries.size());

        int nextShift = currentShiftIndex + 1;
        if (allClusters.containsKey(nextShift)) {
            List<Coordinates> nextCluster = allClusters.get(nextShift);
            nextCluster.addAll(0, clusterDeliveries);
            log.info("Moved all {} deliveries to shift {}", clusterDeliveries.size(), nextShift);
        } else {
            undelivered.addAll(clusterDeliveries);
            log.info("Moved all {} deliveries to undelivered", clusterDeliveries.size());
        }

        return new ArrayList<>();  // Return empty for this shift
    }

    /**
     * Convert optimized clusters to Stop objects
     */
    private List<Stop> convertClustersToStops(
            Coordinates depot,
            Coordinates warehouse,
            ClusterResult clusters,
            List<Parcel> parcelList
    ) {
        List<Stop> allStops = new ArrayList<>();

        // Add depot as first stop
        Stop.addOrUpdateStop(allStops, new Stop(depot, new ArrayList<>(), StopType.DEPOT));

        // Add warehouse stop
        Stop.addOrUpdateStop(allStops, new Stop(warehouse, new ArrayList<>(), StopType.WAREHOUSE));

        // Add delivery stops from all clusters
        for (Map.Entry<Integer, List<Coordinates>> entry : clusters.getShiftClusters().entrySet()) {
            List<Coordinates> clusterCoords = entry.getValue();

            for (Coordinates coord : clusterCoords) {
                // Find parcels for this coordinate
                List<Parcel> parcelsForStop = findParcelsForCoordinate(coord, parcelList);
                Stop deliveryStop = new Stop(coord, parcelsForStop, StopType.CUSTOMER);
                Stop.addOrUpdateStop(allStops, deliveryStop);
            }
        }

        // Add depot as last stop (return) - use add() not addOrUpdateStop() to avoid merging with start depot
        allStops.add(new Stop(depot, new ArrayList<>(), StopType.DEPOT));

        return allStops;
    }

    /**
     * Find all parcels that should be delivered to a specific coordinate
     */
    private List<Parcel> findParcelsForCoordinate(Coordinates coord, List<Parcel> allParcels) {
        List<Parcel> result = new ArrayList<>();

        if (allParcels == null || coord == null) {
            return result;
        }

        for (Parcel parcel : allParcels) {
            if (Math.abs(parcel.getDeliveryLatitude() - coord.getLatitude()) < 0.000001 &&
                Math.abs(parcel.getDeliveryLongitude() - coord.getLongitude()) < 0.000001) {
                result.add(parcel);
            }
        }

        return result;
    }
}




