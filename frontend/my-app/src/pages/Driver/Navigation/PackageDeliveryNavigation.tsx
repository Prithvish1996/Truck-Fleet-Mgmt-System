import React, { useEffect, useState, useCallback } from 'react';
import DriverHeader from '../components/driverHeader';
import MapComponent from '../components/navigation/MapComponent';
import LocationPermission from '../components/navigation/LocationPermission';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import DeliveryConfirmation from '../components/navigation/DeliveryConfirmation';
import PackageInfo from '../components/navigation/PackageInfo';
import DeliveryNavigationControls from '../components/navigation/DeliveryNavigationControls';
import CompletedState from '../components/navigation/CompletedState';
import RouteOverviewButton from '../components/navigation/RouteOverviewButton';
import { deliveryService, DeliveryState } from '../../../services/deliveryService';
import { Package } from '../../../types';
import './PackageDeliveryNavigation.css';

interface PackageDeliveryNavigationProps {
  navigate: (path: string) => void;
  routeId?: string;
}

const PackageDeliveryNavigation: React.FC<PackageDeliveryNavigationProps> = ({ 
  navigate,
  routeId 
}) => {
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [packages, setPackages] = useState<Package[]>([]);
  const [currentPackageIndex, setCurrentPackageIndex] = useState(0);
  const [deliveryState, setDeliveryState] = useState<DeliveryState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [currentDestination, setCurrentDestination] = useState<[number, number] | null>(null);

  const currentPackage = packages.length > 0 ? packages[currentPackageIndex] : null;

  useEffect(() => {
    const loadPackages = async () => {
      if (!routeId) {
        setError('Route ID is required');
        setDeliveryState('error');
        return;
      }

      try {
        setDeliveryState('loading');
        const loadedPackages = await deliveryService.loadPackages(routeId);
        
        if (loadedPackages.length === 0) {
          setDeliveryState('completed');
          setError('No packages to deliver');
          return;
        }
        
        setPackages(loadedPackages);
        setCurrentPackageIndex(0);
        setDeliveryState('waiting_location');
      } catch (err) {
        console.error('Error loading packages:', err);
        setError(err instanceof Error ? err.message : 'Failed to load packages');
        setDeliveryState('error');
      }
    };

    loadPackages();
  }, [routeId]);

  useEffect(() => {
    if (currentPackage) {
      setCurrentDestination([currentPackage.latitude, currentPackage.longitude]);
    }
  }, [currentPackage]);

  const handleLocationGranted = useCallback((location: [number, number]) => {
    setUserLocation(location);
    setError(null);
    
    if (currentPackage && deliveryState === 'waiting_location') {
      setDeliveryState('showing_navigation');
    }
  }, [currentPackage, deliveryState]);

  const handleOpenNavigation = useCallback(() => {
    if (!currentPackage || !currentDestination) return;

    const address = currentPackage.address 
      ? `${currentPackage.address}, ${currentPackage.city} ${currentPackage.postalCode}`
      : undefined;

    deliveryService.openNavigation(currentDestination, address);
    setDeliveryState('waiting_confirmation');
  }, [currentPackage, currentDestination]);

  const handleDeliveryResult = async (confirmed: boolean) => {
    if (!currentPackage || !routeId) return;

    try {
      await deliveryService.handleDeliveryResult(currentPackage.id, confirmed);

      if (confirmed) {
        if (currentPackageIndex < packages.length - 1) {
          const nextIndex = currentPackageIndex + 1;
          setCurrentPackageIndex(nextIndex);
          setDeliveryState('waiting_location');
          
          if (userLocation) {
            setDeliveryState('showing_navigation');
          }
        } else {
          setDeliveryState('completed');
        }
      } else {
        setDeliveryState('showing_navigation');
      }
    } catch (err) {
      console.error('Error updating package status:', err);
      setError(err instanceof Error ? err.message : 'Failed to update package status');
    }
  };

  const handleBackToDashboard = useCallback(() => {
    navigate('/driver/dashboard');
  }, [navigate]);

  const handleRetry = () => {
    setError(null);
    if (userLocation && currentPackage) {
      setDeliveryState('showing_navigation');
    } else {
      setDeliveryState('waiting_location');
    }
  };

  if (deliveryState === 'loading') {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <LoadingSpinner size="large" message="Loading packages..." />
        </div>
      </div>
    );
  }

  if (deliveryState === 'error' && error) {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <ErrorMessage message={error} onRetry={handleRetry} />
        </div>
      </div>
    );
  }

  if (deliveryState === 'completed') {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <CompletedState onReturnToDashboard={() => navigate('/driver/dashboard')} />
        </div>
      </div>
    );
  }

  if (!userLocation && deliveryState === 'waiting_location') {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <LocationPermission
            onLocationGranted={handleLocationGranted}
            onError={(error) => { setError(error); setDeliveryState('error'); }}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={handleBackToDashboard} />
      <div className="package-delivery-navigation__content">
        <RouteOverviewButton 
          onNavigate={() => {
            if (routeId) {
              sessionStorage.setItem('currentRouteId', routeId);
            }
            navigate('/driver/route-overview');
          }} 
        />
        
        {currentPackage && (
          <PackageInfo
            package={currentPackage}
            packageNumber={currentPackageIndex + 1}
            totalPackages={packages.length}
            estimatedTime={currentPackage.estimatedTravelTime}
          />
        )}

        <div className="package-delivery-navigation__map">
          {userLocation && currentDestination ? (
            <MapComponent
              userLocation={userLocation}
              destination={currentDestination}
              onLocationUpdate={setUserLocation}
              onRouteUpdate={() => {}}
              navigationMode={false}
            />
          ) : (
            <div className="package-delivery-navigation__map-placeholder">
              <p>Loading map...</p>
            </div>
          )}
        </div>

        {deliveryState === 'showing_navigation' && (
          <DeliveryNavigationControls
            state={deliveryState}
            onOpenNavigation={handleOpenNavigation}
          />
        )}

        {deliveryState === 'waiting_confirmation' && currentPackage && (
          <DeliveryConfirmation
            package={currentPackage}
            onConfirm={handleDeliveryResult}
          />
        )}
      </div>
    </div>
  );
};

export default PackageDeliveryNavigation;
