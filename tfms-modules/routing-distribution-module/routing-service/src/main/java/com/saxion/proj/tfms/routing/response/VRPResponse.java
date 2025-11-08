package com.saxion.proj.tfms.routing.response;

import com.saxion.proj.tfms.routing.constant.Provider;
import com.saxion.proj.tfms.routing.constant.TruckAssignmentAlgorithm;
import com.saxion.proj.tfms.routing.model.Parcel;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VRPResponse {
    List<WarehouseRoutingResult> warehouseRoutingResults;
    TruckAssignmentAlgorithm algorythm;
    Provider provider;
}
