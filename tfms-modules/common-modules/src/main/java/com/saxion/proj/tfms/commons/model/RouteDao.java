package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "truck_id")
    private TruckDao truck;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "driver_id")
    private DriverDao driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "depot_id")
    private DepotDao depot;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "warehouse_id")
    private WareHouseDao warehouse;

    @Column(nullable = false)
    private Long totalDistance;

    @Column(nullable = false)
    private Long totalTransportTime;

    @Column(nullable = false)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @Column(nullable = false)
    private ZonedDateTime startTime;

    @Column(nullable = false)
    private ZonedDateTime scheduleDate;

    @Column(nullable = false)
    private String duration;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteStopDao> stops = new ArrayList<>();
}
