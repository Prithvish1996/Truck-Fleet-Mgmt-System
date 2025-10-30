package com.saxion.proj.tfms.routing.vrp;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.saxion.proj.tfms.routing.dto.*;
import com.saxion.proj.tfms.routing.service.DistanceMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Parses OR-Tools solution into VRP response DTOs.
 * Single Responsibility Principle: Handles only solution parsing and transformation
 */
@Component
public class SolutionParser {

    @Autowired
    private LocationMapper locationMapper;

    public VrpResponseDto parseSolution(Assignment solution, RoutingModel routing, RoutingIndexManager manager,
                                        VrpRequestDto request, LocationMapper.LocationMapping locationMapping) {

        VrpResponseDto response = new VrpResponseDto();
        List<TruckRoute> truckRoutes = new ArrayList<>();

        int vehiclesUsed = 0;
        long totalDistance = 0;
        long totalTime = 0;

        for (int vehicleId = 0; vehicleId < request.getTrucks().size(); vehicleId++) {
            TruckInfo truck = request.getTrucks().get(vehicleId);
            
            RouteInfo routeInfo = parseVehicleRoute(vehicleId, solution, routing, manager,
                    request, locationMapping);

            if (routeInfo.hasStops) {
                TruckRoute truckRoute = buildTruckRoute(truck, routeInfo);
                truckRoutes.add(truckRoute);
                vehiclesUsed++;
                totalDistance += routeInfo.routeDistance;
                totalTime += routeInfo.routeTime;
            }
        }

        response.setTotalVehiclesUsed(vehiclesUsed);
        response.setTotalDistance((int) totalDistance);
        response.setTotalTime((int) totalTime);
        response.setTruckRoutes(truckRoutes);

        return response;
    }

    private RouteInfo parseVehicleRoute(int vehicleId, Assignment solution,
                                       RoutingModel routing, RoutingIndexManager manager,
                                       VrpRequestDto request, LocationMapper.LocationMapping locationMapping) {
        
        RouteInfo routeInfo = new RouteInfo();
        long index = routing.start(vehicleId);
        
        routeInfo.activities.add(createActivity("start", "", "depot", 
                locationMapping.getLocations().get(0), 0, 0, 0));

        while (!routing.isEnd(index)) {
            long previousIndex = index;
            index = solution.value(routing.nextVar(index));
            
            int nodeIndex = manager.indexToNode(index);
            long arcCost = routing.getArcCostForVehicle(previousIndex, index, vehicleId);
            
            routeInfo.routeDistance += arcCost;
            routeInfo.routeTime += arcCost / 13;
            routeInfo.arrivalTime += (int) (arcCost / 13);

            if (!routing.isEnd(index)) {
                routeInfo.hasStops = true;
                processRouteStop(nodeIndex, arcCost, routeInfo, locationMapping, request);
            }
        }

        routeInfo.activities.add(createActivity("end", "", "depot",
                locationMapping.getLocations().get(0), routeInfo.arrivalTime, 0, 0));

        return routeInfo;
    }

    private void processRouteStop(int nodeIndex, long arcCost, RouteInfo routeInfo,
                                  LocationMapper.LocationMapping locationMapping, VrpRequestDto request) {
        
        DistanceMatrixService.Location location = locationMapping.getLocations().get(nodeIndex);
        String locationId = locationMapper.getLocationKey(nodeIndex, locationMapping);
        
        String activityType = "";
        String parcelId = "";
        
        if (locationId != null) {
            if (locationId.startsWith("pickup_")) {
                activityType = "pickupShipment";
                parcelId = locationId.substring(7);
                routeInfo.assignedParcels.add(parcelId);
                
                for (ParcelInfo p : request.getParcels()) {
                    if (p.getParcelId().equals(parcelId)) {
                        routeInfo.warehousesVisited.add(p.getWarehouseId());
                        break;
                    }
                }
            } else if (locationId.startsWith("delivery_")) {
                activityType = "deliverShipment";
                parcelId = locationId.substring(9);
            }
        }
        
        routeInfo.activities.add(createActivity(activityType, parcelId, locationId, location,
                routeInfo.arrivalTime, (int) arcCost, (int) (arcCost / 13)));
    }

    private TruckRoute buildTruckRoute(TruckInfo truck, RouteInfo routeInfo) {
        TruckRoute truckRoute = new TruckRoute();
        truckRoute.setTruckId(truck.getTruckName());
        truckRoute.setDistance((int) routeInfo.routeDistance);
        truckRoute.setTransportTime((int) routeInfo.routeTime);
        truckRoute.setActivities(routeInfo.activities);
        truckRoute.setAssignedParcels(routeInfo.assignedParcels);
        truckRoute.setWarehouseVisited(
                routeInfo.warehousesVisited.isEmpty() ? null : routeInfo.warehousesVisited.iterator().next());
        return truckRoute;
    }

    private Activity createActivity(String type, String id, String locationId,
                                    DistanceMatrixService.Location location,
                                    int arrivalTime, int distance, int drivingTime) {
        Activity activity = new Activity();
        activity.setType(type);
        activity.setId(id);
        activity.setLocationId(locationId);
        activity.setLatitude(location.latitude);
        activity.setLongitude(location.longitude);
        activity.setArrivalTime(arrivalTime);
        activity.setEndTime(arrivalTime);
        activity.setDistance(distance);
        activity.setDrivingTime(drivingTime);
        activity.setLoadAfter(null);
        return activity;
    }

    private static class RouteInfo {
        List<Activity> activities = new ArrayList<>();
        List<String> assignedParcels = new ArrayList<>();
        Set<String> warehousesVisited = new HashSet<>();
        long routeDistance = 0;
        long routeTime = 0;
        int arrivalTime = 0;
        boolean hasStops = false;
    }
}
