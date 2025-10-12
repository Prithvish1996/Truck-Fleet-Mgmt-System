package com.saxion.proj.tfms.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for delivery optimization requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOptimizationRequest {
    
    @NotNull(message = "Warehouses data is required")
    @NotEmpty(message = "At least one warehouse must be provided")
    @Valid
    private List<WarehouseData> warehouses;
    
    @NotNull(message = "Trucks data is required")
    @NotEmpty(message = "At least one truck must be provided")
    @Valid
    private List<TruckData> trucks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseData {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("delivery_date")
        private String deliveryDate;
        
        @JsonProperty("packages")
        private List<PackageData> packages;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageData {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("weight")
        private Double weight;
        
        @JsonProperty("size")
        private Double size;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TruckData {
        @JsonProperty("truck_id")
        private String truckId;
        
        @JsonProperty("weight_limit")
        private Double weightLimit;
    }
}
