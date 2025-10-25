package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class LocationDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postalCode;

    // reverse relations
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DriverDao> drivers = new ArrayList<>();

    @OneToMany(mappedBy = "pickupLocation", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ParcelDao> pickupParcels = new ArrayList<>();

    @OneToMany(mappedBy = "deliveryLocation", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ParcelDao> deliveryParcels = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<RouteStopDao> routeStops = new ArrayList<>();

    @OneToMany(mappedBy = "startWarehouse", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<RouteDao> routesStart = new ArrayList<>();
}
