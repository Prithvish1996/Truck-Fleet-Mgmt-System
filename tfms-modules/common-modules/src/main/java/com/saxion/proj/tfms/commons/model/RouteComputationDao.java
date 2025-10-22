package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.AlgorithmType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_computations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class RouteComputationDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Input Context ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private TruckDao truck;

    @Column(nullable = false)
    private Long plannerId; // user Id of the planner who ran the computation

    @ManyToMany
    @JoinTable(
            name = "route_computation_parcels",
            joinColumns = @JoinColumn(name = "computation_id"),
            inverseJoinColumns = @JoinColumn(name = "parcel_id")
    )
    private List<ParcelDao> parcelsUsed;

    // --- Algorithm Metadata ---
    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_type", nullable = false)
    private AlgorithmType algorithmType; // e.g., OR_TOOLS, CVRP, CUSTOM

    @Column(columnDefinition = "TEXT")
    private String algorithmParameters; // JSON or serialized details

    @Column(nullable = true)
    private ZonedDateTime computationStart;

    @Column(nullable = true)
    private ZonedDateTime computationEnd;

    @Column(nullable = true)
    private boolean success;

    private String failureReason;

    // --- Computation Results ---

    @Column(nullable = true)
    private double totalDistance;

    @Column(nullable = true)
    private double totalDuration;

    private double totalFuelCost;

    @Column(nullable = true)
    private int totalStops;

    @Column(columnDefinition = "TEXT")
    private String optimizedRouteOrder;
    // JSON array of parcel IDs or waypoints in computed order

    @Column(columnDefinition = "TEXT")
    private String routeGeometry;
    // e.g., encoded polyline or geojson path

    @Column(columnDefinition = "TEXT")
    private String notes; // planner notes, comments, etc.

    // Optional one-to-many for reverse lookup
    @OneToMany(mappedBy = "breaks", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteBreakDao> breaks = new ArrayList<>();

}