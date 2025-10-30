package com.saxion.proj.tfms.routing.vrp;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import com.saxion.proj.tfms.routing.dto.VrpResponseDto;
import com.saxion.proj.tfms.routing.service.DistanceMatrixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Solves VRP using Google OR-Tools constraint solver.
 * Single Responsibility Principle: Manages OR-Tools model building and solving
 */
@Component
public class OrToolsSolver {

    static {
        Loader.loadNativeLibraries();
    }

    @Autowired
    private CostMatrixBuilder costMatrixBuilder;

    @Autowired
    private ConstraintHandler constraintHandler;

    @Autowired
    private SolutionParser solutionParser;

    public VrpResponseDto solve(VrpRequestDto request, LocationMapper.LocationMapping locationMapping,
                                DistanceMatrixService.DistanceMatrix distanceMatrix) {
        
        int numLocations = locationMapping.getLocations().size();
        int numVehicles = request.getTrucks().size();
        int depotIndex = locationMapping.getDepotIndex();

        System.out.println("\n🔧 Building OR-Tools model...");

        RoutingIndexManager manager = new RoutingIndexManager(numLocations, numVehicles, depotIndex);
        RoutingModel routing = new RoutingModel(manager);

        final long[][] costMatrix = costMatrixBuilder.buildCostMatrix(request, distanceMatrix);

        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return costMatrix[fromNode][toNode];
        });

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        constraintHandler.addCapacityConstraints(routing, manager, request, locationMapping);
        constraintHandler.addPickupDeliveryConstraints(routing, manager, request, locationMapping);

        RoutingSearchParameters searchParameters = buildSearchParameters();


        Assignment solution = routing.solveWithParameters(searchParameters);

        if (solution == null) {
            throw new RuntimeException("OR-Tools could not find a solution. Try relaxing constraints.");
        }

        return solutionParser.parseSolution(solution, routing, manager, request, locationMapping);
    }

    private RoutingSearchParameters buildSearchParameters() {
        return main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(30).build())
                .build();
    }
}
