import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authService } from '../../../services/authService';
import { routeService } from '../../../services/routeService';
import { Route } from '../../../types';

interface UseDriverRoutesReturn {
  routes: Route[];
  loading: boolean;
  error: string | null;
  loadRoutes: (forceRefresh?: boolean) => Promise<void>;
  startRoute: (routeId: string) => Promise<void>;
}

export function useDriverRoutes(): UseDriverRoutesReturn {
  const navigate = useNavigate();
  const location = useLocation();
  const [routes, setRoutes] = useState<Route[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRoutes = async (forceRefresh: boolean = false) => {
    try {
      setLoading(true);
      setError(null);
      const driverRoutes = await routeService.getDriverRoutes(forceRefresh);
      setRoutes(driverRoutes);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load routes';
      setError(errorMessage);
      console.error('Error loading routes:', err);
    } finally {
      setLoading(false);
    }
  };

  const startRoute = async (routeId: string) => {
    try {
      await routeService.startRoute(routeId);
      sessionStorage.removeItem('currentRouteId');
      navigate('/driver/route-overview', { state: { routeId } });
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to start route';
      setError(errorMessage);
      console.error('Error starting route:', err);
    }
  };

  useEffect(() => {
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'DRIVER') {
      navigate('/');
    } else {
      const shouldRefresh = location.pathname === '/driver/dashboard' && location.state?.refresh;
      loadRoutes(shouldRefresh);
    }
  }, [navigate, location.pathname, location.state]);

  return {
    routes,
    loading,
    error,
    loadRoutes,
    startRoute,
  };
}

