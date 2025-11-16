package com.saxion.proj.tfms.planner.services.routeServices;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.constants.StopType;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.*;
import com.saxion.proj.tfms.planner.abstractions.routeServices.ICreateRoute;
import com.saxion.proj.tfms.planner.dto.*;
import com.saxion.proj.tfms.planner.dto.routing.model.*;
import com.saxion.proj.tfms.planner.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("createRouteHandler")
public class CreateRouteHandler implements ICreateRoute {

    private final RouteRepository routeRepository;
    private final ParcelRepository parcelRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final DepotRepository depotRepository;
    private final LocationRepository locationRepository;
    private final RouteStopRepository routeStopRepository;
    private final WarehouseRepository warehouseRepository;

    public CreateRouteHandler(RouteRepository routeRepository,
                              ParcelRepository parcelRepository,
                              TruckRepository truckRepository,
                              DriverRepository driverRepository,
                              DepotRepository depotRepository,
                              LocationRepository locationRepository, RouteStopRepository routeStopRepository,
                              WarehouseRepository warehouseRepository) {
        this.routeRepository = routeRepository;
        this.parcelRepository = parcelRepository;
        this.truckRepository = truckRepository;
        this.driverRepository = driverRepository;
        this.depotRepository = depotRepository;
        this.locationRepository = locationRepository;
        this.routeStopRepository = routeStopRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public ApiResponse<GenerateRouteResponseDto> handle(GenerateRouteRequestDto request) {

        //1. Validate parcels
        List<ParcelDao> selectedParcels = parcelRepository.findAllById(request.getParcelIds());
        if (selectedParcels.isEmpty()) {
            return ApiResponse.error("No valid parcels found for the provided IDs.");
        }

        List<Long> invalidStatusParcelIds = selectedParcels.stream()
                .filter(p -> p.getStatus() != StatusEnum.SCHEDULED)
                .map(ParcelDao::getId)
                .collect(Collectors.toList());
        if (!invalidStatusParcelIds.isEmpty()) {
            return ApiResponse.error("Only parcels with status 'SCHEDULED' can be planned. Invalid IDs: " + invalidStatusParcelIds);
        }

        //2. Validate depot
        DepotDao depotEntity = depotRepository.findById(request.getDepot_id())
                .orElseThrow(() -> new RuntimeException("Depot not found for ID: " + request.getDepot_id()));

        DepotInfo depotInfo = new DepotInfo(
                depotEntity.getId(),
                depotEntity.getName(),
                depotEntity.getLocation() != null ? depotEntity.getLocation().getLatitude() : 0.0,
                depotEntity.getLocation() != null ? depotEntity.getLocation().getLongitude() : 0.0
        );

        //2b. Validate warehouse
        WareHouseDao warehouseEntity = warehouseRepository.findById(request.getWarehouse_id())
                .orElseThrow(() -> new RuntimeException("Warehouse not found for ID: " + request.getWarehouse_id()));

        //3. Prepare VRP parcels
        List<Parcel> vrpParcels = selectedParcels.stream().map(p -> {
            Parcel rp = new Parcel();
            rp.setParcelId(p.getId());
            rp.setParcelName(p.getName());
            rp.setVolume(p.getVolume() != null ? p.getVolume() : 0.0);
            if (p.getWarehouse() != null && p.getWarehouse().getLocation() != null) {
                rp.setWarehouseLatitude(p.getWarehouse().getLocation().getLatitude());
                rp.setWarehouseLongitude(p.getWarehouse().getLocation().getLongitude());
            }
            if (p.getDeliveryLocation() != null) {
                rp.setDeliveryLatitude(p.getDeliveryLocation().getLatitude());
                rp.setDeliveryLongitude(p.getDeliveryLocation().getLongitude());
            }
            rp.setRecipientName(p.getRecipientName());
            rp.setRecipientPhone(p.getRecipientPhone());
            rp.setDeliveryInstructions(p.getDeliveryInstructions());
            return rp;
        }).collect(Collectors.toList());

        if (vrpParcels.isEmpty()) {
            return ApiResponse.error("No valid parcel coordinates found for routing.");
        }

        //4. Call VRP Service (stub)
        VRPRequest vrpRequest = new VRPRequest(depotInfo, vrpParcels);
        VRPResponse vrpResponse = callExternalVrpService(vrpRequest);

        //5. Persist Routes, Stops, Parcels using default duration of 8 hours
        String duration = "8 hours";
        List<RouteDao> savedRoutes = new ArrayList<>();
        Set<Long> assignedParcelIds = new HashSet<>();

        if (vrpResponse != null && vrpResponse.getWarehouseRoutingResults() != null) {
            for (WarehouseRoutingResult wr : vrpResponse.getWarehouseRoutingResults()) {
                if (wr.getTruckRoutes() == null) continue;

                for (TruckRouteInfo tri : wr.getTruckRoutes()) {
                    TruckDao truck = truckRepository.findByPlateNumber(tri.getTruckPlateNumber()).orElse(null);

                    // Step 5a: Persist route first
                    RouteDao route = new RouteDao();
                    route.setTruck(truck);
                    route.setDepot(depotEntity);
                    route.setWarehouse(warehouseEntity);
                    route.setTotalDistance(Optional.ofNullable(tri.getTotalDistance()).orElse(0L));
                    route.setTotalTransportTime(Optional.ofNullable(tri.getTotalTransportTime()).orElse(0L));
                    route.setNote(vrpResponse.getNotes());
                    route.setStatus(StatusEnum.PLANNED);
                    route.setStartTime(ZonedDateTime.now());
                    route.setScheduleDate(ZonedDateTime.now());
                    route.setDuration(duration);
                    route = routeRepository.save(route); // persist first

                    // Step 5b: Persist each stop individually
                    List<RouteStopDao> persistedStops = new ArrayList<>();
                    int priority = 1;

                    if (tri.getRouteStops() != null) {
                        for (Stop s : tri.getRouteStops()) {
                            RouteStopDao stop = new RouteStopDao();
                            stop.setDescription("Auto-generated stop");
                            stop.setPriority(priority++);
                            stop.setDuration(duration);
                            stop.setStopType(s.getStopType() == null ? StopType.CUSTOMER :
                                    StopType.valueOf(s.getStopType().name()));
                            stop.setRoute(route);

                            // Persist location
                            if (s.getCoordinates() != null) {
                                double lat = s.getCoordinates().getLatitude();
                                double lon = s.getCoordinates().getLongitude();
                                LocationDao location = locationRepository.findByLatitudeAndLongitude(lat, lon)
                                        .orElseGet(() -> {
                                            LocationDao loc = new LocationDao();
                                            loc.setLatitude(lat);
                                            loc.setLongitude(lon);
                                            loc.setAddress("Auto-generated");
                                            loc.setCity("Auto");
                                            loc.setPostalCode("Auto");
                                            return locationRepository.save(loc);
                                        });
                                stop.setLocation(location);
                            }

                            // Persist stop first
                            stop = routeStopRepository.save(stop);
                            persistedStops.add(stop);

                            final RouteStopDao finalStop = stop;
                            // Step 5c: Update parcels to reference persisted stop
                            if (s.getParcelsToDeliver() != null) {
                                for (Parcel rp : s.getParcelsToDeliver()) {
                                    parcelRepository.findById(rp.getParcelId()).ifPresent(parcel -> {
                                        parcel.setStop(finalStop);
                                        parcel.setStatus(StatusEnum.PLANNED);
                                        parcelRepository.save(parcel);
                                        assignedParcelIds.add(parcel.getId());
                                    });
                                }
                            }
                        }
                    }

                    // Step 5d: Link stops back to route and update
                    route.setStops(persistedStops);
                    routeRepository.save(route);
                    savedRoutes.add(route);
                }
            }
        }

        //6 Collect unassigned parcels
        List<ParcelDao> unassigned = selectedParcels.stream()
                .filter(p -> !assignedParcelIds.contains(p.getId()))
                .toList();

        //7 Build response DTO
        GenerateRouteResponseDto responseDto = new GenerateRouteResponseDto();
        responseDto.setAssignRoutes(savedRoutes.stream().map(this::mapRouteToResponse).toList());
        responseDto.setUnAssignedRoute(
                unassigned.stream().map(p -> {
                    RouteResponseDto dto = new RouteResponseDto();
                    dto.setNote("Unassigned parcel");
                    dto.setRouteStops(Collections.emptyList());
                    return dto;
                }).toList()
        );
        responseDto.setTrucks(truckRepository.findAllByIsAvailableTrue()
                .stream().map(this::mapTruck).toList());
        responseDto.setDrivers(driverRepository.findByIsAvailableTrue()
                .stream().map(this::mapDriver).toList());

        return ApiResponse.success(responseDto);
    }

    //Stub VRP
    protected VRPResponse callExternalVrpService(VRPRequest vrpRequest) {
        TruckRouteInfo tri = new TruckRouteInfo();
        tri.setTruckPlateNumber("TRK-001");
        tri.setDepotId(vrpRequest.getDepot().getDepotId());
        tri.setDepotName(vrpRequest.getDepot().getDepotName());
        tri.setTotalTransportTime(500L);
        tri.setTotalDistance(8L);

        List<Stop> stops = new ArrayList<>();
        WarehouseRoutingResult wrr = new WarehouseRoutingResult();
        if (vrpRequest.getParcels() != null) {
            for (Parcel p : vrpRequest.getParcels()) {
                wrr.setGeneratedForWarehouse(p.getWarehouseId());
                //define coordinates
                LocationResponseDto loc = new LocationResponseDto();
                loc.setLatitude(p.getDeliveryLatitude());
                loc.setLongitude(p.getDeliveryLongitude());

                //define stops
                Stop stop = new Stop();
                stop.setCoordinates(loc);
                stop.setParcelsToDeliver(Collections.singletonList(p));
                stop.setStopType(StopType.CUSTOMER);

                stops.add(stop);
            }
        }
        tri.setRouteStops(stops);
        wrr.setTruckRoutes(Collections.singletonList(tri));

        VRPResponse response = new VRPResponse();
        response.setWarehouseRoutingResults(List.of(wrr));
        response.setTotalTrucksUsed(1);
        response.setEsitimatedDistanceInkm(500.0);
        response.setEstimatedTimeInMinutes(40000);
        response.setNotes("Default VRP stub");
        return response;
    }

    //DTO Mappers
    private RouteResponseDto mapRouteToResponse(RouteDao route) {
        RouteResponseDto dto = new RouteResponseDto();
        dto.setRouteId(route.getId());
        dto.setTruckId(route.getTruck() != null ? route.getTruck().getId() : null);
        dto.setTruckPlateNumber(route.getTruck() != null ? route.getTruck().getPlateNumber() : null);
        dto.setDepotId(route.getDepot() != null ? route.getDepot().getId() : null);
        dto.setDepotName(route.getDepot() != null ? route.getDepot().getName() : null);
        dto.setTotalDistance(route.getTotalDistance());
        dto.setTotalTransportTime(route.getTotalTransportTime());
        dto.setNote(route.getNote());

        dto.setRouteStops(route.getStops().stream().map(stop -> {
            StopDto sDto = new StopDto();
            sDto.setStopId(stop.getId());
            sDto.setPriority(stop.getPriority());
            sDto.setStopType(stop.getStopType());
            sDto.setParcelsToDeliver(stop.getParcels().stream()
                    .map(this::mapParcel)
                    .collect(Collectors.toList()));
            return sDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    private ParcelResponseDto mapParcel(ParcelDao p) {
        ParcelResponseDto dto = new ParcelResponseDto();
        dto.setParcelId(p.getId());
        dto.setName(p.getName());
        dto.setWeight(p.getWeight());
        dto.setVolume(p.getVolume());
        dto.setRecipientName(p.getRecipientName());
        dto.setRecipientPhone(p.getRecipientPhone());
        dto.setDeliveryInstructions(p.getDeliveryInstructions());
        return dto;
    }

    private TruckResponseDto mapTruck(TruckDao t) {
        return new TruckResponseDto(
                t.getId(),
                t.getPlateNumber(),
                t.getType(),
                t.getMake(),
                t.getLastServiceDate(),
                t.getLastServicedBy(),
                t.getVolume(),
                t.getIsAvailable(),
                0,
                0
        );
    }

    private DriverResponseDto mapDriver(DriverDao d) {
        DriverResponseDto dto = new DriverResponseDto();
        dto.setId(d.getId());
        dto.setEmail(d.getUser().getEmail());
        dto.setUserName(d.getUser().getUsername());
        dto.setIsAvailable(d.getIsAvailable());
        dto.setCity(d.getLocation().getCity());
        dto.setLongitude(d.getLocation().getLongitude());
        dto.setLatitude(d.getLocation().getLatitude());
        return dto;
    }
}
