package com.saxion.proj.tfms.truck.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Truck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckDTO {
    
    private Long id;
    
    @NotBlank(message = "Truck identifier is required")
    @Size(max = 50, message = "Truck identifier must not exceed 50 characters")
    private String truckId;
    
    @NotNull(message = "Weight limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight limit must be greater than 0")
    private BigDecimal weightLimit;
    
    private Boolean isAvailable;
    
    // Additional computed fields for API responses
    private BigDecimal currentLoad;
    private BigDecimal remainingCapacity;
    
    // Constructor for creating DTOs from entities
    public TruckDTO(Long id, String truckId, BigDecimal weightLimit, Boolean isAvailable) {
        this.id = id;
        this.truckId = truckId;
        this.weightLimit = weightLimit;
        this.isAvailable = isAvailable;
    }
}
