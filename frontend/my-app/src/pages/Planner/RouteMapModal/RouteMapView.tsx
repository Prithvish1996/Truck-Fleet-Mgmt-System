import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { RouteResponse, StopDto, ParcelResponse } from '../../../services/plannerService';
import { formatParcelId, getStopAddress } from '../../../utils/dataTransformers';
import './RouteMapView.css';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface MapBoundsControllerProps {
  coordinates: [number, number][];
}

function MapBoundsController({ coordinates }: MapBoundsControllerProps) {
  const map = useMap();

  useEffect(() => {
    if (coordinates.length > 0) {
      const bounds = L.latLngBounds(coordinates);
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [coordinates, map]);

  return null;
}

interface RouteMapViewProps {
  routeDetails: RouteResponse;
  parcelStatuses: Map<number, string>;
}

export default function RouteMapView({ routeDetails, parcelStatuses }: RouteMapViewProps) {
  const getStopCoordinates = (stop: StopDto): [number, number] | null => {
    if (stop.location?.latitude && stop.location?.longitude) {
      return [stop.location.latitude, stop.location.longitude];
    }
    
    if (stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0) {
      const firstParcel = stop.parcelsToDeliver[0];
      if (firstParcel.deliveryLatitude && firstParcel.deliveryLongitude) {
        return [firstParcel.deliveryLatitude, firstParcel.deliveryLongitude];
      }
    }
    
    return null;
  };

  const stopsWithParcels = routeDetails.routeStops?.filter(
    stop => stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0
  ) || [];

  const stopCoordinates: [number, number][] = [];
  const stopsWithCoords = stopsWithParcels
    .map(stop => {
      const coords = getStopCoordinates(stop);
      if (coords) {
        stopCoordinates.push(coords);
        return { stop, coords };
      }
      return null;
    })
    .filter((item): item is { stop: StopDto; coords: [number, number] } => item !== null);

  const getStopStatus = (stop: StopDto): 'delivered' | 'pending' | 'in_progress' => {
    if (!stop.parcelsToDeliver || stop.parcelsToDeliver.length === 0) {
      return 'pending';
    }

    const allDelivered = stop.parcelsToDeliver.every(parcel => {
      const status = parcelStatuses.get(parcel.parcelId) || parcel.status;
      return status === 'DELIVERED';
    });

    if (allDelivered) {
      return 'delivered';
    }

    const anyInProgress = stop.parcelsToDeliver.some(parcel => {
      const status = parcelStatuses.get(parcel.parcelId) || parcel.status;
      return status === 'IN_PROGRESS' || status === 'ASSIGNED';
    });

    return anyInProgress ? 'in_progress' : 'pending';
  };

  const getTruckLocation = (): { coords: [number, number] | null; stopIndex: number } => {
    const sortedStops = [...stopsWithCoords].sort((a, b) => a.stop.priority - b.stop.priority);
    
    let lastDeliveredIndex = -1;
    for (let i = 0; i < sortedStops.length; i++) {
      const status = getStopStatus(sortedStops[i].stop);
      if (status === 'delivered') {
        lastDeliveredIndex = i;
      } else {
        break;
      }
    }

    if (lastDeliveredIndex === sortedStops.length - 1) {
      return {
        coords: sortedStops[lastDeliveredIndex].coords,
        stopIndex: lastDeliveredIndex
      };
    }

    if (lastDeliveredIndex === -1) {
      return {
        coords: sortedStops.length > 0 ? sortedStops[0].coords : null,
        stopIndex: 0
      };
    }

    const nextStopIndex = lastDeliveredIndex + 1;
    if (nextStopIndex < sortedStops.length) {
      return {
        coords: sortedStops[nextStopIndex].coords,
        stopIndex: nextStopIndex
      };
    }

    return { coords: null, stopIndex: -1 };
  };

  const getCompletedRoutePath = (): [number, number][] => {
    const sortedStops = [...stopsWithCoords].sort((a, b) => a.stop.priority - b.stop.priority);
    const completedPath: [number, number][] = [];
    
    for (const { stop, coords } of sortedStops) {
      const status = getStopStatus(stop);
      if (status === 'delivered') {
        completedPath.push(coords);
      } else {
        break;
      }
    }
    
    return completedPath;
  };

  const getFullRoutePath = (): [number, number][] => {
    const sortedStops = [...stopsWithCoords].sort((a, b) => a.stop.priority - b.stop.priority);
    return sortedStops.map(({ coords }) => coords);
  };

  const truckLocation = getTruckLocation();
  const completedPath = getCompletedRoutePath();
  const fullPath = getFullRoutePath();

  const getMarkerIcon = (status: 'delivered' | 'pending' | 'in_progress', stopNumber: number) => {
    let color = '#2196F3';
    if (status === 'delivered') {
      color = '#4CAF50';
    } else if (status === 'in_progress') {
      color = '#FF9800';
    }

    return L.divIcon({
      className: 'route-map-marker',
      html: `
        <div class="route-marker-content" style="background-color: ${color};">
          <span class="route-marker-number">${stopNumber}</span>
        </div>
      `,
      iconSize: [32, 32],
      iconAnchor: [16, 32],
    });
  };

  const getTruckIcon = () => {
    return L.divIcon({
      className: 'truck-location-marker',
      html: `
        <div class="truck-icon" style="
          background-color: #FF5722;
          border: 3px solid white;
          border-radius: 50%;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        ">
          <span style="color: white; font-size: 20px; font-weight: bold;">🚚</span>
        </div>
      `,
      iconSize: [40, 40],
      iconAnchor: [20, 20],
    });
  };

  const center: [number, number] = stopCoordinates.length > 0 
    ? stopCoordinates[0] 
    : [52.3676, 4.9041];

  if (stopsWithCoords.length === 0) {
    return (
      <div className="route-map-view-placeholder">
        <p>No location data available for route stops</p>
      </div>
    );
  }

  return (
    <div className="route-map-view">
      <MapContainer
        center={center}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
        zoomControl={true}
        scrollWheelZoom={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          maxZoom={19}
          minZoom={1}
        />
        <MapBoundsController coordinates={stopCoordinates} />

        {truckLocation.coords && (
          <Marker
            position={truckLocation.coords}
            icon={getTruckIcon()}
            zIndexOffset={1000}
          >
            <Popup>
              <div className="route-map-popup">
                <div className="route-map-popup-header">
                  <strong>🚚 Truck Location</strong>
                </div>
                <div className="route-map-popup-address">
                  {truckLocation.stopIndex >= 0 && stopsWithCoords.length > 0 ? (
                    <>
                      <div>
                        <strong>Current Stop:</strong> {truckLocation.stopIndex + 1} of {stopsWithCoords.length}
                      </div>
                      <div>
                        <strong>Progress:</strong> {completedPath.length} of {fullPath.length} stops completed
                      </div>
                    </>
                  ) : (
                    <div>Tracking truck location...</div>
                  )}
                </div>
              </div>
            </Popup>
          </Marker>
        )}

        {[...stopsWithCoords].sort((a, b) => a.stop.priority - b.stop.priority).map(({ stop, coords }, index) => {
          const status = getStopStatus(stop);
          const firstParcel = stop.parcelsToDeliver![0];
          const stopAddress = getStopAddress(stop, firstParcel);
          const deliveredCount = stop.parcelsToDeliver!.filter(parcel => {
            const parcelStatus = parcelStatuses.get(parcel.parcelId) || parcel.status;
            return parcelStatus === 'DELIVERED';
          }).length;
          const totalParcels = stop.parcelsToDeliver!.length;

          return (
            <Marker
              key={stop.stopId}
              position={coords}
              icon={getMarkerIcon(status, index + 1)}
            >
              <Popup>
                <div className="route-map-popup">
                  <div className="route-map-popup-header">
                    <strong>Stop {index + 1}</strong>
                  </div>
                  <div className="route-map-popup-address">
                    {stopAddress}
                  </div>
                  <div className="route-map-popup-parcels">
                    <strong>Parcels:</strong> {deliveredCount}/{totalParcels} delivered
                  </div>
                  <div className="route-map-popup-parcel-list">
                    {stop.parcelsToDeliver!.map((parcel: ParcelResponse) => {
                      const parcelStatus = parcelStatuses.get(parcel.parcelId) || parcel.status;
                      return (
                        <div key={parcel.parcelId} className="route-map-popup-parcel">
                          {formatParcelId(parcel.parcelId)} - {parcelStatus}
                        </div>
                      );
                    })}
                  </div>
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
}

