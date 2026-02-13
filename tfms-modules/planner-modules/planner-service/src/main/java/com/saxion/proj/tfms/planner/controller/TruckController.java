package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.truckServices.IGetTruckById;
import com.saxion.proj.tfms.planner.dto.TruckResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/planner/truck")
public class TruckController {

    @Autowired
    @Qualifier("getTruckByIdHandler")
    private IGetTruckById getTrucks;

    /**
     * 4. Get truck by id
     */
    @GetMapping("/{truckId}")
    public ResponseEntity<ApiResponse<TruckResponseDto>> getTrucks(
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

        return ResponseEntity.ok(getTrucks.Handle(truckId));
    }

}