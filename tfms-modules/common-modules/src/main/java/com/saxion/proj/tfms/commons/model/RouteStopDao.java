package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.constants.StopType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private int priority;

    private String duration;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "location_id")
    private LocationDao location;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false)
    private StopType stopType;

    // The route this break belongs to (planned route)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private RouteDao route;

    @OneToMany(mappedBy = "stop", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ParcelDao> parcels = new ArrayList<>();
}
