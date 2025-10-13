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
