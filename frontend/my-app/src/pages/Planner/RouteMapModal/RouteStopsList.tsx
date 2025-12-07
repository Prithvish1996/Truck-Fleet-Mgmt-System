import React from 'react';
import { RouteResponse, ParcelResponse } from '../../../services/plannerService';
import { formatParcelId, getStopAddress, calculateStopArrivalTime } from '../../../utils/dataTransformers';
import './RouteMapModal.css';

interface RouteStopsListProps {
  routeStops: RouteResponse['routeStops'];
  parcelStatuses: Map<number, string>;
  routeStartTime?: string;
  routeTotalTransportTime?: number;
}

export default function RouteStopsList({ 
  routeStops, 
  parcelStatuses,
  routeStartTime,
  routeTotalTransportTime 
}: RouteStopsListProps) {
  if (!routeStops || routeStops.length === 0) {
    return (
      <div className="route-stops-list">
        <div style={{ padding: '20px', textAlign: 'center' }}>No stops available</div>
      </div>
    );
  }

  const stopsWithParcels = routeStops.filter(
    stop => stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0
  );

  if (stopsWithParcels.length === 0) {
    return (
      <div className="route-stops-list">
        <div style={{ padding: '20px', textAlign: 'center' }}>No stops with parcels available</div>
      </div>
    );
  }

  return (
    <div className="route-stops-list">
      <h3 className="route-stops-title">Delivery Stops</h3>
      <div className="route-stops-container">
        {stopsWithParcels.map((stop, stopIndex) => {
          const firstParcel = stop.parcelsToDeliver![0];
          const stopAddress = getStopAddress(stop, firstParcel);
          const estimatedTime = routeStartTime && routeTotalTransportTime
            ? calculateStopArrivalTime(routeStartTime, routeTotalTransportTime, stopIndex + 1, stopsWithParcels.length)
            : 'N/A';

          return (
            <div key={stop.stopId} className="route-stop-item">
              <div className="route-stop-header">
                <span className="stop-number">Stop {stopIndex + 1}</span>
                {estimatedTime !== 'N/A' && (
                  <span className="stop-time">⏰ {estimatedTime}</span>
                )}
              </div>
              <div className="stop-address">
                <strong>Address:</strong> {stopAddress}
              </div>
              <div className="parcels-list">
                {stop.parcelsToDeliver!.map((parcel: ParcelResponse) => {
                  const status = parcelStatuses.get(parcel.parcelId) || parcel.status;
                  const isDelivered = status === 'DELIVERED';
                  
                  return (
                    <div 
                      key={parcel.parcelId} 
                      className={`parcel-item ${isDelivered ? 'parcel-delivered' : ''}`}
                    >
                      <div className="parcel-header">
                        <span className="parcel-id">{formatParcelId(parcel.parcelId)}</span>
                        {isDelivered && (
                          <span className="delivered-badge">✓ Delivered</span>
                        )}
                      </div>
                      <div className="parcel-info">
                        <div className="parcel-recipient">
                          <strong>Recipient:</strong> {parcel.recipientName || parcel.name || 'N/A'}
                        </div>
                        {parcel.recipientPhone && (
                          <div className="parcel-phone">
                            <strong>Phone:</strong> {parcel.recipientPhone}
                          </div>
                        )}
                        {parcel.deliveryInstructions && (
                          <div className="parcel-instructions">
                            <strong>Instructions:</strong> {parcel.deliveryInstructions}
                          </div>
                        )}
                        <div className="parcel-status">
                          <strong>Status:</strong> <span className={`status-${status.toLowerCase()}`}>{status}</span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

