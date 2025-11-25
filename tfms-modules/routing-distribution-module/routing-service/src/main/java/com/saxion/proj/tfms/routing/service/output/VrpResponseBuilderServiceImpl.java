package com.saxion.proj.tfms.routing.service.output;

import com.saxion.proj.tfms.routing.constant.Provider;
import com.saxion.proj.tfms.routing.constant.TruckAssignmentAlgorithm;
import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VrpResponseBuilderServiceImpl implements VrpResponseBuilderService {


    @Override
    public VRPResponse buildResponse(List<WarehouseRoutingResult> warehouseRoutingResult) {
        return VRPResponse.builder()
                .warehouseRoutingResults(warehouseRoutingResult)
                .algorythm(TruckAssignmentAlgorithm.BEST_FIT)
                .provider(Provider.TOMTOM)
                .build();
    }
}