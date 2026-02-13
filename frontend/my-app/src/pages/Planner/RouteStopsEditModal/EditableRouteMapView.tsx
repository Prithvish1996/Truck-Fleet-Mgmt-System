import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { RouteResponse, StopDto, ParcelResponse } from '../../../services/plannerService';
import { formatParcelId, getStopAddress } from '../../../utils/dataTransformers';
import './EditableRouteMapView.css';

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

interface DraggableMarkerProps {
  position: [number, number];
  stop: StopDto;
  routeId: number;
  stopIndex: number;
  onDragEnd: (stopId: number, newPosition: [number, number]) => void;
}

function DraggableMarker({ position, stop, routeId, stopIndex, onDragEnd }: DraggableMarkerProps) {
  const [currentPosition, setCurrentPosition] = useState(position);

  useEffect(() => {
    setCurrentPosition(position);
  }, [position]);

  const markerIcon = L.divIcon({
    className: 'editable-route-map-marker',
    html: `
      <div class="editable-route-marker-content">
        <span class="editable-route-marker-number">${stopIndex + 1}</span>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  const firstParcel = stop.parcelsToDeliver?.[0];
  const stopAddress = firstParcel ? getStopAddress(stop, firstParcel) : 'No address';

  return (
    <Marker
      position={currentPosition}
      icon={markerIcon}
      draggable={true}
      eventHandlers={{
        dragend: (e) => {
          const marker = e.target;
          const newPos: [number, number] = [marker.getLatLng().lat, marker.getLatLng().lng];
          setCurrentPosition(newPos);
          onDragEnd(stop.stopId, newPos);
        },
      }}
    >
      <Popup>
        <div className="editable-route-map-popup">
          <div className="editable-route-map-popup-header">
            <strong>Stop {stopIndex + 1}</strong>
          </div>
          <div className="editable-route-map-popup-address">
            {stopAddress}
          </div>
          <div className="editable-route-map-popup-parcels">
            <strong>Parcels:</strong>{' '}
            {stop.parcelsToDeliver?.map((p, idx) => (
              <span key={p.parcelId} className="editable-route-parcel-badge">
                {formatParcelId(p.parcelId)}
                {idx < (stop.parcelsToDeliver?.length || 0) - 1 ? ', ' : ''}
              </span>
            )) || 'None'}
          </div>
        </div>
      </Popup>
    </Marker>
  );
}

interface EditableRouteMapViewProps {
  route: RouteResponse;
  onStopPositionUpdate?: (routeId: number, stopId: number, newPosition: [number, number]) => void;
}

export default function EditableRouteMapView({ 
  route, 
  onStopPositionUpdate 
}: EditableRouteMapViewProps) {
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

  const editableStops = (route.routeStops || []).filter(
    stop => stop.parcelsToDeliver && stop.parcelsToDeliver.length > 0
  );

  const stopsWithCoords = editableStops
    .map((stop, index) => {
      const coords = getStopCoordinates(stop);
      if (coords) {
        return { stop, coords, index };
      }
      return null;
    })
    .filter((item): item is { stop: StopDto; coords: [number, number]; index: number } => item !== null);

  const stopCoordinates: [number, number][] = stopsWithCoords.map(item => item.coords);

  const handleMarkerDragEnd = (stopId: number, newPosition: [number, number]) => {
    if (onStopPositionUpdate) {
      onStopPositionUpdate(route.routeId, stopId, newPosition);
    }
  };

  const center: [number, number] = stopCoordinates.length > 0 
    ? stopCoordinates[0] 
    : [52.3676, 4.9041];

  if (stopsWithCoords.length === 0) {
    return (
      <div className="editable-route-map-view-placeholder">
        <p>No location data available for route stops</p>
      </div>
    );
  }

  return (
    <div className="editable-route-map-view">
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

        {stopsWithCoords.map(({ stop, coords, index }) => (
          <DraggableMarker
            key={`${route.routeId}-${stop.stopId}`}
            position={coords}
            stop={stop}
            routeId={route.routeId}
            stopIndex={index}
            onDragEnd={handleMarkerDragEnd}
          />
        ))}
      </MapContainer>
    </div>
  );
}

