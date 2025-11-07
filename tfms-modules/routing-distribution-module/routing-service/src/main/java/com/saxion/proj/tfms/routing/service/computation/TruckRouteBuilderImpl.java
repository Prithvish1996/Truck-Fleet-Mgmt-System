package com.saxion.proj.tfms.routing.service.computation;

import com.saxion.proj.tfms.routing.constant.StopType;
import com.saxion.proj.tfms.routing.model.*;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.helper.RoutingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TruckRouteBuilderImpl implements TruckRouteBuilder {

    @Autowired
    @Qualifier("TomTomRoutingProblemSolver")
    private RoutingProvider routingProvider;
    @Autowired
    private VRPRequest vRPRequest;


    @Override
    public WarehouseRoutingResult buildFullRouteForTrucks(VRPRequest vrpRequest, AssignmentResponse assignmentResponse, Long warehouseId) {

        WarehouseRoutingResult warehouseRoutingResult = new WarehouseRoutingResult();
        List<TruckRouteInfo> truckRoutes = new ArrayList<>();
        for(TruckAssignment singleTruckDetail : assignmentResponse.getTruckAssignments()) {
            TruckRouteInfo truckRouteInfo =   createRouteForTheTruck(vrpRequest, singleTruckDetail, warehouseId);
            truckRoutes.add(truckRouteInfo);
        }
        warehouseRoutingResult.setTruckRoutes(truckRoutes);
        warehouseRoutingResult.setGeneratedForWarehouse(warehouseId);
        return warehouseRoutingResult;
    }

    private TruckRouteInfo createRouteForTheTruck(VRPRequest vrpRequest, TruckAssignment singleTruckAssingmentInfo, Long warehouseId) {

        TruckRouteInfo truckRouteInfo = new TruckRouteInfo();
        String truckName = singleTruckAssingmentInfo.getTruckId();
        Long depotId = vrpRequest.getDepot().getDepotId();
        String depotName = vrpRequest.getDepot().getDepotName();
        Integer totalDistanceInMeters = 0;
        List<Stop> routeStops = new ArrayList<>();
        Long totalTransportTimeInSeconds = 0L;
        Coordinates depotCoordinate = getDepotCoordinate(vrpRequest);
        Coordinates warehouseCoordinate = getWarehouseCoordinate(vrpRequest);
        List<Coordinates> parcelsCoordinates = getParcelsCoordinates(singleTruckAssingmentInfo.getParcels(),  vrpRequest.getParcels());
        RouteCoordinatesGroup routeCoordinatesGroup= groupCoordinates(depotCoordinate,warehouseCoordinate,parcelsCoordinates);

        // Commenting for now to mock behaviour
//        List<Stop> stops = routingProvider.calculateRoute(routeCoordinatesGroup);

        List<Stop> stops = unOptimized(routeCoordinatesGroup,singleTruckAssingmentInfo, vrpRequest);

        truckRouteInfo.setTruckName(truckName);
        truckRouteInfo.setDepotId(depotId);
        truckRouteInfo.setDepotName(depotName);
        truckRouteInfo.setRouteStops(routeStops);
        truckRouteInfo.setTotalDistance(totalDistanceInMeters);
        truckRouteInfo.setTotalTransportTime(totalTransportTimeInSeconds);
        return truckRouteInfo;
    }

    private List<Stop> unOptimized(RouteCoordinatesGroup routeCoordinatesGroup,
                                   TruckAssignment singleTruckAssignmentInfo,
                                   VRPRequest vrpRequest) {

        Coordinates depotCoordinates = routeCoordinatesGroup.getDepot();
        Coordinates warehouseCoordinates = routeCoordinatesGroup.getWarehouse();

        // All parcel coordinates (might be used for mapping or visualization)
        List<Coordinates> parcelCoordinates = routeCoordinatesGroup.getParcels();

        // Parcels actually assigned to this truck
        List<Parcel> parcels = getParcelsToDeliver(singleTruckAssignmentInfo.getParcels(), vrpRequest.getParcels());

        List<Stop> stops = new ArrayList<>();

        // Depot stop
        Stop.addOrUpdateStop(stops, new Stop(depotCoordinates, new ArrayList<>(), StopType.DEPOT));

        // Warehouse stop
        Stop.addOrUpdateStop(stops, new Stop(warehouseCoordinates, new ArrayList<>(), StopType.WAREHOUSE));

        // Customer stops for parcels assigned to this truck
        for (Parcel parcel : parcels) {
            List<Parcel> parcelList = new ArrayList<>();
            parcelList.add(parcel);

            Stop parcelStop = new Stop(new Coordinates(parcel.getDeliveryLatitude(),parcel.getDeliveryLongitude()), parcelList, StopType.CUSTOMER);
            Stop.addOrUpdateStop(stops, parcelStop);
        }

        return stops;
    }





    private RouteCoordinatesGroup groupCoordinates(Coordinates depotCoordinate, Coordinates warehouseCoordinate, List<Coordinates> parcelsCoordinates) {
        return  RouteCoordinatesGroup.builder()
                .warehouse(warehouseCoordinate)
                .depot(depotCoordinate)
                .parcels(parcelsCoordinates).build();

    }

    private List<Parcel> getParcelsToDeliver(List<TruckAssignment.ParcelInfo> assignedParcels, List<Parcel> allParcels) {
        Map<String, Parcel> parcelMap = allParcels.stream()
                .collect(Collectors.toMap(Parcel::getParcelName, p -> p));
        return assignedParcels.stream()
                .map(pi -> parcelMap.get(pi.getParcelId()))
                .filter(Objects::nonNull)
                .toList();
    }



    private List<Coordinates> getParcelsCoordinates(List<TruckAssignment.ParcelInfo> assignedParcels,
                                                    List<Parcel> allParcels) {
        return assignedParcels.stream()
                .map(pi -> Parcel.findByParcelName(allParcels, pi.getParcelId()))
                .filter(Objects::nonNull)
                .map(p -> new Coordinates(p.getDeliveryLatitude(), p.getDeliveryLongitude()))
                .toList();
    }


    private Coordinates getWarehouseCoordinate(VRPRequest vrpRequest) {

        // We are taking first parcel as all the list of parcels have same warehouse coordinates served by the same Truck
        double warehouseLatitude = vrpRequest.getParcels().get(0).getWarehouseLatitude();
        double warehouseLongitude = vrpRequest.getParcels().get(0).getWarehouseLongitude();
        return new Coordinates(warehouseLatitude, warehouseLongitude);
    }

    private Coordinates getDepotCoordinate(VRPRequest vrpRequest) {
        double depotLatitude = vrpRequest.getDepot().getLatitude();
        double depotLongitude = vrpRequest.getDepot().getLongitude();
        return new Coordinates(depotLatitude, depotLongitude);
    }


}
