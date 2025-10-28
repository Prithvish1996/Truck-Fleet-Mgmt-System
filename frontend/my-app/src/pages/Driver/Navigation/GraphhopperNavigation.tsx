import React, { useEffect, useState } from 'react';
import DriverHeader from '../components/driverHeader';
import MapComponent from '../components/navigation/MapComponent';
import RouteInfo from '../components/navigation/RouteInfo';
import LocationPermission from '../components/navigation/LocationPermission';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import { navigationService, NavigationState } from '../../../services/navigationService';
import { GraphhopperRoute } from '../../../services/graphhopperService';
import './Navigation.css';

interface NavigationProps {
  navigate: (path: string) => void;
}

const GraphhopperNavigation: React.FC<NavigationProps> = ({ navigate }) => {
  const [state, setState] = useState<NavigationState>(navigationService.getState());

  useEffect(() => {
    // Set up navigation service callbacks
    navigationService.setCallbacks({
      onLocationUpdate: (location: [number, number]) => {
        setState(prev => ({ ...prev, userLocation: location }));
      },
      onRouteUpdate: (route: GraphhopperRoute | null) => {
        setState(prev => ({ ...prev, routeData: route }));
      },
      onStateChange: (updates: Partial<NavigationState>) => {
        setState(prev => ({ ...prev, ...updates }));
      }
    });

    // Load initial route data
    navigationService.loadRouteData();

    // Cleanup on unmount
    return () => {
      navigationService.cleanup();
    };
  }, []);

  const handleLocationGranted = (location: [number, number]) => {
    setState(prev => ({ ...prev, userLocation: location }));
  };

  const handleLocationError = (error: string) => {
    setState(prev => ({ ...prev, error }));
  };

  const handleRetry = () => {
    setState(prev => ({ ...prev, error: null }));
    navigationService.loadRouteData();
  };

  const handleMapReady = () => {
    navigationService.setMapReady(true);
    
    // Force map to invalidate size when container is ready (only once)
    const timer = setTimeout(() => {
      const mapElement = document.querySelector('.leaflet-container');
      if (mapElement) {
        // Trigger a resize event to ensure map renders properly (only once)
        window.dispatchEvent(new Event('resize'));
      }
    }, 1000);
    
    return () => clearTimeout(timer);
  };

  if (state.loading) {
    return (
      <div className="navigation-container">
        <DriverHeader navigate={navigate} />
        <div className="navigation-container__content">
          <LoadingSpinner 
            size="large" 
            message="Loading navigation..." 
            className="navigation-container__loading"
          />
        </div>
      </div>
    );
  }

  if (state.error) {
    return (
      <div className="navigation-container">
        <DriverHeader navigate={navigate} />
        <div className="navigation-container__content">
          <ErrorMessage 
            message={state.error}
            onRetry={handleRetry}
            className="navigation-container__error"
          />
        </div>
      </div>
    );
  }

  return (
    <div className="navigation-container">
      <DriverHeader navigate={navigate} />
      <div className="navigation-container__content">
        <div className="navigation-container__map">
          {state.userLocation ? (
            <MapComponent
              userLocation={state.userLocation}
              destination={state.destination}
              onLocationUpdate={(location) => setState(prev => ({ ...prev, userLocation: location }))}
              onRouteUpdate={(route) => setState(prev => ({ ...prev, routeData: route }))}
              onMapReady={handleMapReady}
            />
          ) : (
            <LocationPermission
              onLocationGranted={handleLocationGranted}
              onError={handleLocationError}
            />
          )}
        </div>
        
        {state.firstPackage && (
          <RouteInfo
            package={state.firstPackage}
            routeData={state.routeData}
            className="navigation-container__route-info"
          />
        )}
      </div>
    </div>
  );
};

export default GraphhopperNavigation;