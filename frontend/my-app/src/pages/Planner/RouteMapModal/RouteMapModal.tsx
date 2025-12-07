import React, { useState, useEffect, useRef } from 'react';
import { RouteAssignment } from '../../../types';
import { plannerService, RouteResponse, ParcelResponse } from '../../../services/plannerService';
import RouteStopsList from './RouteStopsList';
import RouteInfo from './RouteInfo';
import RouteMapView from './RouteMapView';
import './RouteMapModal.css';

interface RouteMapModalProps {
  assignment: RouteAssignment | null;
  onReturn: () => void;
}

export default function RouteMapModal({ assignment, onReturn }: RouteMapModalProps) {
  const [routeDetails, setRouteDetails] = useState<RouteResponse | null>(null);
  const [parcelStatuses, setParcelStatuses] = useState<Map<number, string>>(new Map());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const has429ErrorRef = useRef(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (assignment?.routeId) {
      loadRouteDetails();
    }
  }, [assignment?.routeId]);

  useEffect(() => {
    if (routeDetails && routeDetails.routeStops) {
      const initialStatusMap = new Map<number, string>();
      routeDetails.routeStops.forEach(stop => {
        if (stop.parcelsToDeliver) {
          stop.parcelsToDeliver.forEach(parcel => {
            initialStatusMap.set(parcel.parcelId, parcel.status);
          });
        }
      });
      setParcelStatuses(initialStatusMap);
      
      has429ErrorRef.current = false;
      
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      
      return () => {
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
      };
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
    
    if (has429ErrorRef.current) {
      console.warn('Skipping parcel status refresh due to previous 429 error');
      return;
    }

    const parcelIds: number[] = [];
    routeDetails.routeStops.forEach(stop => {
      if (stop.parcelsToDeliver) {
        stop.parcelsToDeliver.forEach(parcel => {
          parcelIds.push(parcel.parcelId);
        });
      }
    });

    if (parcelIds.length === 0) return;

    const statusMap = new Map<number, string>();
    let has429 = false;
    
    const batchSize = 5;
    for (let i = 0; i < parcelIds.length; i += batchSize) {
      const batch = parcelIds.slice(i, i + batchSize);
      
      await Promise.all(
        batch.map(async (parcelId) => {
          try {
            const parcel = await plannerService.getParcelById(parcelId);
            statusMap.set(parcelId, parcel.status);
          } catch (err: any) {
            console.error(`Error fetching parcel ${parcelId}:`, err);
            const errorMessage = err.message || '';
            if (errorMessage.includes('429') || 
                errorMessage.includes('Too many requests') ||
                errorMessage.includes('Please try again later')) {
              has429 = true;
              has429ErrorRef.current = true;
              if (intervalRef.current) {
                clearInterval(intervalRef.current);
                intervalRef.current = null;
              }
            }
          }
        })
      );
      
      if (has429) {
        break;
      }
      
      if (i + batchSize < parcelIds.length) {
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
    
    if (statusMap.size > 0) {
      setParcelStatuses(prev => {
        const updated = new Map(prev);
        statusMap.forEach((status, id) => {
          updated.set(id, status);
        });
        return updated;
      });
    }
    
    if (has429) {
      console.warn('Rate limit reached. Parcel status refresh stopped. Using cached data.');
    }
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
            <div className="route-map-split-layout">
              <div className="route-map-left-panel">
                <RouteStopsList 
                  routeStops={routeDetails.routeStops} 
                  parcelStatuses={parcelStatuses}
                  routeStartTime={routeDetails.startTime}
                  routeTotalTransportTime={routeDetails.totalTransportTime}
                />
              </div>
              <div className="route-map-right-panel">
                <RouteMapView 
                  routeDetails={routeDetails}
                  parcelStatuses={parcelStatuses}
                />
              </div>
            </div>
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

