import axios from 'axios';
import { routeService } from './routeService';
import { googleMapsService } from './googleMapsService';
import { Package } from '../types';
import { apiConfig } from '../config/apiConfig';
import { authService } from './authService';
import axiosInstance from '../config/axiosConfig';

export type DeliveryState = 
  | 'loading'
  | 'waiting_location'
  | 'showing_navigation'
  | 'waiting_confirmation'
  | 'completed'
  | 'error';

class DeliveryService {
  async loadPackages(routeId: string, forceRefresh: boolean = true): Promise<Package[]> {
    const cacheKey = `route_packages_${routeId}`;
    
    const routePackages = await routeService.getRoutePackages(routeId, forceRefresh) as Package[];
    
    const packagesArray = Array.isArray(routePackages) ? routePackages : [];

    if (packagesArray.length > 0) {
      localStorage.setItem(cacheKey, JSON.stringify(packagesArray));
    }
    
    return packagesArray;
  }

  async calculateEstimate(
    origin: [number, number],
    destination: [number, number],
    packageId: string
  ): Promise<{ durationText: string; durationInSeconds: number; distanceInMeters: number }> {
    const estimate = await googleMapsService.getTimeEstimate(origin, destination);
    
    await googleMapsService.sendTimeEstimateToBackend(
      packageId,
      origin,
      destination,
      estimate.durationInSeconds,
      estimate.distanceInMeters
    );
    
    return {
      durationText: estimate.durationText,
      durationInSeconds: estimate.durationInSeconds,
      distanceInMeters: estimate.distanceInMeters
    };
  }

  openNavigation(destination: [number, number], address?: string): void {
    googleMapsService.openNavigation(destination, address);
  }

  async handleDeliveryResult(
    packageId: string,
    confirmed: boolean,
    routeId?: string
  ): Promise<{ newStatus: 'pending' | 'delivered' }> {
    const newStatus = confirmed ? 'delivered' : 'pending';
    
    if (confirmed) {
      const token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      try {
        const response = await axios.put(
          `${apiConfig.baseURL}/planner/parcel/status`,
          {
            parcelId: parseInt(packageId, 10),
            status: 'DELIVERED'
          },
          {
            headers: {
              'Authorization': `Bearer ${token.trim()}`,
              'Content-Type': 'application/json',
            },
            withCredentials: true,
          }
        );

        const apiResponse = response.data;
        if (!apiResponse.success) {
          throw new Error(apiResponse.message || 'Failed to update parcel status');
        }
      } catch (error: any) {
        console.error('Error updating parcel status on backend:', error);
        
        if (axios.isAxiosError(error)) {
          const errorMessage = error.response?.data?.message || `Failed to update parcel status (Status: ${error.response?.status})`;
          throw new Error(errorMessage);
        }
        
        throw error;
      }
    }

    await routeService.updatePackageStatus(packageId, newStatus);
    
    if (routeId) {
      const cacheKey = `route_packages_${routeId}`;
      localStorage.removeItem(cacheKey);
      
      await routeService.getDriverRoutes(true);
    }
    
    return { newStatus };
  }

  async markPackagesAsPickedUp(packageIds: string[], routeId?: string): Promise<boolean> {
    try {
      for (const packageId of packageIds) {
        await routeService.updatePackageStatus(packageId, 'picked_up');
      }
      
      if (routeId) {
        const route = await routeService.getRouteById(routeId, true);
        if (route && route.routeId && route.status !== 'parcels_retrieved') {
          const token = authService.getToken();
          if (token) {
            try {
              const response = await axiosInstance.put(
                `${apiConfig.baseURL}/routes/status`,
                {
                  routeId: route.routeId,
                  status: 'PARCELS_RETRIEVED'
                },
                {
                  headers: {
                    'Authorization': `Bearer ${token.trim()}`,
                  },
                }
              );

              const apiResponse = response.data;
              if (apiResponse.success) {
                const driverId = authService.getDriverId();
                if (driverId) {
                  const cacheKey = `driver_routes_${driverId}`;
                  localStorage.removeItem(cacheKey);
                  localStorage.removeItem(`${cacheKey}_time`);
                }
              }
            } catch (error: any) {
              console.warn('Error updating route status to PARCELS_RETRIEVED:', error.response?.data?.message || error.message);
            }
          }
        }
        
        const cacheKey = `route_packages_${routeId}`;
        localStorage.removeItem(cacheKey);
      }
      
      return true;
    } catch (error) {
      console.error('Error marking packages as picked up:', error);
      throw error;
    }
  }
}

export const deliveryService = new DeliveryService();

