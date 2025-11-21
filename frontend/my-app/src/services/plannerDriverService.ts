import { apiConfig } from '../config/apiConfig';
import { authService, ApiResponse } from './authService';

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
    const token = authService.getToken();
    if (!token) {
      throw new Error('Authentication token not found');
    }

    const response = await fetch(`${apiConfig.baseURL}/planner/drivers/available`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.trim()}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Failed to fetch drivers' }));
      throw new Error(errorData.message || `Failed to fetch drivers (status ${response.status})`);
    }

    const apiResponse: ApiResponse<PlannerDriver[]> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message || 'Failed to fetch drivers');
    }

    return apiResponse.data;
  }
}

export const plannerDriverService = new PlannerDriverService();
