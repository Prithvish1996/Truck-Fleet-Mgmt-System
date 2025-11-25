import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../../types';
import { plannerService, RouteResponse, ParcelResponse } from '../../services/plannerService';
import RouteStopsList from './RouteStopsList';
import RouteInfo from './RouteInfo';
import '../RouteMapModal.css';

interface RouteMapModalProps {
  assignment: RouteAssignment | null;
  onReturn: () => void;
}

export default function RouteMapModal({ assignment, onReturn }: RouteMapModalProps) {
  const [routeDetails, setRouteDetails] = useState<RouteResponse | null>(null);
  const [parcelStatuses, setParcelStatuses] = useState<Map<number, string>>(new Map());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (assignment?.routeId) {
      loadRouteDetails();
    }
  }, [assignment?.routeId]);

  useEffect(() => {
    if (routeDetails && routeDetails.routeStops) {
      refreshParcelStatuses();
      const interval = setInterval(refreshParcelStatuses, 5000);
      return () => clearInterval(interval);
    }
  }, [routeDetails]);

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

  const refreshParcelStatuses = async () => {
    if (!routeDetails || !routeDetails.routeStops) return;

    const parcelIds: number[] = [];
    routeDetails.routeStops.forEach(stop => {
      if (stop.parcelsToDeliver) {
        stop.parcelsToDeliver.forEach(parcel => {
          parcelIds.push(parcel.parcelId);
        });
      }
    });

    const statusMap = new Map<number, string>();
    await Promise.all(
      parcelIds.map(async (parcelId) => {
        try {
          const parcel = await plannerService.getParcelById(parcelId);
          statusMap.set(parcelId, parcel.status);
        } catch (err) {
          console.error(`Error fetching parcel ${parcelId}:`, err);
        }
      })
    );
    setParcelStatuses(statusMap);
  };

  if (!assignment) {
    return (
      <div className="route-map-page">
        <div className="route-map-container-page">
          <div style={{ padding: '40px', textAlign: 'center' }}>No assignment selected</div>
          <div className="return-button-container">
            <button className="return-button" onClick={onReturn}>
              Return
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="route-map-page">
      <div className="route-map-container-page">
        <div className="route-map-header-page">
          <div className="route-map-truck-plate">{assignment.truckPlateNo}</div>
        </div>
        <div className="route-map-content">
          {loading && (
            <div style={{ padding: '40px', textAlign: 'center' }}>Loading route...</div>
          )}
          {error && (
            <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>{error}</div>
          )}
          {!loading && !error && routeDetails && (
            <RouteStopsList 
              routeStops={routeDetails.routeStops} 
              parcelStatuses={parcelStatuses}
            />
          )}
          {!loading && !error && !routeDetails && (
            <div style={{ padding: '40px', textAlign: 'center' }}>No route details available</div>
          )}
        </div>
        {routeDetails && <RouteInfo routeDetails={routeDetails} />}
        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}

