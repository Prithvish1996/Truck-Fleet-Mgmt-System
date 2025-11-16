package com.saxion.proj.tfms.routing.model;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RouteCoordinatesGroup {
    private Coordinates depot;
    private Coordinates warehouse;
    private List<Coordinates> parcels;
    private List<Parcel> parcelList; // Full parcel objects for creating stops
}
