package com.saxion.proj.tfms.truck.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "capacity_kg")
    private Double capacityKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckStatus status;

    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @Column(name = "next_maintenance")
    private LocalDateTime nextMaintenance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TruckStatus {
        AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE
    }

    // Constructors
    public Truck() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TruckStatus.AVAILABLE;
    }

    public Truck(String licensePlate, String make, String model) {
        this();
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getCapacityKg() { return capacityKg; }
    public void setCapacityKg(Double capacityKg) { this.capacityKg = capacityKg; }

    public TruckStatus getStatus() { return status; }
    public void setStatus(TruckStatus status) { this.status = status; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public LocalDateTime getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(LocalDateTime lastMaintenance) { this.lastMaintenance = lastMaintenance; }

    public LocalDateTime getNextMaintenance() { return nextMaintenance; }
    public void setNextMaintenance(LocalDateTime nextMaintenance) { this.nextMaintenance = nextMaintenance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
