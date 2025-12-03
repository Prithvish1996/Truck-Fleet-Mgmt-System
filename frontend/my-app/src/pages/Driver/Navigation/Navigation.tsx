import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import PackageDeliveryNavigation from './PackageDeliveryNavigation';
import { routeService } from '../../../services/routeService';

interface NavigationProps {
  navigate: (path: string) => void;
}

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  const location = useLocation();
  const [routeId, setRouteId] = useState<string | undefined>(undefined);

  useEffect(() => {
    const loadCurrentRoute = async () => {
      try {
        const stateRouteId = (location.state as any)?.routeId;
        const storedRouteId = sessionStorage.getItem('currentRouteId');
        
        if (stateRouteId || storedRouteId) {
          const idToUse = stateRouteId || storedRouteId;
          const route = await routeService.getRouteById(idToUse, false);
          if (route && (route.status === 'in_progress' || route.status === 'parcels_retrieved')) {
            setRouteId(idToUse);
            return;
          }
        }
        
        const routes = await routeService.getDriverRoutes(false);
        const activeRoute = routes.find(route => 
          route.status === 'in_progress' || route.status === 'parcels_retrieved'
        );
        if (activeRoute) {
          setRouteId(activeRoute.id);
        } else {
          navigate('/driver/dashboard');
        }
      } catch (error) {
        console.error('Error loading current route:', error);
        navigate('/driver/dashboard');
      }
    };

    loadCurrentRoute();
  }, [navigate, location.state]);

  if (!routeId) {
    return null;
  }

  return <PackageDeliveryNavigation navigate={navigate} routeId={routeId} />;
};

export default Navigation;