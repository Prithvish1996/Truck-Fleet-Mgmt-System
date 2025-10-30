package com.saxion.proj.tfms.routing.dto;

/**
 * Enum to define the cost metric for VRP optimization
 * Enhanced to support Capacitated VRP (CVRP) with capacity-aware decisions
 */
public enum VrpMetric {
    /**
     * Use only distance as the cost metric
     */
    DISTANCE,
    
    /**
     * Use only time as the cost metric
     */
    TIME,
    
    /**
     * Use both distance and time as combined cost metric
     */
    BOTH,

    /**
     * Use distance with capacity utilization efficiency
     * Favors routes that maximize truck capacity usage
     */
    DISTANCE_CAPACITY,

    /**
     * Use time with capacity utilization efficiency
     * Favors routes that maximize truck capacity usage while minimizing time
     */
    TIME_CAPACITY,

    /**
     * Use distance, time, and capacity utilization together
     * Triple optimization: minimize distance, minimize time, maximize capacity usage
     */
    DISTANCE_TIME_CAPACITY
}
