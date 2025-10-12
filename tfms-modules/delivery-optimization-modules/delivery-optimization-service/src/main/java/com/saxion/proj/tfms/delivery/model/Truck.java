package com.saxion.proj.tfms.delivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a truck with capacity constraints
 */
@Entity
@Table(name = "trucks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Truck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Truck identifier is required")
    @Size(max = 50, message = "Truck identifier must not exceed 50 characters")
    @Column(name = "truck_id", nullable = false, unique = true)
    private String truckId;
    
    @NotNull(message = "Weight limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight limit must be greater than 0")
    @Column(name = "weight_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightLimit;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryRoute> deliveryRoutes = new ArrayList<>();
    
    // Constructor for creating trucks
    public Truck(String truckId, BigDecimal weightLimit) {
        this.truckId = truckId;
        this.weightLimit = weightLimit;
        this.isAvailable = true;
    }
    
    // Helper method to calculate current load
    public BigDecimal getCurrentLoad() {
        return deliveryRoutes.stream()
                .flatMap(route -> route.getPackages().stream())
                .map(DeliveryPackage::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Helper method to check if truck can carry additional weight
    public boolean canCarry(BigDecimal additionalWeight) {
        return getCurrentLoad().add(additionalWeight).compareTo(weightLimit) <= 0;
    }
    
    // Helper method to get remaining capacity
    public BigDecimal getRemainingCapacity() {
        return weightLimit.subtract(getCurrentLoad());
    }
}
