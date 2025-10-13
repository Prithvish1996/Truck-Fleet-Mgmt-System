package com.saxion.proj.tfms.truck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    // Constructor for creating trucks
    public Truck(String truckId, BigDecimal weightLimit) {
        this.truckId = truckId;
        this.weightLimit = weightLimit;
        this.isAvailable = true;
    }
    
    // Helper method to check if truck can carry additional weight
    public boolean canCarry(BigDecimal currentLoad, BigDecimal additionalWeight) {
        return currentLoad.add(additionalWeight).compareTo(weightLimit) <= 0;
    }
    
    // Helper method to get remaining capacity
    public BigDecimal getRemainingCapacity(BigDecimal currentLoad) {
        return weightLimit.subtract(currentLoad);
    }
}
