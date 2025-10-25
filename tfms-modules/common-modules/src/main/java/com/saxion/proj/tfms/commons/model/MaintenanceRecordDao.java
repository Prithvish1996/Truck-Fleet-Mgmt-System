package com.saxion.proj.tfms.commons.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class MaintenanceRecordDao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // The user (mechanic) who performed the maintenance
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serviced_by_user_id", nullable = false)
    private UserDao servicedBy;

    @Column(nullable = false)
    private LocalDateTime serviceDate;

    @Column(nullable = false, length = 255)
    private String serviceType;  // e.g., "Oil Change", "Tire Replacement"

    @Column(columnDefinition = "TEXT")
    private String notes;  // Optional remarks

    @Column(nullable = true)
    private Double cost;  // Optional maintenance cost

    // Many maintenance record belongs to one truck
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private TruckDao truck;

    // Many maintenance record belongs to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDao user;
}

