import { mockDataService } from './mockDataService';
import { Route, RouteResponse } from '../types';

class RouteService {
  /**
   * Get all routes for the current driver
   */
  async getDriverRoutes(): Promise<Route[]> {
    try {
      const currentUserId = mockDataService.getCurrentUserId();
      const response = await mockDataService.getRoutesByDriverId(currentUserId);
      
      if (response.success) {
        return response.data;
      }
      throw new Error(response.message);
    } catch (error) {
      console.error('Error fetching driver routes:', error);
      throw error;
    }
  }

  /**
   * Get a specific route by ID
   */
  async getRouteById(routeId: string): Promise<Route | null> {
    try {
      return await mockDataService.getRouteById(routeId);
    } catch (error) {
      console.error('Error fetching route:', error);
      throw error;
    }
  }

  /**
   * Start a route (update status to in_progress)
   */
  async startRoute(routeId: string): Promise<boolean> {
    try {
      const success = await mockDataService.updateRouteStatus(routeId, 'in_progress');
      if (success) {
        console.log(`Route ${routeId} started successfully`);
      }
      return success;
    } catch (error) {
      console.error('Error starting route:', error);
      throw error;
    }
  }

  /**
   * Complete a route (update status to completed)
   */
  async completeRoute(routeId: string): Promise<boolean> {
    try {
      const success = await mockDataService.updateRouteStatus(routeId, 'completed');
      if (success) {
        console.log(`Route ${routeId} completed successfully`);
      }
      return success;
    } catch (error) {
      console.error('Error completing route:', error);
      throw error;
    }
  }

  /**
   * Cancel a route (update status to cancelled)
   */
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

  /**
   * Get packages for a specific route
   */
  async getRoutePackages(routeId: string) {
    try {
      return await mockDataService.getPackagesByRouteId(routeId);
    } catch (error) {
      console.error('Error fetching route packages:', error);
      throw error;
    }
  }

  /**
   * Update package status
   */
  async updatePackageStatus(packageId: string, status: 'pending' | 'picked_up' | 'delivered'): Promise<boolean> {
    try {
      return await mockDataService.updatePackageStatus(packageId, status);
    } catch (error) {
      console.error('Error updating package status:', error);
      throw error;
    }
  }

  /**
   * Save delivery progress (current package index) for a route
   * This should be stored in the database, but we'll also use localStorage as backup
   */
  async saveDeliveryProgress(routeId: string, currentPackageIndex: number): Promise<boolean> {
    try {
      // TODO: Implement backend endpoint to save delivery progress
      // For now, we'll just use localStorage
      // Backend endpoint: PUT /api/routes/{routeId}/delivery-progress
      // Body: { currentPackageIndex: number }
      
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
      // Don't throw - allow localStorage fallback
      return false;
    }
  }

  /**
   * Get delivery progress (current package index) for a route
   */
  async getDeliveryProgress(routeId: string): Promise<number | null> {
    try {
      // TODO: Implement backend endpoint to get delivery progress
      // Backend endpoint: GET /api/routes/{routeId}/delivery-progress
      
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

  /**
   * Get base URL for API calls
   */
  private getBaseUrl(): string {
    // Import apiConfig dynamically to avoid circular dependencies
    const apiConfig = require('../config/apiConfig').apiConfig;
    return apiConfig.baseURL;
  }

  /**
   * Get routes by status
   */
  async getRoutesByStatus(status: Route['status']): Promise<Route[]> {
    try {
      const allRoutes = await this.getDriverRoutes();
      return allRoutes.filter(route => route.status === status);
    } catch (error) {
      console.error('Error fetching routes by status:', error);
      throw error;
    }
  }

  /**
   * Get today's routes
   */
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

  /**
   * Get upcoming routes (future dates)
   */
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
