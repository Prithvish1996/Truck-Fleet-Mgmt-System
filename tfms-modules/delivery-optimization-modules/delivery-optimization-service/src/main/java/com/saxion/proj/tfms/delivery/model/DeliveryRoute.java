package com.saxion.proj.tfms.delivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an optimized delivery route for a truck
 */
@Entity
@Table(name = "delivery_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Truck ID is required")
    @Column(name = "truck_id", nullable = false)
    private String truckId;
    
    @OneToMany(mappedBy = "deliveryRoute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryPackage> packages = new ArrayList<>();
    
    @Column(name = "total_distance", precision = 10, scale = 2)
    private BigDecimal totalDistance;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @Column(name = "route_sequence", columnDefinition = "TEXT")
    private String routeSequence; // JSON string representing the order of deliveries
    
    @Column(name = "is_optimized", nullable = false)
    private Boolean isOptimized = false;
    
    // Constructor for creating routes
    public DeliveryRoute(String truckId) {
        this.truckId = truckId;
        this.packages = new ArrayList<>();
        this.isOptimized = false;
    }
    
    // Helper method to calculate total weight of packages in route
    public BigDecimal getTotalWeight() {
        return packages.stream()
                .map(DeliveryPackage::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Helper method to check if route exceeds truck capacity
    public boolean exceedsCapacity(BigDecimal truckWeightLimit) {
        return getTotalWeight().compareTo(truckWeightLimit) > 0;
    }
    
    // Helper method to get package count
    public int getPackageCount() {
        return packages.size();
    }
    
    // Helper method to add package to route
    public void addPackage(DeliveryPackage packageItem) {
        if (packageItem != null) {
            packages.add(packageItem);
            packageItem.setDeliveryRoute(this);
        }
    }
    
    // Helper method to remove package from route
    public void removePackage(DeliveryPackage packageItem) {
        if (packageItem != null) {
            packages.remove(packageItem);
            packageItem.setDeliveryRoute(null);
        }
    }
}
