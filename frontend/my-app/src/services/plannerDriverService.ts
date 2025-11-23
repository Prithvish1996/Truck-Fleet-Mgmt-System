import apiClient from './apiClient';
import { ApiResponse } from './authService';

export interface PlannerDriver {
  id: number;
  userName: string;
  email: string;
  isAvailable: boolean;
  city: string;
  address: string;
  latitude: number | null;
  longitude: number | null;
  availability: unknown;
  suggestions: unknown;
  name: string | null;
}

class PlannerDriverService {
  async getAvailableDrivers(): Promise<PlannerDriver[]> {
    const response = await apiClient.get<ApiResponse<PlannerDriver[]>>(
      '/planner/drivers/available'
    );

    if (!response.data.success) {
      throw new Error(response.data.message || 'Failed to fetch drivers');
    }

    return response.data.data;
  }
}

export const plannerDriverService = new PlannerDriverService();
