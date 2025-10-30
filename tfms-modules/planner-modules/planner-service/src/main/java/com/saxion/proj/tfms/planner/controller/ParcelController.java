package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.ParcelServices.*;
import org.springframework.web.bind.annotation.*;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Autowired
    private IDeleteParcel deleteParcelById;
    @Autowired
    private IUpdateParcel updateParcel;
    @Autowired
    private IGetNextDayParcelSchedule getNextDayParcel;

    //create parcel
    @PostMapping("/create")
    public ApiResponse<ParcelResponseDto> create(
            @Valid @CurrentUser UserContext user,
            @RequestBody ParcelRequestDto dto
    ){
        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        if (!Objects.equals(user.getRole(), "PLANNER")) {
            return ApiResponse.error("Not Authorized");
        }

        return createParcel.Handle(dto);
    }

    /**
     * Endpoint for external APIs to push a list of parcels for creation
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<List<ParcelResponseDto>>> handleParcelCallback(
            @Valid @RequestBody List<ParcelRequestDto> parcelRequests) {

        // need the implement some form of validation to ensure only authorized external endpoint
        // can utilized this callback

        List<ParcelResponseDto> createdParcels = new ArrayList<>();

        for (ParcelRequestDto parcelDto : parcelRequests) {
            // Call existing CreateParcelHandler for each parcel
            ApiResponse<ParcelResponseDto> response = createParcel.Handle(parcelDto);

            if (response.isSuccess() && response.getData() != null) {
                createdParcels.add(response.getData());
            } else {
                // You can log or handle partial failures here
                System.err.println("Failed to create parcel: " + response.getMessage());
            }
        }

        // Wrap the list of created parcels in a standard ApiResponse
        return ResponseEntity.ok(ApiResponse.success(createdParcels));
    }


    // List all parcels
    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllParcels(
            @CurrentUser UserContext user,
            @RequestParam Long warehouseid,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if(!Objects.equals(role, "PLANNER")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(getAllParcels.Handle(warehouseid, searchText, page, size));
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

    // delete parcel by id
    @DeleteMapping("/{parcelid}")
    public ResponseEntity<ApiResponse<Void>> deleteParcel(
            @CurrentUser UserContext user,
            @PathVariable("parcelid") Long parcelid) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if(!Objects.equals(role, "ADMIN")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(deleteParcelById.Handle(parcelid));
    }

    //update parcel
    @PutMapping("/update/{parcelid}")
    public ApiResponse<ParcelResponseDto> update(
            @Valid @CurrentUser UserContext user,
            @RequestBody ParcelRequestDto dto,
            @PathVariable("parcelid") Long parcelid
    ){
        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        if (!Objects.equals(user.getRole(), "PLANNER")) {
            return ApiResponse.error("Not Authorized");
        }

        return updateParcel.Handle(parcelid,dto);
    }

    // return the list of pending parcels scheduled for delivery tomorrow (status = pending)
    // grouped by warehouse
    @GetMapping("/getPendingParcel")
    public ResponseEntity<ApiResponse<Map<String, List<ParcelResponseDto>>>> getPendingParcel(
            @CurrentUser UserContext user) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(getNextDayParcel.Handle());
    }
}
