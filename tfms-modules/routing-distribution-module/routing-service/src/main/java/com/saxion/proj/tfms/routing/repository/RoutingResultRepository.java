package com.saxion.proj.tfms.routing.repository;

import com.saxion.proj.tfms.routing.model.WarehouseRoutingResult;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for routing results persistence.
 * Abstracts data access layer concerns.
 */
public interface RoutingResultRepository {

    /**
     * Save routing result.
     */
    WarehouseRoutingResult save(WarehouseRoutingResult result);

    /**
     * Find result by warehouse ID.
     */
    Optional<WarehouseRoutingResult> findByWarehouseId(Long warehouseId);

    /**
     * Find all results.
     */
    List<WarehouseRoutingResult> findAll();

    /**
     * Delete result.
     */
    void delete(WarehouseRoutingResult result);

    /**
     * Delete by warehouse ID.
     */
    void deleteByWarehouseId(Long warehouseId);
}
