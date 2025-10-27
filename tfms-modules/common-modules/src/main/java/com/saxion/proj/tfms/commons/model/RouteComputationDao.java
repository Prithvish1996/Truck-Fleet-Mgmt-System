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
    @EqualsAndHashCode.Include
    private Long id;

    // --- Algorithm Metadata ---
    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_type", nullable = false)
    private AlgorithmType algorithmType;

    @Column(columnDefinition = "TEXT")
    private String algorithmParameters;

    private ZonedDateTime computationStart;
    private ZonedDateTime computationEnd;

    private boolean success;
    private String failureReason;

    // --- Computation Results ---
    private double totalDistance;
    private double totalDuration;
    private double totalFuelCost;
    private int totalStops;

    @Column(columnDefinition = "TEXT")
    private String optimizedRouteOrder;

    @Column(columnDefinition = "TEXT")
    private String routeGeometry;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // The route this break belongs to (planned route)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "route_id")
    private RouteDao route;
}