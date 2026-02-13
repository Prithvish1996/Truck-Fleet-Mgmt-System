package com.saxion.proj.tfms.planner.controller;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseById;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IGetWarehouseByParcelId;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IListWarehouses;
import com.saxion.proj.tfms.planner.abstractions.warehouseServices.IUpdateWarehouse;
import com.saxion.proj.tfms.planner.dto.WareHouseRequestDto;
import com.saxion.proj.tfms.planner.dto.WareHouseResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/planner/warehouse")
public class WarehouseController {

    @Autowired
    @Qualifier("listWarehousesHandler")
    private  IListWarehouses listHandler;

    @Autowired
    @Qualifier("getWarehouseByIdHandler")
    private IGetWarehouseById getHandler;

    @Autowired
    @Qualifier("getWarehouseByParcelIdHandler")
    private IGetWarehouseByParcelId getByParcelHandler;

    @Autowired
    @Qualifier("updateWarehouseHandler")
    private IUpdateWarehouse updateHandler;

    // List all available warehouses with pagination
    @GetMapping("/paginated")
    public ApiResponse<Map<String, Object>> listWarehouses(
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
        return listHandler.Handle(PageRequest.of(page, size));
    }

    @GetMapping("/get/{id}")
    public ApiResponse<WareHouseResponseDto> getWarehouse(
            @Valid @CurrentUser UserContext user,
            @PathVariable Long id) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }

        return getHandler.Handle(id);
    }

    //Get warehouse details by parcel id
    @GetMapping("/get/parcel/{id}")
    public ApiResponse<WareHouseResponseDto> getWarehouseByParcelId(
            @Valid @CurrentUser UserContext user,
            @PathVariable Long id) {

        if (!user.isValid()) {
            return ApiResponse.error("Invalid token");
        }

        String role = user.getRole();
        if (!"PLANNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("Not Authorized");
        }

        return getByParcelHandler.Handle(id);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<WareHouseResponseDto> updateWarehouse(
            @Valid @CurrentUser UserContext user,
            @PathVariable Long id,
            @RequestBody @Valid WareHouseRequestDto dto) {

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
