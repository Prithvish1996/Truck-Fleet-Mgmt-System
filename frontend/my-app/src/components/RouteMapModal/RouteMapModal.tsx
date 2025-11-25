import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../../types';
import { plannerService, RouteResponse } from '../../services/plannerService';
import RouteStopsList from './RouteStopsList';
import RouteInfo from './RouteInfo';
import MapPlaceholder from './MapPlaceholder';
import '../RouteMapModal.css';

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
              <MapPlaceholder />
              <RouteStopsList routeStops={routeDetails.routeStops} />
            </>
          )}
          {!loading && !error && !routeDetails && (
            <div style={{ padding: '40px', textAlign: 'center' }}>No route details available</div>
          )}
        </div>
        {routeDetails && <RouteInfo routeDetails={routeDetails} />}
      </div>
    </div>
  );
}

