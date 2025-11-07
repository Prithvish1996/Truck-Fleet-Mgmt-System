package com.saxion.proj.tfms.routing.constant;

public enum StopType {

    DEPOT("Depot - starting/ending point of the truck"),
    WAREHOUSE("Warehouse - pickup point for parcels"),
    CUSTOMER("Customer - delivery point");

    private final String description;

    StopType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
