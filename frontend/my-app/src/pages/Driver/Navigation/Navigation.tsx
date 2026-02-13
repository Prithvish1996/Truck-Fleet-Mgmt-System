import React, { useEffect } from 'react';
import PackageDeliveryNavigation from './PackageDeliveryNavigation';
import { useActiveRoute } from '../hooks';

interface NavigationProps {
  navigate: (path: string) => void;
}

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  const { routeId, error } = useActiveRoute();

  useEffect(() => {
    if (error && !routeId) {
      navigate('/driver/dashboard');
    }
  }, [error, routeId, navigate]);

  if (!routeId) {
    return null;
  }

  return <PackageDeliveryNavigation navigate={navigate} routeId={routeId} />;
};

export default Navigation;