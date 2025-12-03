package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IGetDepotById;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IGetDepotByName;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IListDepot;
import com.saxion.proj.tfms.planner.abstractions.depotServices.IUpdateDepot;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseById;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseByParcelId;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IListWarehouses;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IUpdateWarehouse;
import com.saxion.proj.tfms.planner.dto.DepotRequestDto;
import com.saxion.proj.tfms.planner.dto.DepotResponseDto;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/planner/depot")
public class DepotController {

    @Autowired
    @Qualifier("getDepotByIdHandler")
    private IGetDepotById getDepotByIdHandler;

    @Autowired
    @Qualifier("getDepotByNameHandler")
    private IGetDepotByName getDepotByName;

    @Autowired
    @Qualifier("ListDepotHandler")
    private IListDepot listDepot;

    @Autowired
    @Qualifier("UpdateDepotHandler")
    private IUpdateDepot updateHandler;

    // List all available depot with pagination
    @GetMapping("/paginated")
    public ApiResponse<Map<String, Object>> listDepots(
            @Valid @CurrentUser UserContext user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }
        return listDepot.Handle(PageRequest.of(page, size));
    }

    @GetMapping("/get/id/{id}")
    public ApiResponse<DepotResponseDto> getDepot(
            @Valid @CurrentUser UserContext user,
            @PathVariable Long id) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }

        return getDepotByIdHandler.Handle(id);
    }

    //Get depot details by parcel id
    @GetMapping("/get/name/{name}")
    public ApiResponse<DepotResponseDto> getDepotByName(
            @Valid @CurrentUser UserContext user,
            @PathVariable String name) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }

        return getDepotByName.Hanle(name);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<DepotResponseDto> updateWarehouse(
            @Valid @CurrentUser UserContext user,
            @PathVariable Long id,
            @RequestBody @Valid DepotRequestDto dto) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }

        return updateHandler.Handle(id, dto);
    }
}
