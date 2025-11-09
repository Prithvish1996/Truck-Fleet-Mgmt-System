package com.saxion.proj.tfms.planner.dto;

import com.saxion.proj.tfms.commons.model.RouteDao;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RouteResponseDto {
    private Long routeId;
    private Long driverId;
    private String driverUserName;
    private String driverEmail;
    private Boolean DriverAvailable;
    private Long truckId;
    private String truckPlateNumber;
    private Long depotId;
    private String depotName;
    private List<StopDto> routeStops;
    private Long totalDistance;
    private Long totalTransportTime;
    private String note;
    private String startTime;
    private String status;
    private Double estimatedFuelCost;
    private String duration;

    public static RouteResponseDto fromEntity(RouteDao r) {
        RouteResponseDto dto = new RouteResponseDto();
        dto.setRouteId(r.getId());
        dto.setDriverId(r.getDriver() != null ? r.getDriver().getId() : null);
        dto.setDriverEmail(r.getDriver() != null ? r.getDriver().getUser().getEmail() : null);
        dto.setDriverAvailable(r.getDriver() != null ? r.getDriver().getIsAvailable() : null);
        dto.setDriverUserName(r.getDriver() != null ? r.getDriver().getUser().getUsername()  : null);
        dto.setTruckId(r.getTruck() != null ? r.getTruck().getId() : null);
        dto.setTruckPlateNumber(r.getTruck() != null ? r.getTruck().getPlateNumber() : null);
        dto.setDepotId(r.getDepot() != null ? r.getDepot().getId() : null);
        dto.setDepotName(r.getDepot() != null ? r.getDepot().getName() : null);
        dto.setTotalDistance(r.getTotalDistance());
        dto.setTotalTransportTime(r.getTotalTransportTime());
        dto.setNote(r.getNote());
        // Format startTime to include full date (day, month, year) and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dto.setStartTime(r.getStartTime().format(formatter));
        dto.setDuration(r.getDuration());
        dto.setStatus(r.getStatus().name());
        dto.setEstimatedFuelCost(calculateFuelCost(r.getTotalDistance()));
        dto.setRouteStops(r.getStops() != null
                ? r.getStops().stream().map(StopDto::fromEntity).collect(Collectors.toList())
                : null);
        return dto;
    }

    private static Double calculateFuelCost(Long distanceKm)
    {
        // A average truck typically consumed about 20 litres per 100 km
        // To be extended and refine
        Double fuelConsumptionPer100Km = 20.00;
        Double fuelPrice = 1.70;
        return (distanceKm/100.0) * fuelConsumptionPer100Km * fuelPrice;
    }
}
