import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { routeService } from '../../../services/routeService';
import { Route } from '../../../types';

interface UseRouteByIdReturn {
  route: Route | null;
  packages: Route['packages'];
  loading: boolean;
  error: string | null;
  routeId: string | null;
  loadRoute: () => Promise<void>;
}

export function useRouteById(propRouteId?: string): UseRouteByIdReturn {
  const location = useLocation();
  const [route, setRoute] = useState<Route | null>(null);
  const [packages, setPackages] = useState<Route['packages']>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const storedRouteId = sessionStorage.getItem('currentRouteId');
  const routeId = propRouteId || 
                  (location.state as any)?.routeId || 
                  storedRouteId ||
                  null;

  const loadRoute = async () => {
    try {
      setLoading(true);
      setError(null);
      
      if (routeId) {
        const loadedRoute = await routeService.getRouteById(routeId);
        if (loadedRoute) {
          setRoute(loadedRoute);
          setPackages(loadedRoute.packages);
        } else {
          setError('Route not found');
        }
        } else {
          const routes = await routeService.getDriverRoutes();
        const inProgressRoute = routes.find(r => r.status === 'in_progress');
        if (inProgressRoute) {
          setRoute(inProgressRoute);
          setPackages(inProgressRoute.packages);
        } else {
          setError('No active route found');
        }
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load route';
      setError(errorMessage);
      console.error('Error loading route:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRoute();
  }, [routeId]);

  return {
    route,
    packages,
    loading,
    error,
    routeId,
    loadRoute,
  };
}

