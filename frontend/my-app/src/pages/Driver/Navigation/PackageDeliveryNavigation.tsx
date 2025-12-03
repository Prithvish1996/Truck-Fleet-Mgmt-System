import React, { useState, useCallback } from 'react';
import DriverHeader from '../components/driverHeader';
import MapComponent from '../components/navigation/MapComponent';
import LocationPermission from '../components/navigation/LocationPermission';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';
import DeliveryConfirmation from '../components/navigation/DeliveryConfirmation';
import PackageInfo from '../components/navigation/PackageInfo';
import WarehouseInfo from '../components/navigation/WarehouseInfo';
import WarehousePackageOverview from '../components/navigation/WarehousePackageOverview';
import WarehouseCollectionConfirmation from '../components/navigation/WarehouseCollectionConfirmation';
import DepotInfo from '../components/navigation/DepotInfo';
import DepotArrivalConfirmation from '../components/navigation/DepotArrivalConfirmation';
import DeliveryNavigationControls from '../components/navigation/DeliveryNavigationControls';
import CompletedState from '../components/navigation/CompletedState';
import RouteOverviewButton from '../components/navigation/RouteOverviewButton';
import { deliveryService } from '../../../services/deliveryService';
import { routeService } from '../../../services/routeService';
import { useDeliveryPackages, useDestination } from '../hooks';
import './PackageDeliveryNavigation.css';

interface PackageDeliveryNavigationProps {
  navigate: (path: string, state?: any) => void;
  routeId?: string;
}

const PackageDeliveryNavigation: React.FC<PackageDeliveryNavigationProps> = ({ 
  navigate,
  routeId 
}) => {
  const [userLocation, setUserLocation] = useState<[number, number] | null>(null);
  const [showWarehouseOverview, setShowWarehouseOverview] = useState(false);

  const {
    packages,
    currentRoute,
    deliveryState,
    error,
    currentPackage,
    isCollectingWarehouse,
    isNavigatingToDepot,
    setCurrentPackageIndex,
    setPackages,
    setWarehouseCollected,
    setIsCollectingWarehouse,
    setIsNavigatingToDepot,
    setDeliveryState,
    setError,
  } = useDeliveryPackages(routeId);

  const { currentDestination } = useDestination(
    currentPackage,
    isCollectingWarehouse,
    isNavigatingToDepot,
    currentRoute
  );

  const handleLocationGranted = useCallback((location: [number, number]) => {
    setUserLocation(location);
    setError(null);
    
    if (deliveryState === 'waiting_location') {
      setDeliveryState('showing_navigation');
    }
  }, [deliveryState, setError, setDeliveryState]);

  const handleOpenNavigation = useCallback(() => {
    if (!currentDestination) return;

    let address: string | undefined;
    
    if (isCollectingWarehouse && currentRoute?.warehouse) {
      address = `${currentRoute.warehouse.address}, ${currentRoute.warehouse.city} ${currentRoute.warehouse.postalCode}`;
    } else if (isNavigatingToDepot && currentRoute?.depot) {
      address = `${currentRoute.depot.address}, ${currentRoute.depot.city} ${currentRoute.depot.postalCode}`;
    } else if (currentPackage) {
      address = currentPackage.address 
        ? `${currentPackage.address}, ${currentPackage.city} ${currentPackage.postalCode}`
        : undefined;
    }

    deliveryService.openNavigation(currentDestination, address);
    
    if (isCollectingWarehouse && currentRoute?.warehouse) {
      setShowWarehouseOverview(true);
    } else {
      setDeliveryState('waiting_confirmation');
    }
  }, [currentPackage, currentDestination, isCollectingWarehouse, isNavigatingToDepot, currentRoute]);

  const handleStartWarehouseNavigation = useCallback(() => {
    setShowWarehouseOverview(false);
    setDeliveryState('waiting_confirmation');
  }, []);

  const handleWarehouseCollection = async (confirmed: boolean) => {
    if (!routeId || !currentRoute?.warehouse) return;

    try {
      if (confirmed) {
        const pendingPackageIds = packages
          .filter(pkg => pkg.status === 'pending')
          .map(pkg => pkg.id);
        
        if (pendingPackageIds.length > 0 && currentRoute.routeId) {
          await deliveryService.markPackagesAsPickedUp(pendingPackageIds, routeId);
          
          const updatedPackages = packages.map(pkg => 
            pkg.status === 'pending' ? { ...pkg, status: 'picked_up' as const } : pkg
          );
          setPackages(updatedPackages);
          setWarehouseCollected(true);
        }
        
        setIsCollectingWarehouse(false);
        setShowWarehouseOverview(false);
        setDeliveryState('waiting_location');
        
        if (userLocation) {
          setDeliveryState('showing_navigation');
        }
      } else {
        setDeliveryState('showing_navigation');
      }
    } catch (err) {
      console.error('Error marking packages as picked up:', err);
      setError(err instanceof Error ? err.message : 'Failed to mark packages as picked up');
    }
  };

  const handleDeliveryResult = async (confirmed: boolean) => {
    if (!currentPackage || !routeId) return;

    try {
      await deliveryService.handleDeliveryResult(currentPackage.id, confirmed, routeId);

      if (confirmed) {
        const updatedPackages = packages.map(pkg => 
          pkg.id === currentPackage.id ? { ...pkg, status: 'delivered' as const } : pkg
        );
        setPackages(updatedPackages);
        
        const remainingUndelivered = updatedPackages.filter(pkg => pkg.status !== 'delivered');
        
        if (remainingUndelivered.length > 0) {
          setCurrentPackageIndex(0);
          setDeliveryState('waiting_location');
          
          if (userLocation) {
            setDeliveryState('showing_navigation');
          }
        } else {
          if (currentRoute?.depot) {
            setIsNavigatingToDepot(true);
            setDeliveryState('waiting_location');
            
            if (userLocation) {
              setDeliveryState('showing_navigation');
            }
          } else {
            setDeliveryState('completed');
          }
        }
      } else {
        setDeliveryState('showing_navigation');
      }
    } catch (err) {
      console.error('Error updating package status:', err);
      setError(err instanceof Error ? err.message : 'Failed to update package status');
    }
  };

  const handleDepotArrival = async (confirmed: boolean) => {
    if (!routeId) return;

    try {
      if (confirmed) {
        await routeService.completeRoute(routeId);
        await routeService.getDriverRoutes(true);
        navigate('/driver/dashboard', { state: { refresh: true } });
      } else {
        setDeliveryState('showing_navigation');
      }
    } catch (err) {
      console.error('Error completing route:', err);
      setError(err instanceof Error ? err.message : 'Failed to complete route');
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

  const handleCompleteRoute = async () => {
    if (!routeId) return;
    
    try {
      await routeService.completeRoute(routeId);
      await routeService.getDriverRoutes(true);
      navigate('/driver/dashboard', { state: { refresh: true } });
    } catch (err) {
      console.error('Error completing route:', err);
      setError(err instanceof Error ? err.message : 'Failed to complete route');
    }
  };

  if (deliveryState === 'completed') {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <CompletedState onComplete={handleCompleteRoute} />
        </div>
      </div>
    );
  }

  if (showWarehouseOverview && currentRoute?.warehouse) {
    return (
      <div className="package-delivery-navigation">
        <DriverHeader navigate={handleBackToDashboard} />
        <div className="package-delivery-navigation__content">
          <WarehousePackageOverview
            warehouse={currentRoute.warehouse}
            packages={packages}
            onConfirmCollection={() => handleWarehouseCollection(true)}
            onBack={() => {
              setShowWarehouseOverview(false);
              setDeliveryState('showing_navigation');
            }}
          />
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
        
        {isCollectingWarehouse && currentRoute?.warehouse ? (
          <WarehouseInfo
            warehouse={currentRoute.warehouse}
            packageCount={packages.filter(pkg => pkg.status === 'pending').length}
          />
        ) : isNavigatingToDepot && currentRoute?.depot ? (
          <DepotInfo
            depot={currentRoute.depot}
          />
        ) : currentPackage && (
          <PackageInfo
            package={currentPackage}
            packageNumber={packages.findIndex(p => p.id === currentPackage.id) + 1}
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

        {deliveryState === 'waiting_confirmation' && isNavigatingToDepot && currentRoute?.depot && (
          <DepotArrivalConfirmation
            depot={currentRoute.depot}
            onConfirm={handleDepotArrival}
          />
        )}

        {deliveryState === 'waiting_confirmation' && !isCollectingWarehouse && !isNavigatingToDepot && currentPackage && (
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
