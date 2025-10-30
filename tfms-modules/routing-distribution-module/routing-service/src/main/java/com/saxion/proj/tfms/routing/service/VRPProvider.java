package com.saxion.proj.tfms.routing.service;

import com.saxion.proj.tfms.routing.dto.VrpRequestDto;
import com.saxion.proj.tfms.routing.dto.VrpResponseDto;

import java.io.IOException;

public interface VRPProvider {
    public VrpResponseDto optimizeRoutes(VrpRequestDto request) throws IOException, InterruptedException ;
}
