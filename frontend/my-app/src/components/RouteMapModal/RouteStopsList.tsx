import React from 'react';
import { RouteResponse, ParcelResponse } from '../../services/plannerService';
import { formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
import '../RouteMapModal.css';

interface RouteStopsListProps {
  routeStops: RouteResponse['routeStops'];
  parcelStatuses: Map<number, string>;
}

export default function RouteStopsList({ routeStops, parcelStatuses }: RouteStopsListProps) {
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
        {stopsWithParcels.map((stop, stopIndex) => (
          <div key={stop.stopId} className="route-stop-item">
            <div className="route-stop-header">
              <span className="stop-number">Stop {stopIndex + 1}</span>
              <span className="stop-priority">Priority: {stop.priority}</span>
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
                      <div className="parcel-address">
                        <strong>Address:</strong> {getFullDeliveryAddress(parcel)}
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
        ))}
      </div>
    </div>
  );
}

