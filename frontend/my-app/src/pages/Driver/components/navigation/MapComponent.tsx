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

// Component to show simulation marker
const SimulationMarker: React.FC<{ 
  userLocation: [number, number] | null; 
  isSimulationMode: boolean;
  heading?: number;
}> = ({ 
  userLocation, 
  isSimulationMode,
  heading = 0
}) => {
  const map = useMap();

  useEffect(() => {
    if (!userLocation || !isSimulationMode) return;

    // Create a custom marker for simulation
    const simulationIcon = L.divIcon({
      className: 'simulation-marker',
      html: `
        <div class="simulation-marker__content">
          <div class="simulation-marker__arrow" style="transform: rotate(${heading}deg)"></div>
        </div>
      `,
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    });

    const marker = L.marker(userLocation, { icon: simulationIcon }).addTo(map);

    return () => {
      map.removeLayer(marker);
    };
  }, [userLocation, isSimulationMode, heading, map]);

  return null;
};

// Component to follow user location on the map with rotation
const MapLocationFollower: React.FC<{ 
  userLocation: [number, number] | null; 
  navigationMode: boolean;
  heading?: number;
  zoom?: number;
}> = ({ 
  userLocation, 
  navigationMode,
  heading = 0,
  zoom = 12
}) => {
  const map = useMap();
  const lastLocationRef = useRef<[number, number] | null>(null);
  const lastHeadingRef = useRef<number>(0);

  useEffect(() => {
    if (userLocation && navigationMode) {
      // Only update if location has changed significantly (to avoid constant reloading)
      const hasLocationChanged = !lastLocationRef.current || 
        Math.abs(userLocation[0] - lastLocationRef.current[0]) > 0.0001 ||
        Math.abs(userLocation[1] - lastLocationRef.current[1]) > 0.0001;

      if (hasLocationChanged) {
        console.log('MapLocationFollower: Following location:', userLocation, 'with zoom:', zoom);
        map.setView(userLocation, zoom, { animate: false });
        lastLocationRef.current = userLocation;
      }
    }
  }, [userLocation, navigationMode, zoom, map]);

  // Separate effect for rotation to avoid interfering with location updates
  useEffect(() => {
    if (navigationMode && heading !== undefined) {
      const hasHeadingChanged = Math.abs(heading - lastHeadingRef.current) > 2; // Only update if heading changed by 2+ degrees
      
      if (hasHeadingChanged) {
        console.log('MapLocationFollower: Applying rotation:', heading);
        
        // Apply rotation to the map container (like Google Maps)
        const mapContainer = map.getContainer();
        mapContainer.style.transform = `rotate(${heading}deg)`;
        mapContainer.style.transformOrigin = 'center center';
        mapContainer.style.transition = 'transform 0.3s ease-out';
        
        lastHeadingRef.current = heading;
      }
    }
  }, [heading, navigationMode, map]);

  return null;
};

// Component to handle zoom updates
const MapZoomController: React.FC<{
  zoom: number;
}> = ({ zoom }) => {
  const map = useMap();

  useEffect(() => {
    console.log('MapZoomController: Setting zoom to:', zoom);
    map.setZoom(zoom, { animate: false });
  }, [zoom, map]);

  return null;
};

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
  liveTrackingMode = false,
  navigationMode = false,
  onToggleLiveTracking,
  onToggleNavigation,
  onHeadingUpdate,
  currentHeading = 0,
  isSimulationMode = false,
  mapZoom = 12
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
        <div className="map-viewport">
          <div className="map-container">
            <MapContainer
              key="navigation-map"
              center={userLocation}
              zoom={mapZoom}
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
            maxZoom={19}
            minZoom={1}
            tileSize={256}
            zoomOffset={0}
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
          <MapLocationFollower 
            userLocation={userLocation} 
            navigationMode={navigationMode}
            heading={currentHeading}
            zoom={mapZoom}
          />
          <MapZoomController zoom={mapZoom} />
          <SimulationMarker 
            userLocation={userLocation} 
            isSimulationMode={isSimulationMode}
            heading={currentHeading}
          />
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
