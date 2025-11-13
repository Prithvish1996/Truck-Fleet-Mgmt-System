import { authService } from './authService';
import { apiConfig } from '../config/apiConfig';

export interface ParcelResponse {
  parcelId: number;
  name: string;
  status: string;
  recipientName: string;
  deliveryAddress: string;
  deliveryCity: string;
  deliveryPostalCode: string;
  deliveryLatitude: number;
  deliveryLongitude: number;
  warehouseId: number;
  warehouseAddress: string;
  warehouseCity: string;
  warehousePostalCode: string;
  warehouseLatitude: number;
  warehouseLongitude: number;
  createdAt: string;
  weight?: number;
  volume?: number;
  recipientPhone?: string;
  deliveryInstructions?: string;
  plannedDeliveryDate?: string;
}

export interface ScheduleRequest {
  parcelIds: number[];
  deliveryDate?: string;
}

export interface GenerateRouteRequest {
  depot_id: number;
  parcelIds: number[];
}

export interface RouteResponse {
  routeId: number;
  driverId?: number;
  driverUserName?: string;
  driverEmail?: string;
  driverAvailable?: boolean;
  truckId?: number;
  truckPlateNumber?: string;
  depotId?: number;
  depotName?: string;
  routeStops: StopDto[];
  totalDistance: number;
  totalTransportTime: number;
  startTime: string;
  status: string;
  estimatedFuelCost: number;
  duration: string;
  note?: string;
}

export interface StopDto {
  stopId: number;
  parcelsToDeliver: ParcelResponse[];
  priority: number;
  stopType: string;
}

export interface GenerateRouteResponse {
  assignRoutes: RouteResponse[] | null;
  unAssignedRoute: RouteResponse[];
  trucks: TruckResponse[];
  drivers: DriverResponse[];
}

export interface TruckResponse {
  truckId: number;
  plateNumber: string;
  type: string;
  make?: string;
  isAvailable: boolean;
  volume?: number;
}

export interface DriverResponse {
  id: number;
  Name?: string;
  userName: string;
  email: string;
  isAvailable: boolean;
  city?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
}

export interface AssignRouteRequest {
  routId: number;
  truckId: number;
  driverId: number;
  stops?: StopDto[];
}

class PlannerService {
  private async getAuthHeaders(): Promise<HeadersInit> {
    const token = authService.getToken();
    if (!token) {
      throw new Error('Authentication token not found');
    }
    return {
      'Authorization': `Bearer ${token.trim()}`,
      'Content-Type': 'application/json',
    };
  }

  async getAllParcels(warehouseId: number, page: number = 0, size: number = 50, searchText?: string): Promise<{ content: ParcelResponse[]; totalElements: number; totalPages: number; currentPage: number; pageSize: number }> {
    try {
      const headers = await this.getAuthHeaders();
      const params = new URLSearchParams({
        warehouseid: warehouseId.toString(),
        page: page.toString(),
        size: size.toString(),
      });
      if (searchText) {
        params.append('searchText', searchText);
      }

      const response = await fetch(`${apiConfig.baseURL}/planner/parcel/getAll?${params}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch parcels' }));
        throw new Error(errorData.message || `Failed to fetch parcels (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch parcels');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching parcels:', error);
      throw error;
    }
  }

  async getPendingParcels(): Promise<Record<string, ParcelResponse[]>> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/parcel/getPendingParcel`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch pending parcels' }));
        throw new Error(errorData.message || `Failed to fetch pending parcels (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch pending parcels');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching pending parcels:', error);
      throw error;
    }
  }

  async getScheduledDeliveries(date?: string, page: number = 0, size: number = 50): Promise<{ data: ParcelResponse[]; totalItems: number; totalPages: number; currentPage: number; pageSize: number }> {
    try {
      const headers = await this.getAuthHeaders();
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });
      if (date) {
        params.append('date', date);
      }

      const response = await fetch(`${apiConfig.baseURL}/planner/schedule/paginated?${params}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        // Try to get error message from response
        let errorMessage = `Failed to fetch scheduled deliveries (Status: ${response.status})`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
          // If response is not JSON, use status text
          errorMessage = response.statusText || errorMessage;
        }
        throw new Error(errorMessage);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch scheduled deliveries');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching scheduled deliveries:', error);
      throw error;
    }
  }

  async scheduleParcels(request: ScheduleRequest): Promise<ParcelResponse[]> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/schedule/schedule-next-day`, {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to schedule parcels' }));
        throw new Error(errorData.message || `Failed to schedule parcels (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to schedule parcels');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error scheduling parcels:', error);
      throw error;
    }
  }

  async generateRoutes(request: GenerateRouteRequest): Promise<GenerateRouteResponse> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/routes/generate`, {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to generate routes' }));
        throw new Error(errorData.message || `Failed to generate routes (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to generate routes');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error generating routes:', error);
      throw error;
    }
  }

  async getUnassignedRoutes(): Promise<GenerateRouteResponse> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/routes/unassigned`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch unassigned routes' }));
        throw new Error(errorData.message || `Failed to fetch unassigned routes (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch unassigned routes');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching unassigned routes:', error);
      throw error;
    }
  }

  async getAvailableDrivers(): Promise<DriverResponse[]> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/drivers/available`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch available drivers' }));
        throw new Error(errorData.message || `Failed to fetch available drivers (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch available drivers');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching available drivers:', error);
      throw error;
    }
  }

  async getRouteByDriverId(driverId: number): Promise<{ routes: RouteResponse[] }> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/routes/driver/${driverId}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch route by driver' }));
        throw new Error(errorData.message || `Failed to fetch route by driver (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch route by driver');
      }

      return apiResponse.data; // { routes: RouteResponse[] }
    } catch (error) {
      console.error('Error fetching route by driver:', error);
      throw error;
    }
  }

  async assignDriverToRoute(request: AssignRouteRequest): Promise<string> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/routes/assign`, {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to assign driver' }));
        throw new Error(errorData.message || `Failed to assign driver (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to assign driver');
      }

      return apiResponse.message || 'Driver assigned successfully';
    } catch (error) {
      console.error('Error assigning driver:', error);
      throw error;
    }
  }

  async getRouteById(routeId: number): Promise<RouteResponse> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/routes/${routeId}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch route' }));
        throw new Error(errorData.message || `Failed to fetch route (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch route');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching route:', error);
      throw error;
    }
  }

  async getRouteByTruckId(truckId: number): Promise<{ routes: RouteResponse[] }> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/routes/truck/${truckId}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch route by truck' }));
        throw new Error(errorData.message || `Failed to fetch route by truck (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch route by truck');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching route by truck:', error);
      throw error;
    }
  }

  async getParcelById(parcelId: number): Promise<ParcelResponse> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/parcel/${parcelId}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch parcel' }));
        throw new Error(errorData.message || `Failed to fetch parcel (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch parcel');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching parcel:', error);
      throw error;
    }
  }

  async getWarehouses(page: number = 0, size: number = 10): Promise<{ content: any[]; totalElements: number; totalPages: number }> {
    try {
      const headers = await this.getAuthHeaders();
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });

      const response = await fetch(`${apiConfig.baseURL}/planner/warehouse/paginated?${params}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch warehouses' }));
        throw new Error(errorData.message || `Failed to fetch warehouses (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch warehouses');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching warehouses:', error);
      throw error;
    }
  }

  async getTruckById(truckId: number): Promise<TruckResponse> {
    try {
      const headers = await this.getAuthHeaders();
      const response = await fetch(`${apiConfig.baseURL}/planner/truck/${truckId}`, {
        method: 'GET',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch truck' }));
        throw new Error(errorData.message || `Failed to fetch truck (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch truck');
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Error fetching truck:', error);
      throw error;
    }
  }
}

export const plannerService = new PlannerService();

