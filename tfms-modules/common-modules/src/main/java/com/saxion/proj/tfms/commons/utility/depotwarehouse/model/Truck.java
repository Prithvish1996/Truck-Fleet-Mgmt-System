package com.saxion.proj.tfms.commons.utility.depotwarehouse.model;


public class Truck {
    public String id;
    public boolean assigned = false;
    public Warehouse currentLocation;

    public Truck(String id) {
        this.id = id;
    }
}

