package com.saxion.proj.tfms.routing.model.builder;

import com.saxion.proj.tfms.routing.model.Coordinates;
import com.saxion.proj.tfms.routing.model.Parcel;
import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for RouteCoordinatesGroup.
 * Improves composability and readability of coordinate preparation.
 */
public class RouteCoordinatesGroupBuilder {
    
    private Coordinates depot;
    private Coordinates warehouse;
    private List<Coordinates> parcels = new ArrayList<>();
    private List<Parcel> parcelList = new ArrayList<>();

    public RouteCoordinatesGroupBuilder withDepot(Coordinates depot) {
        this.depot = Objects.requireNonNull(depot, "Depot cannot be null");
        return this;
    }

    public RouteCoordinatesGroupBuilder withWarehouse(Coordinates warehouse) {
        this.warehouse = Objects.requireNonNull(warehouse, "Warehouse cannot be null");
        return this;
    }

    public RouteCoordinatesGroupBuilder withParcels(List<Coordinates> parcels) {
        this.parcels = new ArrayList<>(parcels);
        return this;
    }

    public RouteCoordinatesGroupBuilder withParcelList(List<Parcel> parcelList) {
        this.parcelList = new ArrayList<>(parcelList);
        return this;
    }

    public RouteCoordinatesGroupBuilder addParcel(Coordinates coordinate) {
        this.parcels.add(Objects.requireNonNull(coordinate, "Coordinate cannot be null"));
        return this;
    }

    public RouteCoordinatesGroupBuilder addParcelObject(Parcel parcel) {
        this.parcelList.add(Objects.requireNonNull(parcel, "Parcel cannot be null"));
        return this;
    }

    public RouteCoordinatesGroup build() {
        Objects.requireNonNull(depot, "Depot is required");
        Objects.requireNonNull(warehouse, "Warehouse is required");
        
        if (parcels.isEmpty()) {
            throw new IllegalArgumentException("At least one parcel coordinate is required");
        }

        return RouteCoordinatesGroup.builder()
                .depot(depot)
                .warehouse(warehouse)
                .parcels(parcels)
                .parcelList(parcelList)
                .build();
    }
}
