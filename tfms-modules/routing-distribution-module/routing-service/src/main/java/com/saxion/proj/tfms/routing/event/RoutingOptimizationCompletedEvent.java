package com.saxion.proj.tfms.routing.event;

import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import java.time.Instant;

/**
 * Domain event for successful route optimization.
 * Enables event-driven architecture for downstream systems.
 */
public class RoutingOptimizationCompletedEvent {

    private final WarehouseRoutingResult routingResult;
    private final Instant timestamp;
    private final String requestId;

    public RoutingOptimizationCompletedEvent(WarehouseRoutingResult routingResult, String requestId) {
        this.routingResult = routingResult;
        this.requestId = requestId;
        this.timestamp = Instant.now();
    }

    public WarehouseRoutingResult getRoutingResult() {
        return routingResult;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public Long getWarehouseId() {
        return routingResult.getGeneratedForWarehouse();
    }
}
