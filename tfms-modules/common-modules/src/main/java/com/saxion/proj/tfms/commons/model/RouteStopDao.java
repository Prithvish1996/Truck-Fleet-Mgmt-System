package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class RouteStopDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id")
    private LocationDao location;

    // before/after package in the route sequence (optional)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "before_package_id")
    private ParcelDao beforePackage;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "after_package_id")
    private ParcelDao afterPackage;

    // The route this break belongs to (planned route)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "route_id")
    private RouteDao route;

    // If this break is part of a RouteComputation snapshot, link to it
//    @ManyToOne(fetch = FetchType.LAZY, optional = true)
//    @JoinColumn(name = "route_computation_id")
//    private RouteComputationDao routeComputation;
}
