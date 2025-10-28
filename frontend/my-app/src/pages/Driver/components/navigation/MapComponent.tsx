import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { GraphhopperRoute } from '../../../../services/graphhopperService';
import { mapService } from '../../../../services/mapService';
import './MapComponent.css';

// Fix for default markers in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface MapControllerProps {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate: (route: GraphhopperRoute | null) => void;
  liveTrackingMode?: boolean;
  navigationMode?: boolean;
  onToggleLiveTracking?: () => void;
  onToggleNavigation?: () => void;
  onHeadingUpdate?: (heading: number) => void;
}

const MapController: React.FC<MapControllerProps> = ({ 
  userLocation, 
  destination, 
  onLocationUpdate, 
  onRouteUpdate,
  liveTrackingMode = false,
  navigationMode = false,
  onToggleLiveTracking,
  onToggleNavigation,
  onHeadingUpdate
}) => {
  const map = useMap();
  const [serviceInitialized, setServiceInitialized] = useState(false);

  // Initialize map service
  useEffect(() => {
    if (!serviceInitialized && map) {
      mapService.initialize(map);
      setServiceInitialized(true);
    }
  }, [map, serviceInitialized]);

  // Start/stop location tracking (only when not in navigation mode)
  useEffect(() => {
    if (!navigationMode) {
      mapService.startLocationTracking();
    }
    return () => {
      mapService.stopLocationTracking();
    };
  }, [navigationMode]);

  // Handle live tracking mode changes
  useEffect(() => {
    if (!navigationMode) { // Only set live tracking if not in navigation mode
      mapService.setLiveTrackingMode(liveTrackingMode);
    }
  }, [liveTrackingMode, navigationMode]);

  // Handle navigation mode changes
  useEffect(() => {
    mapService.setNavigationMode(navigationMode);
  }, [navigationMode]);

  // Load route when user location or destination changes (only after map is initialized)
  useEffect(() => {
    if (userLocation && destination && serviceInitialized) {
      mapService.loadRoute(userLocation, destination);
    }
  }, [userLocation, destination, serviceInitialized]);

  // Set up callbacks
  useEffect(() => {
    const callbacks = {
      onLocationUpdate,
      onRouteUpdate,
      onHeadingUpdate,
      onError: (error: string) => {
        console.error('MapService error:', error);
      }
    };

    // Update callbacks in the service
    mapService.setCallbacks(callbacks);
  }, [onLocationUpdate, onRouteUpdate, onHeadingUpdate]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      mapService.cleanup();
    };
  }, []);

  return null;
};

interface MapComponentProps {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate: (route: GraphhopperRoute | null) => void;
  onMapReady?: () => void;
  className?: string;
  liveTrackingMode?: boolean;
  navigationMode?: boolean;
  onToggleLiveTracking?: () => void;
  onToggleNavigation?: () => void;
  onHeadingUpdate?: (heading: number) => void;
}

const MapComponent: React.FC<MapComponentProps> = ({
  userLocation,
  destination,
  onLocationUpdate,
  onRouteUpdate,
  onMapReady,
  className = '',
  liveTrackingMode = false,
  navigationMode = false,
  onToggleLiveTracking,
  onToggleNavigation,
  onHeadingUpdate
}) => {
  const [mapReady, setMapReady] = useState(false);

  const handleMapReady = () => {
    console.log('Map is ready');
    setMapReady(true);
    if (onMapReady) {
      onMapReady();
    }
  };

  return (
    <div className={`map-component ${className}`}>
      {userLocation ? (
        <MapContainer
          key="navigation-map"
          center={userLocation}
          zoom={18}
          style={{ height: '100%', width: '100%' }}
          zoomControl={false}
          scrollWheelZoom={false}
          doubleClickZoom={false}
          touchZoom={false}
          boxZoom={false}
          keyboard={false}
          dragging={false}
          whenReady={handleMapReady}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            eventHandlers={{
              loading: () => console.log('Tiles loading'),
              load: () => console.log('Tiles loaded'),
              error: (e) => console.error('Tile loading error:', e)
            }}
          />
          <MapController
            userLocation={userLocation}
            destination={destination}
            onLocationUpdate={onLocationUpdate}
            onRouteUpdate={onRouteUpdate}
            liveTrackingMode={liveTrackingMode}
            navigationMode={navigationMode}
            onToggleLiveTracking={onToggleLiveTracking}
            onToggleNavigation={onToggleNavigation}
            onHeadingUpdate={onHeadingUpdate}
          />
        </MapContainer>
      ) : (
        <div className="map-component__placeholder">
          <p>Map will appear when location is available</p>
        </div>
      )}
    </div>
  );
};

export default MapComponent;
