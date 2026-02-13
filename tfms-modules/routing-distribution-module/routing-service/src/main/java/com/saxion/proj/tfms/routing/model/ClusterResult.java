package com.saxion.proj.tfms.routing.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Holds clustering result for shift scheduling.
 */
public class ClusterResult {
    @Getter
    private final Map<Integer, List<Coordinates>> shiftClusters;
    @Getter
    private final List<Coordinates> undeliveredParcels;

    public ClusterResult(Map<Integer, List<Coordinates>> shiftClusters, List<Coordinates> undeliveredParcels) {
        this.shiftClusters = shiftClusters;
        this.undeliveredParcels = undeliveredParcels;
    }

}
