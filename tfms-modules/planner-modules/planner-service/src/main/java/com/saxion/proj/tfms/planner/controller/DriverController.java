package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.driverServices.*;
import com.saxion.proj.tfms.planner.dto.DriverAvailabilityRequestDto;
import com.saxion.proj.tfms.planner.dto.DriverResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class DriverController {

    @Autowired
    @Qualifier("getAvailableDriversHandler")
    private IGetAvailableDrivers getAvailableDrivers;

    @Autowired
    @Qualifier("updateDriverAvailabilityHandler")
    private IUpdateDriverAvailability updateDriverAvailability;

    @Autowired
    @Qualifier("assignDriverToTruckHandler")
    private IAssignDriverToTruck assignDriverToTruck;

    @Autowired
    @Qualifier("getDriverByIdHandler")
    private IGetDriverById getDriverById;

    @Autowired
    @Qualifier("createDriverAvailability")
    private ICreateDriverAvailability createDriverAvailability;

    @Autowired
    @Qualifier("createDriverSuggestion")
    private ICreateDriverSuggestion createDriverSuggestion;

    /**
     * 1. Get all available drivers
     */
    @GetMapping("/planner/drivers/available")
    public ResponseEntity<ApiResponse<List<DriverResponseDto>>> getAvailableDrivers(
            @CurrentUser UserContext user
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getAvailableDrivers.Handle());
    }


    /**
     * 2. Update driver availability (e.g., mark driver as available/unavailable)
     */
    @PutMapping("/planner/driver/{driverId}/availability")
    public ResponseEntity<ApiResponse<DriverResponseDto>> updateAvailability(
            @CurrentUser UserContext user,
            @PathVariable Long driverId,
            @RequestParam boolean isAvailable
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(updateDriverAvailability.Handle(driverId, isAvailable));
    }


    /**
     * 3. Assign driver to truck
     * This also sends email notification after assignment.
     */
    @PostMapping("/planner/drivers/{driverId}/assign/{truckId}")
    public ResponseEntity<ApiResponse<String>> assignDriverToTruck(
            @CurrentUser UserContext user,
            @PathVariable Long driverId,
            @PathVariable Long truckId
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(assignDriverToTruck.Handle(driverId, truckId));
    }

    /**
     * 4. Get driver by id
     */
    @GetMapping("/driver/get/{driverId}")
    public ResponseEntity<ApiResponse<DriverResponseDto>> getDriver(
            @CurrentUser UserContext user,
            @PathVariable Long driverId
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(getDriverById.Handle(driverId));
    }

    /**
     * 5. Create driver availabilities
     */
    @PostMapping("/driver/{driverId}/availability")
    public ResponseEntity<ApiResponse<String>> createAvailability(
            @CurrentUser UserContext user,
            @PathVariable Long driverId,
            @RequestBody List<DriverAvailabilityRequestDto> availabilityDates
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(createDriverAvailability.Handle(driverId, availabilityDates));
    }

    /**
     * 6. Create driver suggestions
     */
    @PostMapping("/driver/{driverId}/suggestion")
    public ResponseEntity<ApiResponse<String>> createSuggestion(
            @CurrentUser UserContext user,
            @PathVariable Long driverId,
            @RequestBody String suggestion
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(createDriverSuggestion.Handle(driverId, suggestion));
    }
}
