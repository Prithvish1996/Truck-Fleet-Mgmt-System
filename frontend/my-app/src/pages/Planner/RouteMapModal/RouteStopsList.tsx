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
        <h2 className="route-stops-main-title">Route Tracking</h2>
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
        <h2 className="route-stops-main-title">Route Tracking</h2>
        <div style={{ padding: '20px', textAlign: 'center' }}>No stops with parcels available</div>
      </div>
    );
  }

  const sortedStops = [...stopsWithParcels].sort((a, b) => a.priority - b.priority);

  const getStopDeliveryStatus = (stop: typeof sortedStops[0]) => {
    if (!stop.parcelsToDeliver || stop.parcelsToDeliver.length === 0) {
      return { isFullyDelivered: false, deliveredCount: 0, totalCount: 0 };
    }

    const deliveredCount = stop.parcelsToDeliver.filter(parcel => {
      const status = parcelStatuses.get(parcel.parcelId) || parcel.status;
      return status === 'DELIVERED';
    }).length;

    const totalCount = stop.parcelsToDeliver.length;
    const isFullyDelivered = deliveredCount === totalCount;

    return { isFullyDelivered, deliveredCount, totalCount };
  };

  let lastDeliveredStopIndex = -1;
  for (let i = 0; i < sortedStops.length; i++) {
    const { isFullyDelivered } = getStopDeliveryStatus(sortedStops[i]);
    if (isFullyDelivered) {
      lastDeliveredStopIndex = i;
    } else {
      break;
    }
  }

  return (
    <div className="route-stops-list">
      <h2 className="route-stops-main-title">Route Tracking</h2>
      <h3 className="route-stops-title">Delivery Stops</h3>
      {lastDeliveredStopIndex >= 0 && (
        <div className="last-delivered-indicator">
          <span className="last-delivered-badge">✓ Last Delivered: Stop {lastDeliveredStopIndex + 1}</span>
        </div>
      )}
      <div className="route-stops-container">
        {sortedStops.map((stop, stopIndex) => {
          const firstParcel = stop.parcelsToDeliver![0];
          const stopAddress = getStopAddress(stop, firstParcel);
          const estimatedTime = routeStartTime && routeTotalTransportTime
            ? calculateStopArrivalTime(routeStartTime, routeTotalTransportTime, stopIndex + 1, sortedStops.length)
            : 'N/A';

          const { isFullyDelivered, deliveredCount, totalCount } = getStopDeliveryStatus(stop);
          const isLastDelivered = stopIndex === lastDeliveredStopIndex;

          return (
            <div 
              key={stop.stopId} 
              className={`route-stop-item ${
                isFullyDelivered ? 'stop-delivered' : ''
              } ${
                isLastDelivered ? 'stop-last-delivered' : ''
              }`}
            >
              <div className="route-stop-header">
                <span className="stop-number">
                  Stop {stopIndex + 1}
                  {isFullyDelivered && (
                    <span className="delivered-indicator"> ✓ Delivered</span>
                  )}
                </span>
                {estimatedTime !== 'N/A' && (
                  <span className="stop-time">⏰ {estimatedTime}</span>
                )}
              </div>
              {isFullyDelivered && (
                <div className="stop-delivery-summary">
                  {deliveredCount}/{totalCount} parcels delivered
                </div>
              )}
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

