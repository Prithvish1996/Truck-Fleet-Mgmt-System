package com.saxion.proj.tfms.routing.service.facade;

import com.saxion.proj.tfms.routing.request.VRPRequest;
import com.saxion.proj.tfms.routing.response.VRPResponse;
import com.saxion.proj.tfms.routing.service.OptimizeRouting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Facade service providing simplified access to routing optimization.
 * Hides complexity of the internal service composition.
 */
@Service
public class RoutingServiceFacade {

    private final OptimizeRouting optimizer;

    @Autowired
    public RoutingServiceFacade(OptimizeRouting optimizer) {
        this.optimizer = optimizer;
    }

    /**
     * Optimize routes for given VRP request.
     * @param request Vehicle routing problem specification
     * @return Optimized routes per warehouse
     */
    public VRPResponse optimizeRoutes(VRPRequest request) {
        return optimizer.optimize(request);
    }
}
