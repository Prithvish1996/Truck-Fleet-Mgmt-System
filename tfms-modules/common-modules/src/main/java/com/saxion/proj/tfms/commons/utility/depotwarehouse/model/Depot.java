package com.saxion.proj.tfms.commons.utility.depotwarehouse.model;

import java.util.ArrayList;
import java.util.List;

public class Depot {
    public String id;
    public String name;
    public double lat;
    public double lon;
    public List<Truck> trucks = new ArrayList<>();

    public Depot(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
