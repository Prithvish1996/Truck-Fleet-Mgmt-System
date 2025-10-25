package com.saxion.proj.tfms.commons.model;

import com.saxion.proj.tfms.commons.constants.AlgorithmType;
import com.saxion.proj.tfms.commons.constants.StatusEnum;
import com.saxion.proj.tfms.commons.dto.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "parcels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    // To be replace with pickup location
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WareHouseDao warehouse;

    // link to pickup location
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "pickup_location_id")
    private LocationDao pickupLocation;

    // link to delivery location
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "delivery_location_id")
    private LocationDao deliveryLocation;

    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @Column(columnDefinition = "TEXT")
    private String deliveryInstructions;

    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private boolean active = true;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
