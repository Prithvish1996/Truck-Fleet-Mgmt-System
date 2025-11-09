import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, useMap, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './MapComponent.css';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

const MapController: React.FC<{ 
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  zoom?: number;
}> = ({ 
  userLocation, 
  destination,
  onLocationUpdate,
  zoom = 12
}) => {
  const map = useMap();
  const watchIdRef = useRef<number | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) {
      console.warn('Geolocation is not supported');
      return;
    }

    watchIdRef.current = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const location: [number, number] = [latitude, longitude];
        onLocationUpdate(location);
        
        if (userLocation) {
          map.setView(location, zoom, { animate: true });
        }
      },
      (error) => {
        console.error('Geolocation error:', error);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 5000
      }
    );

    return () => {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
      }
    };
  }, [map, zoom, onLocationUpdate]);

  useEffect(() => {
    if (userLocation) {
      map.setView(userLocation, zoom, { animate: true });
    } else if (destination) {
      map.setView(destination, zoom, { animate: true });
    }
  }, [userLocation, destination, map, zoom]);

  useEffect(() => {
    if (userLocation && destination) {
      const bounds = L.latLngBounds([userLocation, destination]);
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [userLocation, destination, map]);

  return null;
};

interface MapComponentProps {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate?: (route: any) => void;
  onMapReady?: () => void;
  className?: string;
  liveTrackingMode?: boolean;
  navigationMode?: boolean;
  onToggleLiveTracking?: () => void;
  onToggleNavigation?: () => void;
  onHeadingUpdate?: (heading: number) => void;
  currentHeading?: number;
  isSimulationMode?: boolean;
  mapZoom?: number;
}

const MapComponent: React.FC<MapComponentProps> = ({
  userLocation,
  destination,
  onLocationUpdate,
  onRouteUpdate,
  onMapReady,
  className = '',
  mapZoom = 12
}) => {
  const [mapReady, setMapReady] = useState(false);
  const center = userLocation || destination || [52.3676, 4.9041];

  const handleMapReady = () => {
    console.log('Map is ready');
    setMapReady(true);
    if (onMapReady) {
      onMapReady();
    }
  };

  const userIcon = L.divIcon({
    className: 'map-marker map-marker--user',
    html: '<div class="map-marker__content map-marker__content--user">📍</div>',
    iconSize: [32, 32],
    iconAnchor: [16, 32]
  });

  const destinationIcon = L.icon({
    iconUrl: require('leaflet/dist/images/marker-icon.png'),
    iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
    shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  return (
    <div className={`map-component ${className}`}>
      {(userLocation || destination) ? (
        <div className="map-viewport">
          <div className="map-container">
            <MapContainer
              center={center}
              zoom={mapZoom}
              style={{ height: '100%', width: '100%' }}
              zoomControl={true}
              scrollWheelZoom={true}
              whenReady={handleMapReady}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                maxZoom={19}
                minZoom={1}
              />
              <MapController
                userLocation={userLocation}
                destination={destination}
                onLocationUpdate={onLocationUpdate}
                zoom={mapZoom}
              />
              {userLocation && (
                <Marker position={userLocation} icon={userIcon}>
                  <Popup>Your Location</Popup>
                </Marker>
              )}
              {destination && (
                <Marker position={destination} icon={destinationIcon}>
                  <Popup>Delivery Destination</Popup>
                </Marker>
              )}
            </MapContainer>
          </div>
        </div>
      ) : (
        <div className="map-component__placeholder">
          <p>Map will appear when location is available</p>
        </div>
      )}
    </div>
  );
};

export default MapComponent;