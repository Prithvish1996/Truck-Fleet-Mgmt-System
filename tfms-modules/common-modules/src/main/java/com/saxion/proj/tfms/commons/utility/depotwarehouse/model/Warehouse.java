package com.saxion.proj.tfms.commons.utility.depotwarehouse.model;

public class Warehouse {
    public String id;
    public String name;
    public double lat;
    public double lon;

    public Warehouse(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
