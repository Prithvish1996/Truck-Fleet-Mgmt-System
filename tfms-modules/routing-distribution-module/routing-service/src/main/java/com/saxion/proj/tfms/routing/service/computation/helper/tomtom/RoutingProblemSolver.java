package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.routing.model.ClusterResult;
import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;
import com.saxion.proj.tfms.routing.service.computation.helper.RoutingProvider;
import com.saxion.proj.tfms.routing.service.computation.helper.constants.Patterns;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("RoutingProblemSolver")
public class RoutingProblemSolver implements RoutingProvider {
    @Override
    public List<Stop> calculateRoute(RouteCoordinatesGroup routeCoordinatesGroup) {

        Coordinates depotCoordinate = routeCoordinatesGroup.getDepot();
        Coordinates warehouseCoordinates = routeCoordinatesGroup.getWarehouse();
        List<Coordinates> parcels = routeCoordinatesGroup.getParcels();


        // for warehouse to all parcels
        Map<Coordinates, Double> coordinatesDistanceeMap = HeuristicsDistanceFinder.findStraightDistanceInkm(warehouseCoordinates, parcels);

        ClusterResult clusters =
                HeuristicsClusterMaker.clusterByShift(
                        coordinatesDistanceeMap,
                        warehouseCoordinates,
                        Patterns.PATTERN,
                        8,
                        40
                );






        return null; // Implement TomTom's routing API call here and return the list of stops'
    }
}




