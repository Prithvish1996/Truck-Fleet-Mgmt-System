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

        {stopsWithCoords.map(({ stop, coords }, index) => {
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

