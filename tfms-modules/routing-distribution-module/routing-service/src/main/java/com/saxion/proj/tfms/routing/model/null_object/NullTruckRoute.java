package com.saxion.proj.tfms.routing.model.null_object;

import com.saxion.proj.tfms.routing.model.TruckRouteInfo;
import java.util.Collections;

/**
 * Null Object Pattern implementation for TruckRouteInfo.
 * Provides a safe default when no route could be created.
 */
public class NullTruckRoute extends TruckRouteInfo {

    public static final NullTruckRoute INSTANCE = new NullTruckRoute();

    private NullTruckRoute() {
        super();
        setTruckPlateNumber("NONE");
        setDepotId(-1L);
        setDepotName("N/A");
        setRouteStops(Collections.emptyList());
        setTotalDistance(0);
        setTotalTransportTime(0L);
    }

    public static boolean isNullRoute(TruckRouteInfo route) {
        return route instanceof NullTruckRoute;
    }
}
