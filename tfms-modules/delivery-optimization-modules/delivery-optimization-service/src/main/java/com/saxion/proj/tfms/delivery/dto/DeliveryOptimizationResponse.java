package com.saxion.proj.tfms.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for delivery optimization responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOptimizationResponse {
    
    @JsonProperty("optimization_id")
    private String optimizationId;
    
    @JsonProperty("total_routes")
    private Integer totalRoutes;
    
    @JsonProperty("total_packages")
    private Integer totalPackages;
    
    @JsonProperty("total_distance")
    private BigDecimal totalDistance;
    
    @JsonProperty("optimization_status")
    private String optimizationStatus;
    
    @JsonProperty("routes")
    private List<RouteInfo> routes;
    
    @JsonProperty("unassigned_packages")
    private List<PackageInfo> unassignedPackages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteInfo {
        @JsonProperty("route_id")
        private Long routeId;
        
        @JsonProperty("truck_id")
        private String truckId;
        
        @JsonProperty("total_weight")
        private BigDecimal totalWeight;
        
        @JsonProperty("package_count")
        private Integer packageCount;
        
        @JsonProperty("estimated_distance")
        private BigDecimal estimatedDistance;
        
        @JsonProperty("estimated_duration_minutes")
        private Integer estimatedDurationMinutes;
        
        @JsonProperty("packages")
        private List<PackageInfo> packages;
        
        @JsonProperty("route_sequence")
        private List<Coordinate> routeSequence;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageInfo {
        @JsonProperty("package_id")
        private Long packageId;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("weight")
        private BigDecimal weight;
        
        @JsonProperty("latitude")
        private BigDecimal latitude;
        
        @JsonProperty("longitude")
        private BigDecimal longitude;
        
        @JsonProperty("delivery_date")
        private String deliveryDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        @JsonProperty("latitude")
        private BigDecimal latitude;
        
        @JsonProperty("longitude")
        private BigDecimal longitude;
        
        @JsonProperty("package_name")
        private String packageName;
    }
}
