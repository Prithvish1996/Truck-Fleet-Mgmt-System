package com.saxion.proj.tfms.routing.api;

import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.routing.dto.*;
import com.saxion.proj.tfms.routing.service.VRPProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("routing/api/v1/vrp")
@CrossOrigin(origins = "*")
public class VrpController {

    @Autowired
    @Qualifier("OrToolsVrpService")
    private VRPProvider vrpProvider;

    /**
     * Optimize routes for VRP
     * Returns response wrapped in ApiResponse for consistency
     * Exceptions are handled by TFMSExceptionHandler
     */
    @PostMapping("/optimize")
    public ResponseEntity<ApiResponse<VrpResponseDto>> optimizeRoutes(@RequestBody VrpRequestDto request) throws IOException, InterruptedException {
        VrpResponseDto response = vrpProvider.optimizeRoutes(request);
        ApiResponse<VrpResponseDto> apiResponse = ApiResponse.success(response, "Routes optimized successfully");
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "VRP Routing Service");
        ApiResponse<Map<String, String>> response = ApiResponse.success(health, "Service is healthy");
        return ResponseEntity.ok(response);
    }
}
