package com.saxion.proj.tfms.routing.service.computation.helper;

import com.saxion.proj.tfms.routing.model.RouteCoordinatesGroup;
import com.saxion.proj.tfms.routing.model.Stop;

import java.util.List;

public interface RoutingProvider {

    public List<Stop> calculateRoute(RouteCoordinatesGroup routeCoordinatesGroup);

}
