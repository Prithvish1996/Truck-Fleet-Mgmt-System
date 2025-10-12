package com.saxion.proj.tfms.delivery.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.saxion.proj.tfms.delivery.dto.DeliveryOptimizationRequest;
import com.saxion.proj.tfms.delivery.model.DeliveryPackage;
import com.saxion.proj.tfms.delivery.model.Truck;
import com.saxion.proj.tfms.delivery.model.Warehouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing JSON and CSV data into domain models
 */
@Service
@Slf4j
public class DataParsingService {
    
    /**
     * Parse JSON data into domain models
     */
    public ParsedData parseJsonData(DeliveryOptimizationRequest request) {
        log.info("Parsing JSON data with {} warehouses and {} trucks", 
                request.getWarehouses().size(), request.getTrucks().size());
        
        List<Warehouse> warehouses = new ArrayList<>();
        List<Truck> trucks = new ArrayList<>();
        
        // Parse warehouses and packages
        for (DeliveryOptimizationRequest.WarehouseData warehouseData : request.getWarehouses()) {
            Warehouse warehouse = new Warehouse();
            warehouse.setName(warehouseData.getName());
            warehouse.setLatitude(BigDecimal.valueOf(warehouseData.getLatitude()));
            warehouse.setLongitude(BigDecimal.valueOf(warehouseData.getLongitude()));
            warehouse.setDeliveryDate(warehouseData.getDeliveryDate());
            
            List<DeliveryPackage> packages = new ArrayList<>();
            if (warehouseData.getPackages() != null) {
                for (DeliveryOptimizationRequest.PackageData packageData : warehouseData.getPackages()) {
                    DeliveryPackage packageItem = new DeliveryPackage(
                            packageData.getName(),
                            BigDecimal.valueOf(packageData.getWeight()),
                            BigDecimal.valueOf(packageData.getSize()),
                            BigDecimal.valueOf(packageData.getLatitude()),
                            BigDecimal.valueOf(packageData.getLongitude()),
                            warehouseData.getDeliveryDate()
                    );
                    packageItem.setWarehouse(warehouse);
                    packages.add(packageItem);
                }
            }
            warehouse.setPackages(packages);
            warehouses.add(warehouse);
        }
        
        // Parse trucks
        for (DeliveryOptimizationRequest.TruckData truckData : request.getTrucks()) {
            Truck truck = new Truck(
                    truckData.getTruckId(),
                    BigDecimal.valueOf(truckData.getWeightLimit())
            );
            trucks.add(truck);
        }
        
        log.info("Successfully parsed {} warehouses with {} total packages and {} trucks", 
                warehouses.size(), 
                warehouses.stream().mapToInt(Warehouse::getPackageCount).sum(),
                trucks.size());
        
        return new ParsedData(warehouses, trucks);
    }
    
    /**
     * Parse CSV data into domain models
     * Expected CSV format: warehouse_name,warehouse_lat,warehouse_lng,delivery_date,package_name,package_weight,package_size,package_lat,package_lng
     */
    public ParsedData parseCsvData(String csvContent) throws IOException, CsvException {
        log.info("Parsing CSV data");
        
        List<Warehouse> warehouses = new ArrayList<>();
        List<Truck> trucks = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> rows = reader.readAll();
            
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }
            
            // Skip header row if present
            int startRow = 0;
            if (rows.get(0)[0].toLowerCase().contains("warehouse")) {
                startRow = 1;
            }
            
            for (int i = startRow; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 9) {
                    log.warn("Skipping row {} - insufficient columns", i + 1);
                    continue;
                }
                
                try {
                    String warehouseName = row[0].trim();
                    BigDecimal warehouseLat = new BigDecimal(row[1].trim());
                    BigDecimal warehouseLng = new BigDecimal(row[2].trim());
                    String deliveryDate = row[3].trim();
                    String packageName = row[4].trim();
                    BigDecimal packageWeight = new BigDecimal(row[5].trim());
                    BigDecimal packageSize = new BigDecimal(row[6].trim());
                    BigDecimal packageLat = new BigDecimal(row[7].trim());
                    BigDecimal packageLng = new BigDecimal(row[8].trim());
                    
                    // Find or create warehouse
                    Warehouse warehouse = warehouses.stream()
                            .filter(w -> w.getName().equals(warehouseName))
                            .findFirst()
                            .orElse(null);
                    
                    if (warehouse == null) {
                        warehouse = new Warehouse();
                        warehouse.setName(warehouseName);
                        warehouse.setLatitude(warehouseLat);
                        warehouse.setLongitude(warehouseLng);
                        warehouse.setDeliveryDate(deliveryDate);
                        warehouse.setPackages(new ArrayList<>());
                        warehouses.add(warehouse);
                    }
                    
                    // Create package
                    DeliveryPackage packageItem = new DeliveryPackage(
                            packageName,
                            packageWeight,
                            packageSize,
                            packageLat,
                            packageLng,
                            deliveryDate
                    );
                    packageItem.setWarehouse(warehouse);
                    warehouse.getPackages().add(packageItem);
                    
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", i + 1, e.getMessage());
                }
            }
        }
        
        log.info("Successfully parsed {} warehouses with {} total packages from CSV", 
                warehouses.size(), 
                warehouses.stream().mapToInt(Warehouse::getPackageCount).sum());
        
        return new ParsedData(warehouses, trucks);
    }
    
    /**
     * Data container for parsed results
     */
    public static class ParsedData {
        private final List<Warehouse> warehouses;
        private final List<Truck> trucks;
        
        public ParsedData(List<Warehouse> warehouses, List<Truck> trucks) {
            this.warehouses = warehouses;
            this.trucks = trucks;
        }
        
        public List<Warehouse> getWarehouses() {
            return warehouses;
        }
        
        public List<Truck> getTrucks() {
            return trucks;
        }
    }
}
