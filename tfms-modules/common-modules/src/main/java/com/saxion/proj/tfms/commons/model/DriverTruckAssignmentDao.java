package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "driver_truck_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DriverTruckAssignmentDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "driver_id")
    private DriverDao driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "truck_id")
    private TruckDao truck;

    @Column(nullable = false)
    private ZonedDateTime dateAssigned;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment-status", nullable = false)
    private StatusEnum assignmentStatus;
}
