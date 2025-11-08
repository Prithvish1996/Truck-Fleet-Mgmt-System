package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;

public interface OptimizeRouting {

    public VRPResponse optimize(VRPRequest vrpRequest);
}
