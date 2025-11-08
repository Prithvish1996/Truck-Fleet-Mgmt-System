package com.saxion.proj.tfms.routing.service.computation;

import com.saxion.proj.tfms.routing.model.TruckRouteInfo;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.model.TruckAssignment;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.factory.TruckRouteFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TruckRouteBuilderImplTest {

    @Mock
    private TruckRouteFactory truckRouteFactory;

    @Mock
    private AssignmentResponse mockAssignmentResponse;

    @InjectMocks
    private TruckRouteBuilderImpl truckRouteBuilder;

    private VRPRequest vrpRequest;
    private Long warehouseId;
    private TruckAssignment truckAssignment1;
    private TruckAssignment truckAssignment2;
    private TruckRouteInfo truckRouteInfo1;
    private TruckRouteInfo truckRouteInfo2;

    @BeforeEach
    void setUp() {
        vrpRequest = new VRPRequest();
        warehouseId = 100L;
        
        TruckAssignment.ParcelInfo parcel1 = new TruckAssignment.ParcelInfo("P001", 10.0);
        TruckAssignment.ParcelInfo parcel2 = new TruckAssignment.ParcelInfo("P002", 15.0);
        
        truckAssignment1 = new TruckAssignment("TRUCK-001", List.of(parcel1), 10.0, 100.0);
        truckAssignment2 = new TruckAssignment("TRUCK-002", List.of(parcel2), 15.0, 100.0);
        
        truckRouteInfo1 = TruckRouteInfo.builder()
                .truckName("TRUCK-001")
                .build();
        
        truckRouteInfo2 = TruckRouteInfo.builder()
                .truckName("TRUCK-002")
                .build();
    }

    @Test
    void buildFullRouteForTrucks_withSingleTruckAssignment_shouldCreateSingleRoute() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(warehouseId, result.getGeneratedForWarehouse());
        assertEquals(1, result.getTruckRoutes().size());
        assertEquals("TRUCK-001", result.getTruckRoutes().get(0).getTruckName());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withMultipleTruckAssignments_shouldCreateMultipleRoutes() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment2, warehouseId))
                .thenReturn(truckRouteInfo2);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(warehouseId, result.getGeneratedForWarehouse());
        assertEquals(2, result.getTruckRoutes().size());
        assertEquals("TRUCK-001", result.getTruckRoutes().get(0).getTruckName());
        assertEquals("TRUCK-002", result.getTruckRoutes().get(1).getTruckName());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment2, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withEmptyTruckAssignments_shouldReturnEmptyRoutes() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(Collections.emptyList());

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(warehouseId, result.getGeneratedForWarehouse());
        assertTrue(result.getTruckRoutes().isEmpty());
        verify(truckRouteFactory, never()).createRouteForTruck(any(), any(), any());
    }

    @Test
    void buildFullRouteForTrucks_withNullTruckAssignments_shouldThrowNullPointerException() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        verify(truckRouteFactory, never()).createRouteForTruck(any(), any(), any());
    }

    @Test
    void buildFullRouteForTrucks_withNullAssignmentResponse_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, null, warehouseId));
        
        verify(truckRouteFactory, never()).createRouteForTruck(any(), any(), any());
    }

    @Test
    void buildFullRouteForTrucks_withNullVrpRequest_shouldPropagateToFactory() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(null, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                null, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        verify(truckRouteFactory, times(1)).createRouteForTruck(null, truckAssignment1, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withNullWarehouseId_shouldAcceptAndProcess() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, null))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, null);

        assertNotNull(result);
        assertNull(result.getGeneratedForWarehouse());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, null);
    }

    @Test
    void buildFullRouteForTrucks_whenFactoryThrowsRuntimeException_shouldPropagateWithContext() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenThrow(new RuntimeException("Factory error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        assertTrue(exception.getMessage().contains("Failed to create route for truck TRUCK-001"));
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_whenFactoryThrowsIllegalArgumentException_shouldWrapInRuntimeException() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        assertTrue(exception.getMessage().contains("Failed to create route for truck TRUCK-001"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_whenFirstTruckFails_shouldStopProcessingAndThrow() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenThrow(new RuntimeException("First truck failed"));

        assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
        verify(truckRouteFactory, never()).createRouteForTruck(vrpRequest, truckAssignment2, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_whenSecondTruckFails_shouldThrowAfterProcessingFirst() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment2, warehouseId))
                .thenThrow(new RuntimeException("Second truck failed"));

        assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment2, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withLargeNumberOfTrucks_shouldProcessAll() {
        List<TruckAssignment> manyTrucks = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            TruckAssignment.ParcelInfo parcel = new TruckAssignment.ParcelInfo("P" + i, 10.0);
            TruckAssignment truck = new TruckAssignment("TRUCK-" + String.format("%03d", i), List.of(parcel), 10.0, 100.0);
            manyTrucks.add(truck);
            
            TruckRouteInfo route = TruckRouteInfo.builder()
                    .truckName("TRUCK-" + String.format("%03d", i))
                    .build();
            
            when(truckRouteFactory.createRouteForTruck(vrpRequest, truck, warehouseId))
                    .thenReturn(route);
        }
        
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(manyTrucks);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(100, result.getTruckRoutes().size());
        verify(truckRouteFactory, times(100)).createRouteForTruck(eq(vrpRequest), any(TruckAssignment.class), eq(warehouseId));
    }

    @Test
    void buildFullRouteForTrucks_withZeroWarehouseId_shouldAcceptAndProcess() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        Long zeroWarehouseId = 0L;
        
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, zeroWarehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, zeroWarehouseId);

        assertNotNull(result);
        assertEquals(zeroWarehouseId, result.getGeneratedForWarehouse());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, zeroWarehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withNegativeWarehouseId_shouldAcceptAndProcess() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        Long negativeWarehouseId = -1L;
        
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, negativeWarehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, negativeWarehouseId);

        assertNotNull(result);
        assertEquals(negativeWarehouseId, result.getGeneratedForWarehouse());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, negativeWarehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withMaxLongWarehouseId_shouldAcceptAndProcess() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        Long maxWarehouseId = Long.MAX_VALUE;
        
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, maxWarehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, maxWarehouseId);

        assertNotNull(result);
        assertEquals(maxWarehouseId, result.getGeneratedForWarehouse());
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, maxWarehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withFactoryReturningNull_shouldIncludeNullInList() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(null);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(1, result.getTruckRoutes().size());
        assertNull(result.getTruckRoutes().get(0));
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withMixedSuccessAndNullReturns_shouldIncludeAll() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment2, warehouseId))
                .thenReturn(null);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(2, result.getTruckRoutes().size());
        assertNotNull(result.getTruckRoutes().get(0));
        assertNull(result.getTruckRoutes().get(1));
        verify(truckRouteFactory, times(2)).createRouteForTruck(eq(vrpRequest), any(TruckAssignment.class), eq(warehouseId));
    }

    @Test
    void buildFullRouteForTrucks_resultShouldBeImmutableList() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertNotNull(result.getTruckRoutes());
        assertThrows(UnsupportedOperationException.class, () -> 
                result.getTruckRoutes().add(truckRouteInfo2));
    }

    @Test
    void buildFullRouteForTrucks_shouldPreserveOrderOfTruckAssignments() {
        TruckAssignment.ParcelInfo parcel3 = new TruckAssignment.ParcelInfo("P003", 20.0);
        TruckAssignment truck3 = new TruckAssignment("TRUCK-003", List.of(parcel3), 20.0, 100.0);
        TruckRouteInfo route3 = TruckRouteInfo.builder().truckName("TRUCK-003").build();
        
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment2, truck3, truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment2, warehouseId))
                .thenReturn(truckRouteInfo2);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truck3, warehouseId))
                .thenReturn(route3);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertEquals(3, result.getTruckRoutes().size());
        assertEquals("TRUCK-002", result.getTruckRoutes().get(0).getTruckName());
        assertEquals("TRUCK-003", result.getTruckRoutes().get(1).getTruckName());
        assertEquals("TRUCK-001", result.getTruckRoutes().get(2).getTruckName());
    }

    @Test
    void buildFullRouteForTrucks_whenFactoryThrowsNullPointerException_shouldWrapWithContext() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenThrow(new NullPointerException("Null coordinate"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        assertTrue(exception.getMessage().contains("Failed to create route for truck TRUCK-001"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }

    @Test
    void buildFullRouteForTrucks_withAllNullReturnsFromFactory_shouldReturnListOfNulls() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2));
        when(truckRouteFactory.createRouteForTruck(any(), any(), any())).thenReturn(null);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, warehouseId);

        assertNotNull(result);
        assertEquals(2, result.getTruckRoutes().size());
        assertNull(result.getTruckRoutes().get(0));
        assertNull(result.getTruckRoutes().get(1));
    }

    @Test
    void buildFullRouteForTrucks_withMinLongWarehouseId_shouldAcceptAndProcess() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        Long minWarehouseId = Long.MIN_VALUE;
        
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, minWarehouseId))
                .thenReturn(truckRouteInfo1);

        WarehouseRoutingResult result = truckRouteBuilder.buildFullRouteForTrucks(
                vrpRequest, mockAssignmentResponse, minWarehouseId);

        assertNotNull(result);
        assertEquals(minWarehouseId, result.getGeneratedForWarehouse());
    }

    @Test
    void buildFullRouteForTrucks_withExceptionInStreamProcessing_shouldPropagateCorrectly() {
        TruckAssignment.ParcelInfo parcel3 = new TruckAssignment.ParcelInfo("P003", 20.0);
        TruckAssignment truck3 = new TruckAssignment("TRUCK-003", List.of(parcel3), 20.0, 100.0);
        
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1, truckAssignment2, truck3));
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenReturn(truckRouteInfo1);
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment2, warehouseId))
                .thenThrow(new IllegalStateException("Invalid state"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        assertTrue(exception.getMessage().contains("Failed to create route for truck TRUCK-002"));
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment1, warehouseId);
        verify(truckRouteFactory, times(1)).createRouteForTruck(vrpRequest, truckAssignment2, warehouseId);
        verify(truckRouteFactory, never()).createRouteForTruck(vrpRequest, truck3, warehouseId);
    }

    @Test
    void buildFullRouteForTrucks_withSingleTruckAndExceptionInTryCatch_shouldPropagateOriginalException() {
        when(mockAssignmentResponse.getTruckAssignments()).thenReturn(List.of(truckAssignment1));
        RuntimeException originalException = new RuntimeException("Original error");
        when(truckRouteFactory.createRouteForTruck(vrpRequest, truckAssignment1, warehouseId))
                .thenThrow(originalException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> 
                truckRouteBuilder.buildFullRouteForTrucks(vrpRequest, mockAssignmentResponse, warehouseId));
        
        assertTrue(thrown.getMessage().contains("Failed to create route for truck"));
        assertEquals(originalException, thrown.getCause());
    }
}

