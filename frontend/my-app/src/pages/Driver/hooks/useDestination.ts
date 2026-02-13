import { useState, useEffect } from 'react';
import { Package, Route } from '../../../types';

interface UseDestinationReturn {
  currentDestination: [number, number] | null;
  setCurrentDestination: (destination: [number, number] | null) => void;
}

export function useDestination(
  currentPackage: Package | null,
  isCollectingWarehouse: boolean,
  isNavigatingToDepot: boolean,
  currentRoute: Route | null
): UseDestinationReturn {
  const [currentDestination, setCurrentDestination] = useState<[number, number] | null>(null);

  useEffect(() => {
    if (isCollectingWarehouse && currentRoute?.warehouse) {
      setCurrentDestination([currentRoute.warehouse.latitude, currentRoute.warehouse.longitude]);
    } else if (isNavigatingToDepot && currentRoute?.depot) {
      setCurrentDestination([currentRoute.depot.latitude, currentRoute.depot.longitude]);
    } else if (currentPackage) {
      setCurrentDestination([currentPackage.latitude, currentPackage.longitude]);
    }
  }, [currentPackage, isCollectingWarehouse, isNavigatingToDepot, currentRoute]);

  return {
    currentDestination,
    setCurrentDestination,
  };
}

