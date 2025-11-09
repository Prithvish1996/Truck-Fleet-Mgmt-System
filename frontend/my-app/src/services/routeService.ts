import { mockDataService } from './mockDataService';
import { Route, RouteResponse, RouteByDriverResponse, RouteData, Parcel, RouteStop } from '../types';
import { authService } from './authService';
import { apiConfig } from '../config/apiConfig';
import { dateTimeService } from './dateTimeService';

class RouteService {
  private mapRouteDataToRoute(routeData: RouteData, index: number): Route {
    const packages: Route['packages'] = [];
    
    const sortedStops = [...routeData.routeStops].sort((a, b) => a.priority - b.priority);
    
    sortedStops.forEach((stop: RouteStop) => {
      stop.parcelsToDeliver.forEach((parcel: Parcel) => {
        packages.push({
          id: parcel.parcelId.toString(),
          name: parcel.name,
          latitude: parcel.deliveryLatitude,
          longitude: parcel.deliveryLongitude,
          address: parcel.deliveryAddress,
          city: parcel.deliveryCity,
          postalCode: parcel.deliveryPostalCode,
          weight: parcel.weight,
          status: this.mapParcelStatusToPackageStatus(parcel.status),
          deliveryInstructions: parcel.deliveryInstructions,
          recipientName: parcel.recipientName,
          recipientPhone: parcel.recipientPhone,
        });
      });
    });

    const { date: startDate } = 
      dateTimeService.convertTimeStringToDateTimeAndDate(routeData.startTime);
    const startTime = dateTimeService.formatTimeString(routeData.startTime);

    return {
      id: `route-${routeData.driverId}-${index}`,
      routeId: routeData.routeId,
      truckId: routeData.truckId.toString(),
      driverId: routeData.driverId.toString(),
      packages,
      breaks: [],
      startTime: startTime,
      duration: routeData.duration,
      date: startDate,
      status: this.mapApiStatusToRouteStatus(routeData.status),
      totalDistance: routeData.totalDistance,
      estimatedFuelCost: routeData.estimatedFuelCost,
      priority: 'medium' as const,
    };
  }

  getNextStop(routeData: RouteData): { stopId: number; priority: number; stopType: string; packages: Parcel[] } | null {
    const sortedStops = [...routeData.routeStops].sort((a, b) => a.priority - b.priority);
    
    for (const stop of sortedStops) {
      const undeliveredParcels = stop.parcelsToDeliver.filter(
        parcel => parcel.status.toUpperCase() !== 'DELIVERED'
      );
      
      if (undeliveredParcels.length > 0) {
        return {
          stopId: stop.stopId,
          priority: stop.priority,
          stopType: stop.stopType,
          packages: undeliveredParcels
        };
      }
    }
    
    return null;
  }

  private mapParcelStatusToPackageStatus(apiStatus: string): 'pending' | 'picked_up' | 'delivered' {
    const statusUpper = apiStatus.toUpperCase();
    if (statusUpper === 'DELIVERED') return 'delivered';
    return 'pending';
  }

  private mapApiStatusToRouteStatus(apiStatus: string): 'scheduled' | 'in_progress' | 'completed' | 'cancelled' {
    const statusLower = apiStatus.toLowerCase();
    if (statusLower === 'completed') return 'completed';
    if (statusLower === 'in_progress' || statusLower === 'assigned') return 'in_progress';
    if (statusLower === 'cancelled') return 'cancelled';
    return 'scheduled';
  }

  async getDriverRoutes(forceRefresh: boolean = false): Promise<Route[]> {
    try {
      const driverId = authService.getDriverId();
      if (!driverId) {
        throw new Error('Driver ID not found in token. Please log out and log back in to get a new token with your user ID.');
      }

      const cacheKey = `driver_routes_${driverId}`;
      
      if (!forceRefresh) {
        const cached = localStorage.getItem(cacheKey);
        if (cached) {
          try {
            const cachedRoutes = JSON.parse(cached) as Route[];
            const cacheTime = localStorage.getItem(`${cacheKey}_time`);
            if (cacheTime) {
              const age = Date.now() - parseInt(cacheTime, 10);
              if (age < 60000) {
                return cachedRoutes;
              }
            }
          } catch (e) {
            console.warn('Failed to parse cached routes', e);
          }
        }
      }

      let token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      token = token.trim();

      const response = await fetch(`${apiConfig.baseURL}/routes/driver/${driverId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch routes' }));
        const errorMessage = errorData.message || `Failed to fetch routes (Status: ${response.status})`;
        
        if (response.status === 401) {
          throw new Error(`${errorMessage}. Please log out and log back in to refresh your token.`);
        }
        
        throw new Error(errorMessage);
      }

      const apiResponse: RouteByDriverResponse = await response.json();
      
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch routes');
      }

      const routes = apiResponse.data.routes.map((routeData, index) => 
        this.mapRouteDataToRoute(routeData, index)
      );

      localStorage.setItem(cacheKey, JSON.stringify(routes));
      localStorage.setItem(`${cacheKey}_time`, Date.now().toString());

      return routes;
    } catch (error) {
      console.error('Error fetching driver routes:', error);
      throw error;
    }
  }

  async getRouteById(routeId: string, forceRefresh: boolean = false): Promise<Route | null> {
    try {
      const routes = await this.getDriverRoutes(forceRefresh);
      const route = routes.find(r => r.id === routeId);
      return route || null;
    } catch (error) {
      console.error('Error fetching route:', error);
      throw error;
    }
  }

  async startRoute(routeId: string): Promise<boolean> {
    try {
      console.log(`Route ${routeId} started`);
      return true;
    } catch (error) {
      console.error('Error starting route:', error);
      throw error;
    }
  }

  async completeRoute(routeId: string): Promise<boolean> {
    try {
      const routes = await this.getDriverRoutes(false);
      const route = routes.find(r => r.id === routeId);
      
      if (!route) {
        throw new Error(`Route ${routeId} not found`);
      }

      if (!route.routeId) {
        throw new Error('Route ID not found. Backend needs to include routeId in RouteResponseDto');
      }

      const token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      const backendRouteId = route.routeId;
      
      const response = await fetch(`${apiConfig.baseURL}/routes/status`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          routeId: backendRouteId,
          status: 'COMPLETED'
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to update route status' }));
        throw new Error(errorData.message || `Failed to update route status (Status: ${response.status})`);
      }

      const apiResponse = await response.json();
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to update route status');
      }

      const driverId = parseInt(route.driverId, 10);
      const cacheKey = `driver_routes_${driverId}`;
      localStorage.removeItem(cacheKey);
      localStorage.removeItem(`${cacheKey}_time`);

      return true;
    } catch (error) {
      console.error('Error completing route:', error);
      throw error;
    }
  }

  async cancelRoute(routeId: string): Promise<boolean> {
    try {
      const success = await mockDataService.updateRouteStatus(routeId, 'cancelled');
      if (success) {
        console.log(`Route ${routeId} cancelled successfully`);
      }
      return success;
    } catch (error) {
      console.error('Error cancelling route:', error);
      throw error;
    }
  }

  async getRoutePackages(routeId: string, forceRefresh: boolean = false) {
    try {
      const route = await this.getRouteById(routeId, forceRefresh);
      return route ? route.packages : [];
    } catch (error) {
      console.error('Error fetching route packages:', error);
      throw error;
    }
  }

  async updatePackageStatus(packageId: string, status: 'pending' | 'picked_up' | 'delivered'): Promise<boolean> {
    try {
      const driverId = authService.getDriverId();
      if (driverId) {
        const cacheKey = `driver_routes_${driverId}`;
        const cached = localStorage.getItem(cacheKey);
        if (cached) {
          try {
            const routes = JSON.parse(cached) as Route[];
            const updatedRoutes = routes.map(route => ({
              ...route,
              packages: route.packages.map(pkg =>
                pkg.id === packageId ? { ...pkg, status } : pkg
              )
            }));
            localStorage.setItem(cacheKey, JSON.stringify(updatedRoutes));
          } catch (e) {
            console.warn('Failed to update cached routes', e);
          }
        }
      }
      return await mockDataService.updatePackageStatus(packageId, status);
    } catch (error) {
      console.error('Error updating package status:', error);
      throw error;
    }
  }

  async saveDeliveryProgress(routeId: string, currentPackageIndex: number): Promise<boolean> {
    try {
      const url = `${this.getBaseUrl()}/routes/${routeId}/delivery-progress`;
      const response = await fetch(url, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ currentPackageIndex }),
        credentials: 'include'
      });

      if (response.ok) {
        return true;
      } else {
        console.warn('Failed to save delivery progress to backend, using localStorage only');
        return false;
      }
    } catch (error) {
      console.warn('Error saving delivery progress to backend:', error);
      return false;
    }
  }

  async getDeliveryProgress(routeId: string): Promise<number | null> {
    try {
      const url = `${this.getBaseUrl()}/routes/${routeId}/delivery-progress`;
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        return data.currentPackageIndex ?? null;
      }
      return null;
    } catch (error) {
      console.warn('Error loading delivery progress from backend:', error);
      return null;
    }
  }

  private getBaseUrl(): string {
    const apiConfig = require('../config/apiConfig').apiConfig;
    return apiConfig.baseURL;
  }

  async getRoutesByStatus(status: Route['status']): Promise<Route[]> {
    try {
      const allRoutes = await this.getDriverRoutes();
      return allRoutes.filter(route => route.status === status);
    } catch (error) {
      console.error('Error fetching routes by status:', error);
      throw error;
    }
  }

  async getTodaysRoutes(): Promise<Route[]> {
    try {
      const allRoutes = await this.getDriverRoutes();
      const today = new Date().toISOString().split('T')[0];
      return allRoutes.filter(route => route.date === today);
    } catch (error) {
      console.error('Error fetching today\'s routes:', error);
      throw error;
    }
  }

  async getUpcomingRoutes(): Promise<Route[]> {
    try {
      const allRoutes = await this.getDriverRoutes();
      const today = new Date().toISOString().split('T')[0];
      return allRoutes.filter(route => route.date > today);
    } catch (error) {
      console.error('Error fetching upcoming routes:', error);
      throw error;
    }
  }
}

export const routeService = new RouteService();
