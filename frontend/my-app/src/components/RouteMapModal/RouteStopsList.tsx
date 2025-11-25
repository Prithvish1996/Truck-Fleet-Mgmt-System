import React from 'react';
import { RouteResponse, ParcelResponse } from '../../services/plannerService';

interface RouteStopsListProps {
  routeStops: RouteResponse['routeStops'];
}

export default function RouteStopsList({ routeStops }: RouteStopsListProps) {
  if (!routeStops || routeStops.length === 0) return null;

  return (
    <div style={{ padding: '10px' }}>
      <h3>Route Stops:</h3>
      <ol>
        {routeStops.map((stop, stopIndex) => (
          <li key={stop.stopId}>
            <strong>Stop {stopIndex + 1}</strong> (Priority: {stop.priority})
            <ul>
              {stop.parcelsToDeliver?.map((parcel: ParcelResponse) => (
                <li key={parcel.parcelId}>
                  {parcel.recipientName || parcel.name} - 
                  {parcel.deliveryAddress}, {parcel.deliveryCity}
                  {parcel.deliveryLatitude && parcel.deliveryLongitude && (
                    <span> (Lat: {parcel.deliveryLatitude}, Lng: {parcel.deliveryLongitude})</span>
                  )}
                </li>
              ))}
            </ul>
          </li>
        ))}
      </ol>
    </div>
  );
}

