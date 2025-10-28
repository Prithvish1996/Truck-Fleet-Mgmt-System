import React, { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { GraphhopperRoute } from '../../../../services/graphhopperService';
import { graphhopperService } from '../../../../services/graphhopperService';
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
}

const MapController: React.FC<MapControllerProps> = ({ 
  userLocation, 
  destination, 
  onLocationUpdate, 
  onRouteUpdate 
}) => {
  const map = useMap();
  const routeLayerRef = useRef<L.Polyline | null>(null);
  const markersRef = useRef<L.Marker[]>([]);
  const lastLocationRef = useRef<[number, number] | null>(null);
  const [currentRoute, setCurrentRoute] = useState<GraphhopperRoute | null>(null);
  const routeFetchTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Cleanup effect for component unmount
  useEffect(() => {
    return () => {
      if (routeLayerRef.current && map) {
        try {
          map.removeLayer(routeLayerRef.current);
        } catch (error) {
          console.warn('Error removing route layer on unmount:', error);
        }
        routeLayerRef.current = null;
      }
      
      // Remove all markers
      markersRef.current.forEach(marker => {
        try {
          map.removeLayer(marker);
        } catch (error) {
          console.warn('Error removing marker on unmount:', error);
        }
      });
      markersRef.current = [];
    };
  }, [map]);

  useEffect(() => {
    // Set up geolocation tracking with throttling
    if (navigator.geolocation) {
      let lastUpdate = 0;
      const throttleDelay = 2000; // Update every 2 seconds max
      
      const watchId = navigator.geolocation.watchPosition(
        (position) => {
          const now = Date.now();
          if (now - lastUpdate < throttleDelay) {
            return; // Skip this update
          }
          lastUpdate = now;
          
          const { latitude, longitude } = position.coords;
          const newLocation: [number, number] = [latitude, longitude];
          
          // Check if location has changed significantly (at least 10 meters)
          if (lastLocationRef.current) {
            const distance = Math.sqrt(
              Math.pow(newLocation[0] - lastLocationRef.current[0], 2) +
              Math.pow(newLocation[1] - lastLocationRef.current[1], 2)
            );
            // Skip if distance is less than ~10 meters (roughly 0.0001 degrees)
            if (distance < 0.0001) {
              return;
            }
          }
          
          lastLocationRef.current = newLocation;
          onLocationUpdate(newLocation);
          
          // Update map view to user location with navigation-style orientation
          map.setView(newLocation, 18);
        },
        (error) => {
          console.error('Geolocation error:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 5000 // Cache for 5 seconds
        }
      );

      return () => {
        navigator.geolocation.clearWatch(watchId);
      };
    }
  }, [map, onLocationUpdate]);

  // Fetch route from Graphhopper with caching
  useEffect(() => {
    const fetchRoute = async () => {
      if (!userLocation || !destination) {
        return;
      }

      // Clear any existing timeout
      if (routeFetchTimeoutRef.current) {
        clearTimeout(routeFetchTimeoutRef.current);
      }

      // Debounce route fetching to prevent rapid API calls
      routeFetchTimeoutRef.current = setTimeout(async () => {
        try {
          console.log('Fetching new route from Graphhopper...');
          const route = await graphhopperService.getRoute({
            points: [userLocation, destination],
            vehicle: 'car',
            instructions: true,
            points_encoded: true
          });

          setCurrentRoute(route);
          onRouteUpdate(route);
          
          // Decode the polyline and create route visualization
          if (route.paths && route.paths.length > 0) {
            const path = route.paths[0];
            const coordinates = graphhopperService.decodePolyline(path.points);
            
            // Clear existing route
            if (routeLayerRef.current) {
              map.removeLayer(routeLayerRef.current);
            }
            
            // Clear existing markers
            markersRef.current.forEach(marker => {
              map.removeLayer(marker);
            });
            markersRef.current = [];

            // Create route polyline
            const routePolyline = L.polyline(coordinates, {
              color: '#3388ff',
              weight: 6,
              opacity: 0.8,
              smoothFactor: 1
            }).addTo(map);

            routeLayerRef.current = routePolyline;

            // Add start marker (user location)
            const startMarker = L.marker(userLocation, {
              icon: L.divIcon({
                className: 'map-marker map-marker--start',
                html: '<div class="map-marker__content map-marker__content--start"></div>',
                iconSize: [20, 20],
                iconAnchor: [10, 10]
              })
            }).addTo(map);

            // Add end marker (destination)
            const endMarker = L.marker(destination, {
              icon: L.divIcon({
                className: 'map-marker map-marker--end',
                html: '<div class="map-marker__content map-marker__content--end"></div>',
                iconSize: [20, 20],
                iconAnchor: [10, 10]
              })
            }).addTo(map);

            markersRef.current = [startMarker, endMarker];

            // Fit map to show the entire route
            const group = new L.FeatureGroup([routePolyline, startMarker, endMarker]);
            map.fitBounds(group.getBounds().pad(0.1));

            console.log('Route displayed successfully');
          }
        } catch (error) {
          console.error('Error fetching route from Graphhopper:', error);
          // Fallback to straight line if Graphhopper fails
          if (routeLayerRef.current) {
            map.removeLayer(routeLayerRef.current);
          }
          
          markersRef.current.forEach(marker => {
            map.removeLayer(marker);
          });
          markersRef.current = [];

          const fallbackLine = L.polyline([userLocation, destination], {
            color: '#ff6b6b',
            weight: 4,
            opacity: 0.6,
            dashArray: '10, 10'
          }).addTo(map);

          routeLayerRef.current = fallbackLine;

          // Add markers
          const startMarker = L.marker(userLocation, {
            icon: L.divIcon({
              className: 'map-marker map-marker--start',
              html: '<div class="map-marker__content map-marker__content--start"></div>',
              iconSize: [20, 20],
              iconAnchor: [10, 10]
            })
          }).addTo(map);

          const endMarker = L.marker(destination, {
            icon: L.divIcon({
              className: 'map-marker map-marker--end',
              html: '<div class="map-marker__content map-marker__content--end"></div>',
              iconSize: [20, 20],
              iconAnchor: [10, 10]
            })
          }).addTo(map);

          markersRef.current = [startMarker, endMarker];

          const group = new L.FeatureGroup([fallbackLine, startMarker, endMarker]);
          map.fitBounds(group.getBounds().pad(0.1));
        }
      }, 500); // Debounce delay
    };

    fetchRoute();

    return () => {
      if (routeFetchTimeoutRef.current) {
        clearTimeout(routeFetchTimeoutRef.current);
      }
    };
  }, [userLocation, destination, map, onRouteUpdate]);

  return null;
};

interface MapComponentProps {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate: (route: GraphhopperRoute | null) => void;
  onMapReady?: () => void;
  className?: string;
}

const MapComponent: React.FC<MapComponentProps> = ({
  userLocation,
  destination,
  onLocationUpdate,
  onRouteUpdate,
  onMapReady,
  className = ''
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
