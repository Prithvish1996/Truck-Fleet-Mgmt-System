package com.saxion.proj.tfms.delivery.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a warehouse containing packages
 */
@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Warehouse name is required")
    @Size(max = 255, message = "Warehouse name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;
    
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
    
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryPackage> packages = new ArrayList<>();
    
    @JsonProperty("delivery_date")
    @Column(name = "delivery_date")
    private String deliveryDate; // ISO date string
    
    // Constructor for JSON/CSV parsing
    public Warehouse(String name, BigDecimal latitude, BigDecimal longitude, 
                    String deliveryDate, List<DeliveryPackage> packages) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryDate = deliveryDate;
        this.packages = packages != null ? packages : new ArrayList<>();
    }
    
    // Helper method to calculate total weight of all packages
    public BigDecimal getTotalWeight() {
        return packages.stream()
                .map(DeliveryPackage::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Helper method to get package count
    public int getPackageCount() {
        return packages.size();
    }
}
