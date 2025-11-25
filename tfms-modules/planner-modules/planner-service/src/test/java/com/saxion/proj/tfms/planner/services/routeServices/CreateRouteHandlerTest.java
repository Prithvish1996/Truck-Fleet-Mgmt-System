//package com.saxion.proj.tfms.planner.services.routeServices;
//
//import com.saxion.proj.tfms.commons.constants.StatusEnum;
//import com.saxion.proj.tfms.commons.model.*;
//import com.saxion.proj.tfms.planner.dto.GenerateRouteRequestDto;
//import com.saxion.proj.tfms.planner.dto.GenerateRouteResponseDto;
//import com.saxion.proj.tfms.planner.dto.routing.model.VRPRequest;
//import com.saxion.proj.tfms.planner.dto.routing.model.VRPResponse;
//import com.saxion.proj.tfms.planner.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class CreateRouteHandlerTest {
//
//    private RouteRepository routeRepository;
//    private ParcelRepository parcelRepository;
//    private TruckRepository truckRepository;
//    private DriverRepository driverRepository;
//    private DepotRepository depotRepository;
//    private LocationRepository locationRepository;
//    private RouteStopRepository routeStopRepository;
//    private WarehouseRepository warehouseRepository;
//
//    private CreateRouteHandler handler;
//
//    @BeforeEach
//    void setUp() {
//        routeRepository = mock(RouteRepository.class);
//        parcelRepository = mock(ParcelRepository.class);
//        truckRepository = mock(TruckRepository.class);
//        driverRepository = mock(DriverRepository.class);
//        depotRepository = mock(DepotRepository.class);
//        locationRepository = mock(LocationRepository.class);
//        routeStopRepository = mock(RouteStopRepository.class);
//        warehouseRepository = mock(WarehouseRepository.class);
//
//        handler = spy(new CreateRouteHandler(
//                routeRepository,
//                parcelRepository,
//                truckRepository,
//                driverRepository,
//                depotRepository,
//                locationRepository,
//                routeStopRepository,
//                warehouseRepository
//        ));
//    }
//
//    // -------------------------
//    // 1. No valid parcels
//    // -------------------------
//    @Test
//    void handle_noParcels_returnsError() {
//        GenerateRouteRequestDto request = new GenerateRouteRequestDto();
//        request.setParcelIds(List.of(1L, 2L));
//
//        when(parcelRepository.findAllById(request.getParcelIds())).thenReturn(List.of());
//
//        var res = handler.handle(request);
//
//        assertFalse(res.isSuccess());
//        assertEquals("No valid parcels found for the provided IDs.", res.getMessage());
//    }
//
//    // -------------------------
//    // 2. Parcel with invalid status
//    // -------------------------
//    @Test
//    void handle_invalidParcelStatus_returnsError() {
//        ParcelDao p1 = new ParcelDao();
//        p1.setId(1L);
//        p1.setStatus(StatusEnum.PLANNED); // invalid
//
//        when(parcelRepository.findAllById(List.of(1L))).thenReturn(List.of(p1));
//
//        GenerateRouteRequestDto request = new GenerateRouteRequestDto();
//        request.setParcelIds(List.of(1L));
//
//        var res = handler.handle(request);
//
//        assertFalse(res.isSuccess());
//        assertTrue(res.getMessage().contains("Only parcels with status 'SCHEDULED' can be planned"));
//    }
//
//    // -------------------------
//    // 3. Depot or warehouse not found
//    // -------------------------
//    @Test
//    void handle_depotNotFound_throwsException() {
//        when(parcelRepository.findAllById(List.of(1L))).thenReturn(List.of(validParcel(1L)));
//
//        GenerateRouteRequestDto request = new GenerateRouteRequestDto();
//        request.setParcelIds(List.of(1L));
//        request.setDepot_id(99L);
//
//        when(depotRepository.findById(99L)).thenReturn(Optional.empty());
//
//        assertThrows(RuntimeException.class, () -> handler.handle(request));
//    }
//
//    @Test
//    void handle_warehouseNotFound_throwsException() {
//        when(parcelRepository.findAllById(List.of(1L))).thenReturn(List.of(validParcel(1L)));
//
//        DepotDao depot = new DepotDao();
//        depot.setId(1L);
//
//        when(depotRepository.findById(1L)).thenReturn(Optional.of(depot));
//        when(warehouseRepository.findById(2L)).thenReturn(Optional.empty());
//
//        GenerateRouteRequestDto request = new GenerateRouteRequestDto();
//        request.setParcelIds(List.of(1L));
//        request.setDepot_id(1L);
//        request.setWarehouse_id(2L);
//
//        assertThrows(RuntimeException.class, () -> handler.handle(request));
//    }
//
//    // Helper
//    private ParcelDao validParcel(long id) {
//        ParcelDao p = new ParcelDao();
//        p.setId(id);
//        p.setStatus(StatusEnum.SCHEDULED);
//        p.setName("Parcel " + id);
//        p.setWarehouse(new WareHouseDao() {{
//            setLocation(new LocationDao() {{
//                setLatitude(52.0);
//                setLongitude(4.0);
//            }});
//        }});
//        p.setDeliveryLocation(new LocationDao() {{
//            setLatitude(52.1);
//            setLongitude(4.1);
//        }});
//        p.setRecipientName("John");
//        p.setRecipientPhone("12345");
//        return p;
//    }
//}
//
