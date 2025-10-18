package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import org.springframework.web.bind.annotation.*;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.ICreateParcel;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IGetAllParcels;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.IGetParcelById;
import com.saxion.proj.tfms.planner.dto.ParcelRequestDto;
import com.saxion.proj.tfms.planner.dto.ParcelResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planner/parcel")
public class ParcelController {
    @Autowired
    private ICreateParcel createParcel;
    @Autowired
    private IGetAllParcels getAllParcels;
    @Autowired
    private IGetParcelById getParcelById;

    //create parcel
    @PostMapping("/create")
    public ApiResponse<ParcelResponseDto> create(
            @Valid @CurrentUser UserContext user,
            @RequestBody ParcelRequestDto dto
    ){
        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ApiResponse.error("Not Authorized");
        }

        return createParcel.Handle(dto);
    }

    // List all parcels
    @GetMapping("/getAll/{warehouseid}")
    public ResponseEntity<ApiResponse<List<ParcelResponseDto>>> getAllParcels(
            @CurrentUser UserContext user,
            @PathVariable("warehouseid") Long warehouseid) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if(!Objects.equals(role, "ADMIN")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getAllParcels.Handle(warehouseid));
    }

    // get parcel by id
    @GetMapping("/{parcelid}")
    public ResponseEntity<ApiResponse<ParcelResponseDto>> getParcel(
            @CurrentUser UserContext user,
            @PathVariable("parcelid") Long parcelid) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(getParcelById.Handle(parcelid));
    }

}
