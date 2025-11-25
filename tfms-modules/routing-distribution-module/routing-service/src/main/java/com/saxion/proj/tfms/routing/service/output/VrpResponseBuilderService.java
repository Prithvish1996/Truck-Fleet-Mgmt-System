package com.saxion.proj.tfms.routing.service.output;

import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;

import java.util.List;

public interface VrpResponseBuilderService {
    public VRPResponse buildResponse(List<WarehouseRoutingResult> WarehouseRoutingResult);
}
