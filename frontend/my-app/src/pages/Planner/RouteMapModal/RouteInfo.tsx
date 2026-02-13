import React from 'react';
import { RouteResponse } from '../../../services/plannerService';

interface RouteInfoProps {
  routeDetails: RouteResponse;
}

export default function RouteInfo({ routeDetails }: RouteInfoProps) {
  const stopsWithParcels = routeDetails.routeStops?.filter(
    stop => stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0
  ) || [];
  
  return (
    <div className="route-map-info" style={{ padding: '10px', borderTop: '1px solid #e0e0e0' }}>
      <div>Total Distance: {routeDetails.totalDistance || 0} km</div>
      <div>Duration: {routeDetails.duration || 'N/A'}</div>
      <div>Number of Stops: {stopsWithParcels.length}</div>
    </div>
  );
}

