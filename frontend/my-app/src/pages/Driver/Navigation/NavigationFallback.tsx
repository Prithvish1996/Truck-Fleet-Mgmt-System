import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet-routing-machine';
import 'leaflet/dist/leaflet.css';
import 'leaflet-routing-machine/dist/leaflet-routing-machine.css';
import DriverHeader from '../components/driverHeader';
import { routeService } from '../../../services/routeService';
import { Route, Package } from '../../../types';
import './Navigation.css';

// Fix for default markers in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface NavigationProps {
  navigate: (path: string) => void;
}

interface MapControllerProps {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
}

const MapController: React.FC<MapControllerProps> = ({ userLocation, destination, onLocationUpdate }) => {
  const map = useMap();
  const routingControlRef = useRef<any>(null);
  const lastLocationRef = useRef<[number, number] | null>(null);

  // Cleanup effect for component unmount
  useEffect(() => {
    return () => {
      if (routingControlRef.current && map) {
        try {
          if (routingControlRef.current instanceof L.Polyline) {
            map.removeLayer(routingControlRef.current);
          } else {
            map.removeControl(routingControlRef.current);
          }
        } catch (error) {
          console.warn('Error removing route elements on unmount:', error);
        }
        routingControlRef.current = null;
      }
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

  useEffect(() => {
    // Add debouncing to prevent rapid re-renders
    const timeoutId = setTimeout(() => {
      // Clear existing route elements safely
      if (routingControlRef.current && map) {
        try {
          if (routingControlRef.current instanceof L.Polyline) {
            map.removeLayer(routingControlRef.current);
          } else {
            map.removeControl(routingControlRef.current);
          }
        } catch (error) {
          console.warn('Error removing route elements:', error);
        }
        routingControlRef.current = null;
      }

      // Add simple line between user location and destination (no external routing service)
      if (userLocation && destination && map && map.getContainer()) {
        try {
          // Create a simple straight line between the points
          const line = L.polyline([userLocation, destination], {
            color: '#3388ff',
            weight: 6,
            opacity: 0.8
          }).addTo(map);

          // Store the line reference for cleanup
          routingControlRef.current = line;

          // Add markers for start and end points
          const startMarker = L.marker(userLocation, {
            icon: L.divIcon({
              className: 'custom-marker start-marker',
              html: '<div style="background-color: #4CAF50; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
              iconSize: [20, 20],
              iconAnchor: [10, 10]
            })
          }).addTo(map);

          const endMarker = L.marker(destination, {
            icon: L.divIcon({
              className: 'custom-marker end-marker',
              html: '<div style="background-color: #F44336; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
              iconSize: [20, 20],
              iconAnchor: [10, 10]
            })
          }).addTo(map);

          // Fit map to show both points
          const group = new L.FeatureGroup([line, startMarker, endMarker]);
          map.fitBounds(group.getBounds().pad(0.1));
        } catch (error) {
          console.error('Error creating route line:', error);
        }
      }
    }, 500); // Increased delay to prevent rapid re-renders

    return () => {
      clearTimeout(timeoutId);
      if (routingControlRef.current && map) {
        try {
          // Remove the line and markers
          if (routingControlRef.current instanceof L.Polyline) {
            map.removeLayer(routingControlRef.current);
          } else {
            map.removeControl(routingControlRef.current);
          }
        } catch (error) {
          console.warn('Error removing route elements in cleanup:', error);
        }
        routingControlRef.current = null;
      }
    };
  }, [userLocation, destination, map]);

  return null;
};

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [destination, setDestination] = useState<[number, number] | null>(null);
  const [currentRoute, setCurrentRoute] = useState<Route | null>(null);
  const [firstPackage, setFirstPackage] = useState<Package | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mapReady, setMapReady] = useState(false);

  useEffect(() => {
    const loadRouteData = async () => {
      try {
        setLoading(true);
        // Get the first in-progress route or the first scheduled route
        const routes = await routeService.getDriverRoutes();
        const activeRoute = routes.find(route => route.status === 'in_progress') || 
                           routes.find(route => route.status === 'scheduled');
        
        if (activeRoute && activeRoute.packages.length > 0) {
          setCurrentRoute(activeRoute);
          const firstPkg = activeRoute.packages[0];
          setFirstPackage(firstPkg);
          setDestination([firstPkg.latitude, firstPkg.longitude]);
        } else {
          setError('No active route or packages found');
        }
      } catch (err) {
        console.error('Error loading route data:', err);
        setError('Failed to load route data');
      } finally {
        setLoading(false);
      }
    };

    loadRouteData();
  }, []);

  const handleLocationUpdate = (location: [number, number]) => {
    setUserLocation(location);
  };

  // Force map to invalidate size when container is ready (only once)
  useEffect(() => {
    if (mapReady && userLocation) {
      const timer = setTimeout(() => {
        const mapElement = document.querySelector('.leaflet-container');
        if (mapElement) {
          // Trigger a resize event to ensure map renders properly (only once)
          window.dispatchEvent(new Event('resize'));
        }
      }, 1000); // Increased delay to prevent rapid calls
      return () => clearTimeout(timer);
    }
  }, [mapReady]); // Removed userLocation dependency to prevent rapid calls

  if (loading) {
    return (
      <div className="navigation-container">
        <DriverHeader navigate={navigate} />
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading navigation...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="navigation-container">
        <DriverHeader navigate={navigate} />
        <div className="error-container">
          <p>Error: {error}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="navigation-container">
      <DriverHeader navigate={navigate} />
      <div className="map-container">
        {userLocation ? (
          <MapContainer
            key="navigation-map"
            center={userLocation}
            zoom={18}
            style={{ height: '100%', width: '100%' }}
            zoomControl={false}
            whenReady={() => {
              console.log('Map is ready');
              setMapReady(true);
            }}
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
              onLocationUpdate={handleLocationUpdate}
            />
          </MapContainer>
        ) : (
          <div className="location-request">
            <p>Requesting location permission...</p>
            <button onClick={() => {
              if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                  (position) => {
                    setUserLocation([position.coords.latitude, position.coords.longitude]);
                  },
                  (error) => {
                    console.error('Geolocation error:', error);
                    setError('Location access denied');
                  }
                );
              }
            }}>
              Enable Location
            </button>
          </div>
        )}
      </div>
      
      {firstPackage && (
        <div className="destination-info">
          <h3>Next Delivery</h3>
          <p><strong>{firstPackage.name}</strong></p>
          <p>{firstPackage.address}, {firstPackage.city}</p>
          <p>Recipient: {firstPackage.recipientName}</p>
        </div>
      )}
    </div>
  );
};

export default Navigation;
