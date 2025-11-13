import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../types';
import { plannerService, RouteResponse, ParcelResponse } from '../services/plannerService';
import './RouteMapModal.css';

interface RouteMapModalProps {
  isOpen: boolean;
  assignment: RouteAssignment | null;
  onClose: () => void;
}

export default function RouteMapModal({ isOpen, assignment, onClose }: RouteMapModalProps) {
  const [routeDetails, setRouteDetails] = useState<RouteResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && assignment?.routeId) {
      loadRouteDetails();
    }
  }, [isOpen, assignment?.routeId]);

  const loadRouteDetails = async () => {
    if (!assignment?.routeId) return;

    setLoading(true);
    setError('');
    try {
      const route = await plannerService.getRouteById(assignment.routeId);
      setRouteDetails(route);
    } catch (err: any) {
      console.error('Error loading route details:', err);
      setError(err.message || 'Failed to load route details.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen || !assignment) return null;

  return (
    <div className="route-map-modal-overlay" onClick={onClose}>
      <div className="route-map-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="route-map-header">
          <div className="route-map-truck-plate">{assignment.truckPlateNo}</div>
          <button className="route-map-close-button" onClick={onClose}>
            ×
          </button>
        </div>
        <div className="route-map-container">
          {loading && (
            <div style={{ padding: '40px', textAlign: 'center' }}>Loading route...</div>
          )}
          {error && (
            <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>{error}</div>
          )}
          {!loading && !error && routeDetails && (
            <>
              <div style={{ width: '100%', height: '400px', backgroundColor: '#f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '10px' }}>
                <div>Map will be displayed here with route stops</div>
              </div>
              <div style={{ padding: '10px' }}>
                <h3>Route Stops:</h3>
                <ol>
                  {routeDetails.routeStops?.map((stop, stopIndex) => (
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
            </>
          )}
          {!loading && !error && !routeDetails && (
            <div style={{ padding: '40px', textAlign: 'center' }}>No route details available</div>
          )}
        </div>
        {routeDetails && (
          <div className="route-map-info" style={{ padding: '10px', borderTop: '1px solid #e0e0e0' }}>
            <div>Total Distance: {routeDetails.totalDistance} km</div>
            <div>Duration: {routeDetails.duration || 'N/A'}</div>
            <div>Number of Stops: {routeDetails.routeStops?.length || 0}</div>
          </div>
        )}
      </div>
    </div>
  );
}
