import React, { useEffect, useState, useCallback } from 'react';
import DriverHeader from '../components/driverHeader';
import MapComponent from '../components/navigation/MapComponent';
import NavigationControls from '../components/navigation/NavigationControls';
import SimulationControls from '../components/navigation/SimulationControls';
import LocationPermission from '../components/navigation/LocationPermission';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import { GraphhopperRoute } from '../../../services/graphhopperService';
import { routeSimulator } from '../../../services/routeSimulator';
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
  const [isSimulationMode, setIsSimulationMode] = useState<boolean>(false);
  const [mapZoom, setMapZoom] = useState<number>(12);

  // Mock destination for demo purposes (you can replace this with real data)
  useEffect(() => {
    // Set a mock destination (Amsterdam city center)
    setDestination([52.3676, 4.9041]);
    setLoading(false);
  }, []);

  const handleLocationGranted = (location: [number, number]) => {
    console.log('Location granted:', location);
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
    console.log('Route updated:', !!route);
    if (route) {
      console.log('Route details:', route);
    }
    setRouteData(route);
  };

  const handleHeadingUpdate = (heading: number) => {
    setCurrentHeading(heading);
  };

  const handleSimulationLocationUpdate = useCallback((location: [number, number]) => {
    setUserLocation(location);
  }, []);

  const handleSimulationHeadingUpdate = useCallback((heading: number) => {
    setCurrentHeading(heading);
  }, []);

  const handleToggleSimulationMode = () => {
    console.log('Toggling simulation mode. Current state:', isSimulationMode);
    console.log('Route data available:', !!routeData);
    
    if (isSimulationMode) {
      // Exiting simulation - stop the simulation
      console.log('Stopping simulation...');
      routeSimulator.stopSimulation();
    }
    
    setIsSimulationMode(!isSimulationMode);
  };

  const handleZoomIn = () => {
    console.log('Zoom In clicked, current zoom:', mapZoom);
    setMapZoom(prev => {
      const newZoom = Math.min(prev + 1, 18);
      console.log('New zoom level:', newZoom);
      return newZoom;
    });
  };

  const handleZoomOut = () => {
    console.log('Zoom Out clicked, current zoom:', mapZoom);
    setMapZoom(prev => {
      const newZoom = Math.max(prev - 1, 8);
      console.log('New zoom level:', newZoom);
      return newZoom;
    });
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
                     onLocationUpdate={isSimulationMode ? handleSimulationLocationUpdate : handleLocationUpdate}
                     onRouteUpdate={handleRouteUpdate}
                     onMapReady={handleMapReady}
                     navigationMode={true}
                     onHeadingUpdate={isSimulationMode ? handleSimulationHeadingUpdate : handleHeadingUpdate}
                     currentHeading={currentHeading}
                     isSimulationMode={isSimulationMode}
                     mapZoom={mapZoom}
                   />
          ) : (
            <LocationPermission
              onLocationGranted={handleLocationGranted}
              onError={handleLocationError}
            />
          )}
        </div>
        
        {/* Simulation Mode Toggle */}
        <div className="simulation-toggle">
          <button
            onClick={handleToggleSimulationMode}
            className={`simulation-toggle__btn ${isSimulationMode ? 'simulation-toggle__btn--active' : ''}`}
          >
            {isSimulationMode ? 'Exit Simulation' : 'Enter Simulation Mode'}
          </button>
        </div>
        
        {/* Zoom Controls */}
        <div className="zoom-controls">
          <button
            onClick={handleZoomOut}
            className="zoom-btn zoom-btn--out"
            title="Zoom Out"
          >
            −
          </button>
          <button
            onClick={handleZoomIn}
            className="zoom-btn zoom-btn--in"
            title="Zoom In"
          >
            +
          </button>
        </div>
        
        {/* Navigation Controls */}
        {userLocation && !isSimulationMode && (
          <NavigationControls
            currentHeading={currentHeading}
            routeData={routeData}
          />
        )}
        
        {/* Debug Panel */}
        {isSimulationMode && (
          <div className="debug-panel">
            <h4>Debug Info</h4>
            <p>User Location: {userLocation ? `${userLocation[0].toFixed(6)}, ${userLocation[1].toFixed(6)}` : 'Not available'}</p>
            <p>Destination: {destination ? `${destination[0].toFixed(6)}, ${destination[1].toFixed(6)}` : 'Not set'}</p>
            <p>Route Data: {routeData ? 'Available' : 'Not loaded'}</p>
            <p>Simulation Mode: {isSimulationMode ? 'Active' : 'Inactive'}</p>
          </div>
        )}

        {/* Simulation Controls */}
        {isSimulationMode && (
          <SimulationControls
            routeData={routeData}
            onLocationUpdate={handleSimulationLocationUpdate}
            onHeadingUpdate={handleSimulationHeadingUpdate}
          />
        )}
        
      </div>
    </div>
  );
};

export default GraphhopperNavigation;