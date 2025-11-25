package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import com.saxion.proj.tfms.routing.service.assignment.TruckAssignmentService;
import com.saxion.proj.tfms.routing.service.assignment.helper.truckassignment.response.AssignmentResponse;
import com.saxion.proj.tfms.routing.service.computation.TruckRouteBuilder;
import com.saxion.proj.tfms.routing.service.output.VrpResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingOptimizerImplTest {

    @Mock
    private TruckAssignmentService truckAssignmentService;

    @Mock
    private TruckRouteBuilder truckRouteBuilder;

    @Mock
    private VrpResponseBuilderService vrpResponseBuilderService;

    @InjectMocks
    private RoutingOptimizerImpl routingOptimizer;

    private VRPRequest request;
    private VRPResponse expectedResponse;
    private Map<Long, AssignmentResponse> assignments;
    private AssignmentResponse assignmentResponse1;
    private AssignmentResponse assignmentResponse2;
    private WarehouseRoutingResult warehouseRoutingResult1;
    private WarehouseRoutingResult warehouseRoutingResult2;

    @BeforeEach
    void setUp() {
        request = mock(VRPRequest.class);
        expectedResponse = mock(VRPResponse.class);
        assignmentResponse1 = mock(AssignmentResponse.class);
        assignmentResponse2 = mock(AssignmentResponse.class);
        warehouseRoutingResult1 = mock(WarehouseRoutingResult.class);
        warehouseRoutingResult2 = mock(WarehouseRoutingResult.class);
        assignments = new HashMap<>();
    }

    @Test
    void testOptimize_WithEmptyAssignments_ReturnsResponseWithEmptyResults() {
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(vrpResponseBuilderService.buildResponse(argThat(list -> list.isEmpty()))).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.isEmpty()));
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
    }

    @Test
    void testOptimize_WithNullAssignments_ReturnsResponseWithEmptyResults() {
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(null);
        when(vrpResponseBuilderService.buildResponse(argThat(list -> list.isEmpty()))).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.isEmpty()));
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
    }

    @Test
    void testOptimize_WithSingleWarehouse_ProcessesOneAssignment() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenReturn(warehouseRoutingResult1);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> 
            list.size() == 1 && list.contains(warehouseRoutingResult1)
        ));
    }

    @Test
    void testOptimize_WithNullAssignmentResponse_SkipsWarehouse() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        assignments.put(warehouseId1, null);
        assignments.put(warehouseId2, assignmentResponse2);
        when(assignmentResponse2.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2))
                .thenReturn(warehouseRoutingResult2);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(eq(request), eq(null), eq(warehouseId1));
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.size() == 1));
    }

    @Test
    void testOptimize_WithSuccessfulAssignmentResponse_ProcessesWarehouse() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenReturn(warehouseRoutingResult1);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.size() == 1));
    }

    @Test
    void testOptimize_WithFailedAssignmentResponse_SkipsWarehouse() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(false);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.isEmpty()));
    }

    @Test
    void testOptimize_WithMultipleWarehouses_ProcessesAllAssignments() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, assignmentResponse2);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        when(assignmentResponse2.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1))
                .thenReturn(warehouseRoutingResult1);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2))
                .thenReturn(warehouseRoutingResult2);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> 
            list.size() == 2 && list.contains(warehouseRoutingResult1) && list.contains(warehouseRoutingResult2)
        ));
    }

    @Test
    void testOptimize_WithMixedSuccessAndFailedAssignments_ProcessesOnlySuccessful() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        Long warehouseId3 = 3L;
        AssignmentResponse failedResponse = mock(AssignmentResponse.class);
        when(failedResponse.isSuccess()).thenReturn(false);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        when(assignmentResponse2.isSuccess()).thenReturn(true);

        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, failedResponse);
        assignments.put(warehouseId3, assignmentResponse2);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1))
                .thenReturn(warehouseRoutingResult1);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse2, warehouseId3))
                .thenReturn(warehouseRoutingResult2);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(request, failedResponse, warehouseId2);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse2, warehouseId3);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.size() == 2));
    }

    @Test
    void testOptimize_WhenAssignmentServiceThrowsException_PropagatesException() {
        RuntimeException exception = new RuntimeException("Assignment failed");
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> routingOptimizer.optimize(request));

        assertEquals("Route optimization failed", thrown.getMessage());
        assertEquals(exception, thrown.getCause());
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
        verify(vrpResponseBuilderService, never()).buildResponse(any());
    }

    @Test
    void testOptimize_WhenRouteBuilderThrowsException_PropagatesException() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        RuntimeException exception = new RuntimeException("Route building failed");
        
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> routingOptimizer.optimize(request));

        assertEquals("Route optimization failed", thrown.getMessage());
        assertNotNull(thrown.getCause());
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId);
        verify(vrpResponseBuilderService, never()).buildResponse(any());
    }

    @Test
    void testOptimize_WhenResponseBuilderThrowsException_PropagatesException() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        RuntimeException exception = new RuntimeException("Response building failed");
        
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenReturn(warehouseRoutingResult1);
        when(vrpResponseBuilderService.buildResponse(any())).thenThrow(exception);

        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> routingOptimizer.optimize(request));

        assertEquals("Route optimization failed", thrown.getMessage());
        assertEquals(exception, thrown.getCause());
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId);
        verify(vrpResponseBuilderService).buildResponse(any());
    }

    @Test
    void testOptimize_WithThreeWarehouses_ProcessesAllInOrder() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        Long warehouseId3 = 3L;
        AssignmentResponse assignmentResponse3 = mock(AssignmentResponse.class);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        when(assignmentResponse2.isSuccess()).thenReturn(true);
        when(assignmentResponse3.isSuccess()).thenReturn(true);
        WarehouseRoutingResult warehouseRoutingResult3 = mock(WarehouseRoutingResult.class);
        
        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, assignmentResponse2);
        assignments.put(warehouseId3, assignmentResponse3);
        
        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1))
                .thenReturn(warehouseRoutingResult1);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2))
                .thenReturn(warehouseRoutingResult2);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse3, warehouseId3))
                .thenReturn(warehouseRoutingResult3);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(truckAssignmentService).assignTrucksPerWarehouse(request);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse3, warehouseId3);
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.size() == 3));
    }

    @Test
    void testOptimize_VerifiesCorrectWarehouseIdsPassed() {
        Long warehouseId1 = 100L;
        Long warehouseId2 = 200L;
        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, assignmentResponse2);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        when(assignmentResponse2.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(eq(request), eq(assignmentResponse1), eq(warehouseId1)))
                .thenReturn(warehouseRoutingResult1);
        when(truckRouteBuilder.buildFullRouteForTrucks(eq(request), eq(assignmentResponse2), eq(warehouseId2)))
                .thenReturn(warehouseRoutingResult2);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1);
        verify(truckRouteBuilder).buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2);
    }

    @Test
    void testOptimize_VerifiesCorrectAssignmentResponsesPassed() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenReturn(warehouseRoutingResult1);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        routingOptimizer.optimize(request);

        verify(truckRouteBuilder).buildFullRouteForTrucks(eq(request), eq(assignmentResponse1), eq(warehouseId));
    }

    @Test
    void testOptimize_VerifiesRequestPassedToAllServices() {
        Long warehouseId = 1L;
        assignments.put(warehouseId, assignmentResponse1);
        when(assignmentResponse1.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId))
                .thenReturn(warehouseRoutingResult1);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        routingOptimizer.optimize(request);

        verify(truckAssignmentService).assignTrucksPerWarehouse(same(request));
        verify(truckRouteBuilder).buildFullRouteForTrucks(same(request), any(), any());
    }

    @Test
    void testOptimize_WhenPartialProcessing_StopsAtFirstException() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, assignmentResponse2);
        when(assignmentResponse1.isSuccess()).thenReturn(true);
        when(assignmentResponse2.isSuccess()).thenReturn(true);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse1, warehouseId1))
                .thenReturn(warehouseRoutingResult1);
        when(truckRouteBuilder.buildFullRouteForTrucks(request, assignmentResponse2, warehouseId2))
                .thenThrow(new RuntimeException("Warehouse 2 failed"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> routingOptimizer.optimize(request));

        assertEquals("Route optimization failed", thrown.getMessage());
        assertNotNull(thrown.getCause());
        verify(vrpResponseBuilderService, never()).buildResponse(any());
    }

    @Test
    void testOptimize_AllWarehousesWithNullResponses_ReturnsEmptyResult() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        assignments.put(warehouseId1, null);
        assignments.put(warehouseId2, null);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.isEmpty()));
    }

    @Test
    void testOptimize_AllWarehousesWithFailedResponses_ReturnsEmptyResult() {
        Long warehouseId1 = 1L;
        Long warehouseId2 = 2L;
        when(assignmentResponse1.isSuccess()).thenReturn(false);
        when(assignmentResponse2.isSuccess()).thenReturn(false);
        assignments.put(warehouseId1, assignmentResponse1);
        assignments.put(warehouseId2, assignmentResponse2);

        when(truckAssignmentService.assignTrucksPerWarehouse(request)).thenReturn(assignments);
        when(vrpResponseBuilderService.buildResponse(any())).thenReturn(expectedResponse);

        VRPResponse result = routingOptimizer.optimize(request);

        assertNotNull(result);
        verify(truckRouteBuilder, never()).buildFullRouteForTrucks(any(), any(), any());
        verify(vrpResponseBuilderService).buildResponse(argThat(list -> list.isEmpty()));
    }
}

