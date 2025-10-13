package com.saxion.proj.tfms.delivery.controller;

import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationRequest;
import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationResponse;
import com.saxion.proj.tfms.truck.model.Truck;
import com.saxion.proj.tfms.delivery.service.DeliveryOptimizationService;
// Swagger annotations removed for simplicity
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for delivery optimization endpoints
 */
@RestController
@RequestMapping("/api/delivery-optimization")
@RequiredArgsConstructor
@Slf4j
// Simplified controller without Swagger annotations
public class DeliveryOptimizationController {
    
    private final DeliveryOptimizationService deliveryOptimizationService;
    
    /**
     * Optimize delivery routes from JSON data
     */
    @PostMapping(value = "/optimize/json", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    // Optimize delivery routes from JSON data
    public ResponseEntity<DeliveryOptimizationResponse> optimizeFromJson(
            @Valid @RequestBody DeliveryOptimizationRequest request) {
        
        log.info("Received JSON optimization request with {} warehouses and {} trucks", 
                request.getWarehouses().size(), request.getTrucks().size());
        
        try {
            DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromJson(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing JSON optimization request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Optimize delivery routes from CSV file upload
     */
    @PostMapping(value = "/optimize/csv", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    // Optimize delivery routes from CSV file
    public ResponseEntity<DeliveryOptimizationResponse> optimizeFromCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "trucks", required = false) String trucksJson) {
        
        log.info("Received CSV optimization request for file: {}", file.getOriginalFilename());
        
        try {
            // Read CSV content
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            // Parse trucks if provided
            List<Truck> trucks = parseTrucksFromJson(trucksJson);
            
            DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromCsv(csvContent, trucks);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing CSV optimization request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Optimize delivery routes from CSV content (text)
     */
    @PostMapping(value = "/optimize/csv-content", 
                 consumes = MediaType.TEXT_PLAIN_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    // Optimize delivery routes from CSV content
    public ResponseEntity<DeliveryOptimizationResponse> optimizeFromCsvContent(
            @RequestBody String csvContent,
            @RequestParam(value = "trucks", required = false) String trucksJson) {
        
        log.info("Received CSV content optimization request");
        
        try {
            List<Truck> trucks = parseTrucksFromJson(trucksJson);
            DeliveryOptimizationResponse response = deliveryOptimizationService.optimizeFromCsv(csvContent, trucks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing CSV content optimization request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get optimization status and health check
     */
    @GetMapping("/health")
    // Health check for delivery optimization service
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Delivery Optimization Service is running");
    }
    
    /**
     * Parse trucks from JSON string
     */
    private List<Truck> parseTrucksFromJson(String trucksJson) {
        if (trucksJson == null || trucksJson.trim().isEmpty()) {
            // Return default trucks if none provided
            return createDefaultTrucks();
        }
        
        try {
            // Simple JSON parsing for trucks
            // This is a simplified implementation - in production, use proper JSON parsing
            List<Truck> trucks = new ArrayList<>();
            // Add JSON parsing logic here if needed
            return trucks.isEmpty() ? createDefaultTrucks() : trucks;
        } catch (Exception e) {
            log.warn("Error parsing trucks JSON, using default trucks", e);
            return createDefaultTrucks();
        }
    }
    
    /**
     * Create default trucks for testing
     */
    private List<Truck> createDefaultTrucks() {
        List<Truck> defaultTrucks = new ArrayList<>();
        defaultTrucks.add(new Truck("TRUCK-001", new BigDecimal("5000.0")));
        defaultTrucks.add(new Truck("TRUCK-002", new BigDecimal("7500.0")));
        defaultTrucks.add(new Truck("TRUCK-003", new BigDecimal("10000.0")));
        return defaultTrucks;
    }
}
