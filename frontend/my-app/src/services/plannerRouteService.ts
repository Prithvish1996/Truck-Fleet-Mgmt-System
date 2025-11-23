import apiClient from './apiClient';
import { ApiResponse } from './authService';
import { RouteData } from '../types';

interface RoutesByTruckResponse {
  routes: RouteData[];
}

class PlannerRouteService {
  async getRoutesByTruckId(truckId: number): Promise<RouteData[]> {
    const response = await apiClient.get<ApiResponse<RoutesByTruckResponse>>(
      `/planner/routes/truck/${truckId}`
    );

    if (!response.data.success) {
      throw new Error(response.data.message || 'Failed to fetch routes');
    }

    return response.data.data.routes;
  }
}

export const plannerRouteService = new PlannerRouteService();
