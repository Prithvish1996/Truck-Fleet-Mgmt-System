import { useState, useEffect, useMemo } from 'react';
import { deliveryService, DeliveryState } from '../../../services/deliveryService';
import { routeService } from '../../../services/routeService';
import { Package, Route } from '../../../types';

interface UseDeliveryPackagesReturn {
  packages: Package[];
  currentRoute: Route | null;
  currentPackageIndex: number;
  deliveryState: DeliveryState;
  error: string | null;
  undeliveredPackages: Package[];
  currentPackage: Package | null;
  warehouseCollected: boolean;
  isCollectingWarehouse: boolean;
  isNavigatingToDepot: boolean;
  setCurrentPackageIndex: (index: number) => void;
  setPackages: (packages: Package[]) => void;
  setWarehouseCollected: (collected: boolean) => void;
  setIsCollectingWarehouse: (collecting: boolean) => void;
  setIsNavigatingToDepot: (navigating: boolean) => void;
  setDeliveryState: (state: DeliveryState) => void;
  setError: (error: string | null) => void;
}

export function useDeliveryPackages(routeId?: string): UseDeliveryPackagesReturn {
  const [packages, setPackages] = useState<Package[]>([]);
  const [currentRoute, setCurrentRoute] = useState<Route | null>(null);
  const [currentPackageIndex, setCurrentPackageIndex] = useState(0);
  const [deliveryState, setDeliveryState] = useState<DeliveryState>('loading');
  const [error, setError] = useState<string | null>(null);
  const [warehouseCollected, setWarehouseCollected] = useState(false);
  const [isCollectingWarehouse, setIsCollectingWarehouse] = useState(false);
  const [isNavigatingToDepot, setIsNavigatingToDepot] = useState(false);

  const undeliveredPackages = useMemo(() => 
    packages.filter(pkg => pkg.status !== 'delivered'),
    [packages]
  );

  const currentPackage = useMemo(() => 
    undeliveredPackages.length > 0 ? undeliveredPackages[currentPackageIndex] : null,
    [undeliveredPackages, currentPackageIndex]
  );

  useEffect(() => {
    const loadRouteAndPackages = async () => {
      if (!routeId) {
        setError('Route ID is required');
        setDeliveryState('error');
        return;
      }

      try {
        setDeliveryState('loading');
        setError(null);
        setPackages([]);
        setCurrentRoute(null);
        setCurrentPackageIndex(0);
        setWarehouseCollected(false);
        setIsCollectingWarehouse(false);
        setIsNavigatingToDepot(false);
        
        const route = await routeService.getRouteById(routeId, true);
        if (!route) {
          setError('Route not found');
          setDeliveryState('error');
          return;
        }
        
        setCurrentRoute(route);
        
        const loadedPackages = await deliveryService.loadPackages(routeId);
        
        const undelivered = loadedPackages.filter(pkg => pkg.status !== 'delivered');
        
        if (undelivered.length === 0) {
          setDeliveryState('completed');
          setError('No packages to deliver');
          return;
        }
        
        setPackages(loadedPackages);
        setCurrentPackageIndex(0);
        
        const routeStatusIsParcelsRetrieved = route.status === 'parcels_retrieved';
        const allPackagesPickedUp = loadedPackages.every(pkg => 
          pkg.status === 'picked_up' || pkg.status === 'delivered'
        );
        const parcelsRetrieved = routeStatusIsParcelsRetrieved || allPackagesPickedUp;
        
        setWarehouseCollected(parcelsRetrieved);
        
        if (route.warehouse && !parcelsRetrieved) {
          setIsCollectingWarehouse(true);
          setDeliveryState('waiting_location');
        } else {
          setIsCollectingWarehouse(false);
          setDeliveryState('waiting_location');
        }
      } catch (err) {
        console.error('Error loading packages:', err);
        setError(err instanceof Error ? err.message : 'Failed to load packages');
        setDeliveryState('error');
      }
    };

    loadRouteAndPackages();
  }, [routeId]);

  return {
    packages,
    currentRoute,
    currentPackageIndex,
    deliveryState,
    error,
    undeliveredPackages,
    currentPackage,
    warehouseCollected,
    isCollectingWarehouse,
    isNavigatingToDepot,
    setCurrentPackageIndex,
    setPackages,
    setWarehouseCollected,
    setIsCollectingWarehouse,
    setIsNavigatingToDepot,
    setDeliveryState,
    setError,
  };
}

