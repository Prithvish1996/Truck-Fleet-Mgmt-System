import React, { useEffect, useState } from 'react';
import PackageDeliveryNavigation from './PackageDeliveryNavigation';
import { routeService } from '../../../services/routeService';

interface NavigationProps {
  navigate: (path: string) => void;
}

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  const [routeId, setRouteId] = useState<string | undefined>(undefined);

  useEffect(() => {
    const loadCurrentRoute = async () => {
      try {
        const routes = await routeService.getDriverRoutes();
        const inProgressRoute = routes.find(route => route.status === 'in_progress');
        if (inProgressRoute) {
          setRouteId(inProgressRoute.id);
        } else {
          navigate('/driver/dashboard');
        }
      } catch (error) {
        console.error('Error loading current route:', error);
        navigate('/driver/dashboard');
      }
    };

    loadCurrentRoute();
  }, [navigate]);

  if (!routeId) {
    return null;
  }

  return <PackageDeliveryNavigation navigate={navigate} routeId={routeId} />;
};

export default Navigation;