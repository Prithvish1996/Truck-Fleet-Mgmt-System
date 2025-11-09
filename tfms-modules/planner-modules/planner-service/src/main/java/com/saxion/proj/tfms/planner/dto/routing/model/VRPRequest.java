package com.saxion.proj.tfms.planner.dto.routing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Request DTO for VRP optimization
 * Contains all data needed to optimize truck routes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class VRPRequest{
    private DepotInfo depot;
    private List<Parcel> parcels;
}
