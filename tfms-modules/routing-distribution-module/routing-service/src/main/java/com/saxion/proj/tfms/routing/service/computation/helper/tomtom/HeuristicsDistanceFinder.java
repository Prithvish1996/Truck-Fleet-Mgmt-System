package com.saxion.proj.tfms.routing.service.computation.helper.tomtom;

import com.saxion.proj.tfms.routing.model.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeuristicsDistanceFinder {

    private static final double EARTH_RADIUS = 6371.0;


    public static Map<Coordinates, Double> findStraightDistanceInkm(Coordinates mainCoordinate, List<Coordinates> subCoordinates) {
        Map<Coordinates, Double> distances = new HashMap<>();

        for (Coordinates sub : subCoordinates) {
            distances.put(sub, haversineDistance(mainCoordinate, sub));
        }
        return distances;
    }

    private static double haversineDistance(Coordinates c1, Coordinates c2) {
        final int R = 6371; // Radius of Earth in km
        double latDistance = Math.toRadians(c2.getLatitude() - c1.getLatitude());
        double lonDistance = Math.toRadians(c2.getLongitude() - c1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(c1.getLatitude())) * Math.cos(Math.toRadians(c2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


}
