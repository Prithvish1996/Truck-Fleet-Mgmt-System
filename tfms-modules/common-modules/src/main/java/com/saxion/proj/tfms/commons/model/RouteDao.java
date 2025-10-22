package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class RouteDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private TruckDao truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DriverDao driver;

    @Column(nullable = false)
    private ZonedDateTime startTime;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private ZonedDateTime scheduleDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private double totalDistance;

    @Column(nullable = false)
    private double estimatedFuelCost;

    @Column(nullable = false)
    private String priority;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParcelDao> parcels;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteBreakDao> breaks;
}
