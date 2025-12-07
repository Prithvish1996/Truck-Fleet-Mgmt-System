import React, { useState, useCallback } from 'react';
import { routeService } from '../../../services/routeService';
import { useDeliveryPackages, useDestination, useDeliveryNavigationHandlers } from '../hooks';
import LoadingView from './views/LoadingView';
import ErrorView from './views/ErrorView';
import CompletedView from './views/CompletedView';
import WarehouseOverviewView from './views/WarehouseOverviewView';
import LocationPermissionView from './views/LocationPermissionView';
import NavigationView from './views/NavigationView';
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

  const {
    handleLocationGranted: handleLocationGrantedBase,
    handleOpenNavigation,
    handleWarehouseCollection,
    handleDeliveryResult,
    handleDepotArrival: handleDepotArrivalBase,
    handleCompleteRoute: handleCompleteRouteBase,
    handleRetry,
  } = useDeliveryNavigationHandlers({
    routeId,
    packages,
    currentRoute,
    currentPackage,
    currentDestination,
    isCollectingWarehouse,
    isNavigatingToDepot,
    userLocation,
    deliveryState,
    setPackages,
    setCurrentPackageIndex,
    setWarehouseCollected,
    setIsCollectingWarehouse,
    setIsNavigatingToDepot,
    setDeliveryState,
    setError,
    setShowWarehouseOverview,
  });

  const handleLocationGranted = useCallback((location: [number, number]) => {
    setUserLocation(location);
    handleLocationGrantedBase(location);
  }, [handleLocationGrantedBase]);

  const handleDepotArrival = useCallback(async (confirmed: boolean) => {
    await handleDepotArrivalBase(confirmed);
    if (confirmed) {
      navigate('/driver/dashboard', { state: { refresh: true } });
    }
  }, [handleDepotArrivalBase, navigate]);

  const handleCompleteRoute = useCallback(async () => {
    await handleCompleteRouteBase();
    navigate('/driver/dashboard', { state: { refresh: true } });
  }, [handleCompleteRouteBase, navigate]);

  const handleBackToDashboard = useCallback(() => {
    navigate('/driver/dashboard');
  }, [navigate]);

  const handleNavigateToRouteOverview = useCallback(() => {
    if (routeId) {
      sessionStorage.setItem('currentRouteId', routeId);
    }
    navigate('/driver/route-overview');
  }, [routeId, navigate]);

  const handleWarehouseOverviewBack = useCallback(() => {
    setShowWarehouseOverview(false);
    setDeliveryState('showing_navigation');
  }, [setDeliveryState]);

  const handleLocationError = useCallback((error: string) => {
    setError(error);
    setDeliveryState('error');
  }, [setError, setDeliveryState]);

  if (deliveryState === 'loading') {
    return <LoadingView onBack={handleBackToDashboard} />;
  }

  if (deliveryState === 'error' && error) {
    return <ErrorView error={error} onBack={handleBackToDashboard} onRetry={handleRetry} />;
  }

  if (deliveryState === 'completed') {
    return <CompletedView onBack={handleBackToDashboard} onComplete={handleCompleteRoute} />;
  }

  if (showWarehouseOverview && currentRoute?.warehouse) {
    return (
      <WarehouseOverviewView
        warehouse={currentRoute.warehouse}
        packages={packages}
        onBack={handleWarehouseOverviewBack}
        onConfirmCollection={() => handleWarehouseCollection(true)}
      />
    );
  }

  if (!userLocation && deliveryState === 'waiting_location') {
    return (
      <LocationPermissionView
        onBack={handleBackToDashboard}
        onLocationGranted={handleLocationGranted}
        onError={handleLocationError}
      />
    );
  }

  if (!userLocation || !currentDestination) {
    return null;
  }

  return (
    <NavigationView
      userLocation={userLocation}
      currentDestination={currentDestination}
      deliveryState={deliveryState}
      currentPackage={currentPackage}
      packages={packages}
      currentRoute={currentRoute}
      isCollectingWarehouse={isCollectingWarehouse}
      isNavigatingToDepot={isNavigatingToDepot}
      routeId={routeId}
      onBack={handleBackToDashboard}
      onLocationUpdate={setUserLocation}
      onOpenNavigation={handleOpenNavigation}
      onNavigateToRouteOverview={handleNavigateToRouteOverview}
      onDeliveryResult={handleDeliveryResult}
      onDepotArrival={handleDepotArrival}
    />
  );
};

export default PackageDeliveryNavigation;
