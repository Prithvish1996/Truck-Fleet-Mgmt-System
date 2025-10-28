import React, { useEffect, useState } from 'react';
import DriverHeader from '../components/driverHeader';
import MapComponent from '../components/navigation/MapComponent';
import NavigationControls from '../components/navigation/NavigationControls';
import LocationPermission from '../components/navigation/LocationPermission';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import { GraphhopperRoute } from '../../../services/graphhopperService';
import './Navigation.css';

interface NavigationProps {
  navigate: (path: string) => void;
}

const GraphhopperNavigation: React.FC<NavigationProps> = ({ navigate }) => {
  // State management
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [destination, setDestination] = useState<[number, number] | null>(null);
  const [routeData, setRouteData] = useState<GraphhopperRoute | null>(null);
  const [currentHeading, setCurrentHeading] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Mock destination for demo purposes (you can replace this with real data)
  useEffect(() => {
    // Set a mock destination (Amsterdam city center)
    setDestination([52.3676, 4.9041]);
    setLoading(false);
  }, []);

  const handleLocationGranted = (location: [number, number]) => {
    setUserLocation(location);
    setError(null);
  };

  const handleLocationError = (error: string) => {
    setError(error);
  };

  const handleLocationUpdate = (location: [number, number]) => {
    setUserLocation(location);
  };

  const handleRouteUpdate = (route: GraphhopperRoute | null) => {
    setRouteData(route);
  };

  const handleHeadingUpdate = (heading: number) => {
    setCurrentHeading(heading);
  };


  const handleMapReady = () => {
    console.log('Map is ready for navigation');
  };

  const handleRetry = () => {
    setError(null);
    // Force reload by updating destination
    if (destination) {
      setDestination([...destination]);
    }
  };

  if (loading) {
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

  if (error) {
    return (
      <div className="navigation-container">
        <DriverHeader navigate={navigate} />
        <div className="navigation-container__content">
          <ErrorMessage 
            message={error}
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
          {userLocation ? (
            <MapComponent
              userLocation={userLocation}
              destination={destination}
              onLocationUpdate={handleLocationUpdate}
              onRouteUpdate={handleRouteUpdate}
              onMapReady={handleMapReady}
              navigationMode={true}
              onHeadingUpdate={handleHeadingUpdate}
            />
          ) : (
            <LocationPermission
              onLocationGranted={handleLocationGranted}
              onError={handleLocationError}
            />
          )}
        </div>
        
        {/* Navigation Controls */}
        {userLocation && (
          <NavigationControls
            currentHeading={currentHeading}
            routeData={routeData}
          />
        )}
        
      </div>
    </div>
  );
};

export default GraphhopperNavigation;