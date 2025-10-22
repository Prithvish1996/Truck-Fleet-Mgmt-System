package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class RouteBreakDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationDao location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_package_id", nullable = false)
    private ParcelDao beforePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_package_id", nullable = false)
    private ParcelDao afterPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteDao route;
}
