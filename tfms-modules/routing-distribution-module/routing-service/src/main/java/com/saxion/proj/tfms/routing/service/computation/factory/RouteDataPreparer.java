package com.saxion.proj.tfms.routing.service.computation.factory;

import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.model.Parcel;
import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RouteDataPreparer {

    private static final ServiceLogger logger = ServiceLogger.getLogger(RouteDataPreparer.class);

    public RouteCoordinatesGroup prepareCoordinates(VRPRequest vrpRequest, TruckAssignment assignment) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "PREPARE_COORDINATES",
                "Preparing coordinates for truck assignment with {} parcels",
                assignment.getParcels().size());

        try {
            Coordinates depot = new Coordinates(vrpRequest.getDepot().getLatitude(), vrpRequest.getDepot().getLongitude());
            Coordinates warehouse = getWarehouseCoordinate(vrpRequest);
            List<Coordinates> parcels = getParcelsCoordinates(assignment.getParcels(), vrpRequest.getParcels());

            logger.debugOp(ServiceName.ROUTING_SERVICE, "PREPARE_COORDINATES",
                    "Successfully prepared coordinates: depot, warehouse, and {} parcel locations",
                    parcels.size());

            return RouteCoordinatesGroup.builder()
                    .depot(depot)
                    .warehouse(warehouse)
                    .parcels(parcels)
                    .build();
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "PREPARE_COORDINATES",
                    "Failed to prepare coordinates - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to prepare coordinates", e);
        }
    }

    public List<Stop> createUnoptimizedStops(RouteCoordinatesGroup coords, TruckAssignment assignment, VRPRequest vrpRequest) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "CREATE_STOPS",
                "Creating unoptimized stops for truck with {} assigned parcels",
                assignment.getParcels().size());

        try {
            List<Parcel> parcels = getParcelsToDeliver(assignment.getParcels(), vrpRequest.getParcels());
            List<Stop> stops = new ArrayList<>();
            Stop.addOrUpdateStop(stops, new Stop(coords.getDepot(), new ArrayList<>(), StopType.DEPOT));
            Stop.addOrUpdateStop(stops, new Stop(coords.getWarehouse(), new ArrayList<>(), StopType.WAREHOUSE));

            for (Parcel p : parcels) {
                List<Parcel> parcelList = new ArrayList<>();
                parcelList.add(p);
                Stop.addOrUpdateStop(stops, new Stop(new Coordinates(p.getDeliveryLatitude(), p.getDeliveryLongitude()), parcelList, StopType.CUSTOMER));
            }

            logger.debugOp(ServiceName.ROUTING_SERVICE, "CREATE_STOPS",
                    "Successfully created {} unoptimized stops (including depot and warehouse)",
                    stops.size());

            return stops;
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "CREATE_STOPS",
                    "Failed to create unoptimized stops - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to create unoptimized stops", e);
        }
    }

    private List<Parcel> getParcelsToDeliver(List<TruckAssignment.ParcelInfo> assignedParcels, List<Parcel> allParcels) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "PARCEL_MAPPING",
                "Mapping {} assigned parcels from {} total parcels",
                assignedParcels.size(), allParcels.size());

        try {
            Map<String, Parcel> parcelMap = allParcels.stream()
                    .collect(Collectors.toMap(Parcel::getParcelName, p -> p));

            List<Parcel> result = assignedParcels.stream()
                    .map(pi -> parcelMap.get(pi.getParcelId()))
                    .filter(Objects::nonNull)
                    .toList();

            if (result.size() != assignedParcels.size()) {
                logger.warnOp(ServiceName.ROUTING_SERVICE, "PARCEL_MAPPING",
                        "Some parcels not found: assigned {} but mapped only {}",
                        assignedParcels.size(), result.size());
            } else {
                logger.debugOp(ServiceName.ROUTING_SERVICE, "PARCEL_MAPPING",
                        "Successfully mapped all {} parcels", result.size());
            }

            return result;
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "PARCEL_MAPPING",
                    "Failed to map parcels - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to map parcels", e);
        }
    }

    private List<Coordinates> getParcelsCoordinates(List<TruckAssignment.ParcelInfo> assignedParcels, List<Parcel> allParcels) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "GET_COORDINATES",
                "Extracting coordinates for {} assigned parcels",
                assignedParcels.size());

        try {
            List<Coordinates> coordinates = assignedParcels.stream()
                    .map(pi -> Parcel.findByParcelName(allParcels, pi.getParcelId()))
                    .filter(Objects::nonNull)
                    .map(p -> new Coordinates(p.getDeliveryLatitude(), p.getDeliveryLongitude()))
                    .toList();

            if (coordinates.size() != assignedParcels.size()) {
                logger.warnOp(ServiceName.ROUTING_SERVICE, "GET_COORDINATES",
                        "Some parcel coordinates not found: expected {} but got {}",
                        assignedParcels.size(), coordinates.size());
            } else {
                logger.debugOp(ServiceName.ROUTING_SERVICE, "GET_COORDINATES",
                        "Successfully extracted {} parcel coordinates", coordinates.size());
            }

            return coordinates;
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "GET_COORDINATES",
                    "Failed to extract parcel coordinates - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to extract parcel coordinates", e);
        }
    }

    private Coordinates getWarehouseCoordinate(VRPRequest vrpRequest) {
        logger.debugOp(ServiceName.ROUTING_SERVICE, "GET_WAREHOUSE",
                "Extracting warehouse coordinates from first parcel");

        try {
            if (vrpRequest.getParcels() == null || vrpRequest.getParcels().isEmpty()) {
                logger.errorOp(ServiceName.ROUTING_SERVICE, "GET_WAREHOUSE",
                        "No parcels available to extract warehouse coordinates");
                throw new IllegalStateException("No parcels available to extract warehouse coordinates");
            }

            Parcel firstParcel = vrpRequest.getParcels().get(0);
            Coordinates warehouseCoords = new Coordinates(firstParcel.getWarehouseLatitude(), firstParcel.getWarehouseLongitude());

            logger.debugOp(ServiceName.ROUTING_SERVICE, "GET_WAREHOUSE",
                    "Successfully extracted warehouse coordinates: ({}, {})",
                    warehouseCoords.getLatitude(), warehouseCoords.getLongitude());

            return warehouseCoords;
        } catch (Exception e) {
            logger.errorOp(ServiceName.ROUTING_SERVICE, "GET_WAREHOUSE",
                    "Failed to extract warehouse coordinates - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to extract warehouse coordinates", e);
        }
    }
}

