package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;

public interface OptimizeRoting {

    public VRPResponse optimize(VRPRequest vrpRequest);
}
