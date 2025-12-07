import { useCallback } from 'react';
import { deliveryService, DeliveryState } from '../../../services/deliveryService';
import { routeService } from '../../../services/routeService';
import { Package, Route } from '../../../types';

interface UseDeliveryNavigationHandlersProps {
  routeId?: string;
  packages: Package[];
  currentRoute: Route | null;
  currentPackage: Package | null;
  currentDestination: [number, number] | null;
  isCollectingWarehouse: boolean;
  isNavigatingToDepot: boolean;
  userLocation: [number, number] | null;
  deliveryState: DeliveryState;
  setPackages: (packages: Package[]) => void;
  setCurrentPackageIndex: (index: number) => void;
  setWarehouseCollected: (collected: boolean) => void;
  setIsCollectingWarehouse: (collecting: boolean) => void;
  setIsNavigatingToDepot: (navigating: boolean) => void;
  setDeliveryState: (state: DeliveryState) => void;
  setError: (error: string | null) => void;
  setShowWarehouseOverview: (show: boolean) => void;
}

interface UseDeliveryNavigationHandlersReturn {
  handleLocationGranted: (location: [number, number]) => void;
  handleOpenNavigation: () => void;
  handleWarehouseCollection: (confirmed: boolean) => Promise<void>;
  handleDeliveryResult: (confirmed: boolean) => Promise<void>;
  handleDepotArrival: (confirmed: boolean) => Promise<void>;
  handleCompleteRoute: () => Promise<void>;
  handleRetry: () => void;
}

export function useDeliveryNavigationHandlers({
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
}: UseDeliveryNavigationHandlersProps): UseDeliveryNavigationHandlersReturn {
  const handleLocationGranted = useCallback((location: [number, number]) => {
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
  }, [currentPackage, currentDestination, isCollectingWarehouse, isNavigatingToDepot, currentRoute, setDeliveryState, setShowWarehouseOverview]);

  const handleWarehouseCollection = useCallback(async (confirmed: boolean) => {
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
  }, [routeId, currentRoute, packages, userLocation, setPackages, setWarehouseCollected, setIsCollectingWarehouse, setShowWarehouseOverview, setDeliveryState, setError]);

  const handleDeliveryResult = useCallback(async (confirmed: boolean) => {
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
  }, [currentPackage, routeId, packages, userLocation, currentRoute, setPackages, setCurrentPackageIndex, setIsNavigatingToDepot, setDeliveryState, setError]);

  const handleDepotArrival = useCallback(async (confirmed: boolean) => {
    if (!routeId) return;

    try {
      if (confirmed) {
        await routeService.completeRoute(routeId);
        await routeService.getDriverRoutes(true);
      } else {
        setDeliveryState('showing_navigation');
      }
    } catch (err) {
      console.error('Error completing route:', err);
      setError(err instanceof Error ? err.message : 'Failed to complete route');
    }
  }, [routeId, setDeliveryState, setError]);

  const handleCompleteRoute = useCallback(async () => {
    if (!routeId) return;
    
    try {
      await routeService.completeRoute(routeId);
      await routeService.getDriverRoutes(true);
    } catch (err) {
      console.error('Error completing route:', err);
      setError(err instanceof Error ? err.message : 'Failed to complete route');
    }
  }, [routeId, setError]);

  const handleRetry = useCallback(() => {
    setError(null);
    if (userLocation && currentPackage) {
      setDeliveryState('showing_navigation');
    } else {
      setDeliveryState('waiting_location');
    }
  }, [userLocation, currentPackage, setError, setDeliveryState]);

  return {
    handleLocationGranted,
    handleOpenNavigation,
    handleWarehouseCollection,
    handleDeliveryResult,
    handleDepotArrival,
    handleCompleteRoute,
    handleRetry,
  };
}

