import { mockDataService } from './mockDataService';
import { Truck, TruckResponse } from '../types';

class TruckService {
  async getAllTrucks(): Promise<Truck[]> {
    try {
      const response = await mockDataService.getTrucks();
      if (response.success) {
        return response.data;
      }
      throw new Error(response.message);
    } catch (error) {
      console.error('Error fetching trucks:', error);
      throw error;
    }
  }

  async getDriverTrucks(): Promise<Truck[]> {
    try {
      const currentUserId = mockDataService.getCurrentUserId();
      const response = await mockDataService.getTrucksByDriverId(currentUserId);
      
      if (response.success) {
        return response.data;
      }
      throw new Error(response.message);
    } catch (error) {
      console.error('Error fetching driver trucks:', error);
      throw error;
    }
  }

  async getTruckById(truckId: string): Promise<Truck | null> {
    try {
      const trucks = await this.getAllTrucks();
      return trucks.find(truck => truck.id === truckId) || null;
    } catch (error) {
      console.error('Error fetching truck by ID:', error);
      throw error;
    }
  }

  async getTrucksByStatus(status: Truck['status']): Promise<Truck[]> {
    try {
      const allTrucks = await this.getAllTrucks();
      return allTrucks.filter(truck => truck.status === status);
    } catch (error) {
      console.error('Error fetching trucks by status:', error);
      throw error;
    }
  }

  async getAvailableTrucks(): Promise<Truck[]> {
    try {
      return await this.getTrucksByStatus('available');
    } catch (error) {
      console.error('Error fetching available trucks:', error);
      throw error;
    }
  }

  async getTrucksInUse(): Promise<Truck[]> {
    try {
      return await this.getTrucksByStatus('in_use');
    } catch (error) {
      console.error('Error fetching trucks in use:', error);
      throw error;
    }
  }

  async getTrucksInMaintenance(): Promise<Truck[]> {
    try {
      return await this.getTrucksByStatus('maintenance');
    } catch (error) {
      console.error('Error fetching trucks in maintenance:', error);
      throw error;
    }
  }

  async getTrucksOutOfService(): Promise<Truck[]> {
    try {
      return await this.getTrucksByStatus('out_of_service');
    } catch (error) {
      console.error('Error fetching trucks out of service:', error);
      throw error;
    }
  }

  async getTrucksByFuelType(fuelType: Truck['fuelType']): Promise<Truck[]> {
    try {
      const allTrucks = await this.getAllTrucks();
      return allTrucks.filter(truck => truck.fuelType === fuelType);
    } catch (error) {
      console.error('Error fetching trucks by fuel type:', error);
      throw error;
    }
  }

  async getTrucksByCapacity(minCapacity: number, maxCapacity: number): Promise<Truck[]> {
    try {
      const allTrucks = await this.getAllTrucks();
      return allTrucks.filter(truck => 
        truck.capacity >= minCapacity && truck.capacity <= maxCapacity
      );
    } catch (error) {
      console.error('Error fetching trucks by capacity:', error);
      throw error;
    }
  }

  async getTrucksNeedingMaintenance(daysAhead: number = 30): Promise<Truck[]> {
    try {
      const allTrucks = await this.getAllTrucks();
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + daysAhead);
      
      return allTrucks.filter(truck => {
        const maintenanceDate = new Date(truck.nextMaintenanceDate);
        return maintenanceDate <= futureDate;
      });
    } catch (error) {
      console.error('Error fetching trucks needing maintenance:', error);
      throw error;
    }
  }

  async getTruckStatistics(): Promise<{
    total: number;
    available: number;
    inUse: number;
    maintenance: number;
    outOfService: number;
    diesel: number;
    electric: number;
    hybrid: number;
  }> {
    try {
      const allTrucks = await this.getAllTrucks();
      
      return {
        total: allTrucks.length,
        available: allTrucks.filter(t => t.status === 'available').length,
        inUse: allTrucks.filter(t => t.status === 'in_use').length,
        maintenance: allTrucks.filter(t => t.status === 'maintenance').length,
        outOfService: allTrucks.filter(t => t.status === 'out_of_service').length,
        diesel: allTrucks.filter(t => t.fuelType === 'diesel').length,
        electric: allTrucks.filter(t => t.fuelType === 'electric').length,
        hybrid: allTrucks.filter(t => t.fuelType === 'hybrid').length,
      };
    } catch (error) {
      console.error('Error fetching truck statistics:', error);
      throw error;
    }
  }
}

export const truckService = new TruckService();
