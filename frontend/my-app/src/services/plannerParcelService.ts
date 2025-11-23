import apiClient from './apiClient';
import { ApiResponse } from './authService';

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
    const { warehouseId, page = 0, size = 10 } = params;

    const response = await apiClient.get<ApiResponse<PaginatedResult<PlannerParcel>>>(
      '/planner/parcel/getAll',
      {
        params: {
          warehouseid: warehouseId,
          page,
          size,
        },
      }
    );

    if (!response.data.success) {
      throw new Error(response.data.message || 'Failed to fetch parcels');
    }

    return response.data.data;
  }
}

export const plannerParcelService = new PlannerParcelService();
