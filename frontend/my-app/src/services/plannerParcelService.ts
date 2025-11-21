import { apiConfig } from '../config/apiConfig';
import { authService, ApiResponse } from './authService';

export interface PlannerParcel {
  parcelId: number;
  name: string;
  weight: number;
  volume: number;
  status: string;
  createdAt: string;
  recipientName: string;
  recipientPhone: string;
  deliveryInstructions: string;
  deliveryAddress: string;
  deliveryPostalCode: string;
  deliveryCity: string;
  deliveryLatitude: number;
  deliveryLongitude: number;
  warehouseId: number;
  warehouseAddress: string;
  warehousePostalCode: string;
  warehouseCity: string;
  warehouseLatitude: number;
  warehouseLongitude: number;
}

export interface PaginatedResult<T> {
  totalItems: number;
  data: T[];
  totalPages: number;
  pageSize: number;
  currentPage: number;
}

class PlannerParcelService {
  async getWarehouseParcels(params: {
    warehouseId: number;
    page?: number;
    size?: number;
  }): Promise<PaginatedResult<PlannerParcel>> {
    const token = authService.getToken();
    if (!token) {
      throw new Error('Authentication token not found');
    }

    const { warehouseId, page = 0, size = 10 } = params;

    const url = new URL(`${apiConfig.baseURL}/planner/parcel/getAll`);
    url.searchParams.set('warehouseid', String(warehouseId));
    url.searchParams.set('page', String(page));
    url.searchParams.set('size', String(size));

    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.trim()}`,
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Failed to fetch parcels' }));
      throw new Error(errorData.message || `Failed to fetch parcels (status ${response.status})`);
    }

    const apiResponse: ApiResponse<PaginatedResult<PlannerParcel>> = await response.json();

    if (!apiResponse.success) {
      throw new Error(apiResponse.message || 'Failed to fetch parcels');
    }

    return apiResponse.data;
  }
}

export const plannerParcelService = new PlannerParcelService();
