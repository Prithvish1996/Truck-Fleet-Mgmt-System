import React from 'react';
import { RouteResponse } from '../../../services/plannerService';

interface RouteInfoProps {
  routeDetails: RouteResponse;
}

export default function RouteInfo({ routeDetails }: RouteInfoProps) {
  return (
    <div className="route-map-info" style={{ padding: '10px', borderTop: '1px solid #e0e0e0' }}>
      <div>Total Distance: {routeDetails.totalDistance} km</div>
      <div>Duration: {routeDetails.duration || 'N/A'}</div>
      <div>Number of Stops: {routeDetails.routeStops?.length || 0}</div>
    </div>
  );
}

