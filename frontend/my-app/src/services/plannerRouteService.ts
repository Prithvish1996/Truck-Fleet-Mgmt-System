import { apiConfig } from '../config/apiConfig';
import { authService, ApiResponse } from './authService';
import { RouteData } from '../types';

interface RoutesByTruckResponse {
  routes: RouteData[];
}

class PlannerRouteService {
  async getRoutesByTruckId(truckId: number): Promise<RouteData[]> {
    const token = authService.getToken();
    if (!token) {
      throw new Error('Authentication token not found');
    }

    const response = await fetch(`${apiConfig.baseURL}/planner/routes/truck/${truckId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.trim()}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Failed to fetch routes' }));
      throw new Error(errorData.message || `Failed to fetch routes (status ${response.status})`);
    }

    const apiResponse: ApiResponse<RoutesByTruckResponse> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message || 'Failed to fetch routes');
    }

    return apiResponse.data.routes;
  }
}

export const plannerRouteService = new PlannerRouteService();
