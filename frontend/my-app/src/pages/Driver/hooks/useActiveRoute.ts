import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { routeService } from '../../../services/routeService';

interface UseActiveRouteReturn {
  routeId: string | undefined;
  loading: boolean;
  error: string | null;
}

export function useActiveRoute(): UseActiveRouteReturn {
  const location = useLocation();
  const [routeId, setRouteId] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadCurrentRoute = async () => {
      try {
        setLoading(true);
        setError(null);

        const stateRouteId = (location.state as any)?.routeId;
        const storedRouteId = sessionStorage.getItem('currentRouteId');
        
        if (stateRouteId || storedRouteId) {
          const idToUse = stateRouteId || storedRouteId;
          const route = await routeService.getRouteById(idToUse, false);
          if (route && (route.status === 'in_progress' || route.status === 'parcels_retrieved')) {
            setRouteId(idToUse);
            setLoading(false);
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
          setError('No active route found');
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load active route';
        setError(errorMessage);
        console.error('Error loading current route:', err);
      } finally {
        setLoading(false);
      }
    };

    loadCurrentRoute();
  }, [location.state]);

  return {
    routeId,
    loading,
    error,
  };
}

