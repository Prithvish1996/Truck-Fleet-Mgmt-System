package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.routeServices.*;
import com.saxion.proj.tfms.planner.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api")
public class RouteController {
    @Autowired
    @Qualifier("assignDriverToRoute")
    private IAssignDriverToRoute assignDriverToRoute;

    @Autowired
    @Qualifier("createRouteHandler")
    private ICreateRoute createRoute;

    @Autowired
    @Qualifier("getRouteByDriverId")
    private IGetRouteByDriverId getRouteByDriverId;

    @Autowired
    @Qualifier("getRouteById")
    private IGetRouteById getRouteById;

    @Autowired
    @Qualifier("getRouteByTruckId")
    private IGetRouteByTruckId getRouteByTruckId;

    @Autowired
    @Qualifier("getUnassignedRoutes")
    private IGetUnassignedRoutes getUnassignedRoutes;

    @Autowired
    @Qualifier("updateRouteStatus")
    private IUpdateRouteStatus updateRouteStatus;


    // -------------------- Assign Driver to Route --------------------
    @PostMapping("/planner/routes/assign")
    public ResponseEntity<ApiResponse<String>> assignDriverToRoute(
            @CurrentUser UserContext user,
            @Valid @RequestBody AssignRouteRequestDto dto
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(assignDriverToRoute.handle(dto));
    }

    // -------------------- Create Route --------------------
    @PostMapping("/planner/routes/generate")
    public ResponseEntity<ApiResponse<GenerateRouteResponseDto>> createRoute(
            @CurrentUser UserContext user,
            @Valid @RequestBody GenerateRouteRequestDto request
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(createRoute.handle(request));
    }

    // -------------------- Get Routes by Driver --------------------
    @GetMapping("/routes/driver/{driverId}")
    public ResponseEntity<ApiResponse<DriverRouteResponseDto>> getRouteByDriver(
            @CurrentUser UserContext user,
            @PathVariable Long driverId
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(getRouteByDriverId.handle(driverId));
    }

    // -------------------- Get Route by Route ID --------------------
    @GetMapping("/planner/routes/{routeId}")
    public ResponseEntity<ApiResponse<RouteResponseDto>> getRouteById(
            @CurrentUser UserContext user,
            @PathVariable Long routeId
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getRouteById.handle(routeId));
    }

    // -------------------- Get Routes by Truck --------------------
    @GetMapping("/planner/routes/truck/{truckId}")
    public ResponseEntity<ApiResponse<DriverRouteResponseDto>> getRouteByTruck(
            @CurrentUser UserContext user,
            @PathVariable Long truckId
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getRouteByTruckId.handle(truckId));
    }

    // -------------------- Get Unassigned Routes --------------------
    @GetMapping("/planner/routes/unassigned")
    public ResponseEntity<ApiResponse<GenerateRouteResponseDto>> getUnassignedRoutes(
            @CurrentUser UserContext user
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if (!Objects.equals(role, "PLANNER") && !Objects.equals(role, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getUnassignedRoutes.handle());
    }

    // -------------------- Update Route Status --------------------
    @PutMapping("/routes/status")
    public ResponseEntity<ApiResponse<String>> updateRouteStatus(
            @CurrentUser UserContext user,
            @Valid @RequestBody UpdateRouteStatusRequestDto dto
    ) {
        if (!user.isValid()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(updateRouteStatus.handle(dto));
    }
}
