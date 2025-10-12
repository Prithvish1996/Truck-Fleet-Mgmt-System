package com.saxion.proj.tfms.delivery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a package to be delivered
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Package name is required")
    @Size(max = 255, message = "Package name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;
    
    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Size must be greater than 0")
    @Column(name = "size", nullable = false, precision = 10, scale = 2)
    private BigDecimal size;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_route_id")
    private DeliveryRoute deliveryRoute;
    
    @JsonProperty("delivery_date")
    @Column(name = "delivery_date")
    private String deliveryDate; // ISO date string
    
    // Constructor for JSON/CSV parsing
    public DeliveryPackage(String name, BigDecimal weight, BigDecimal size, 
                   BigDecimal latitude, BigDecimal longitude, String deliveryDate) {
        this.name = name;
        this.weight = weight;
        this.size = size;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryDate = deliveryDate;
    }
}
