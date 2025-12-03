import { routeService } from './routeService';
import { googleMapsService } from './googleMapsService';
import { Package } from '../types';
import { apiConfig } from '../config/apiConfig';
import { authService } from './authService';

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
        const response = await fetch(`${apiConfig.baseURL}/planner/parcel/status`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token.trim()}`,
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            parcelId: parseInt(packageId, 10),
            status: 'DELIVERED'
          }),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ message: 'Failed to update parcel status' }));
          throw new Error(errorData.message || `Failed to update parcel status (Status: ${response.status})`);
        }

        const apiResponse = await response.json();
        if (!apiResponse.success) {
          throw new Error(apiResponse.message || 'Failed to update parcel status');
        }
      } catch (error) {
        console.error('Error updating parcel status on backend:', error);
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
              const response = await fetch(`${apiConfig.baseURL}/routes/status`, {
                method: 'PUT',
                headers: {
                  'Authorization': `Bearer ${token.trim()}`,
                  'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                  routeId: route.routeId,
                  status: 'PARCELS_RETRIEVED'
                }),
              });

              if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: 'Failed to update route status' }));
                console.warn('Failed to update route status:', errorData.message || `Status: ${response.status}`);
              } else {
                const apiResponse = await response.json();
                if (apiResponse.success) {
                  const driverId = authService.getDriverId();
                  if (driverId) {
                    const cacheKey = `driver_routes_${driverId}`;
                    localStorage.removeItem(cacheKey);
                    localStorage.removeItem(`${cacheKey}_time`);
                  }
                }
              }
            } catch (error) {
              console.warn('Error updating route status to PARCELS_RETRIEVED:', error);
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

