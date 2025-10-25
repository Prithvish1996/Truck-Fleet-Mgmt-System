package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.TruckType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "trucks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class TruckDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "truck_type", nullable = false)
    private TruckType type;

    @Column(nullable = false)
    private String make;

    private LocalDate lastServiceDate;

    private String lastServicedBy;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverTruckAssignmentDao> assignments;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceRecordDao> maintenances;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteDao> routes;
}
