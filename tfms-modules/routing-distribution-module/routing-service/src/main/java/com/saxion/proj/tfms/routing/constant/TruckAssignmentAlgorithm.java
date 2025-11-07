package com.saxion.proj.tfms.routing.constant;

import lombok.Getter;

@Getter
public enum TruckAssignmentAlgorithm {
    BEST_FIT("Assign trucks to warehouses minimizing unused capacity"),
    ROUND_ROBIN("Assign trucks in a simple rotation"),
    NEAREST_WAREHOUSE("Assign trucks based on nearest warehouse");

    private final String description;

    TruckAssignmentAlgorithm(String description) {
        this.description = description;
    }
}
