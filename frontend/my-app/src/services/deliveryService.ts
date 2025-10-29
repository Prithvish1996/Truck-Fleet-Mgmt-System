import { routeService } from './routeService';
import { googleMapsService } from './googleMapsService';
import { Package } from '../types';

export type DeliveryState = 
  | 'loading'
  | 'waiting_location'
  | 'showing_navigation'
  | 'waiting_confirmation'
  | 'completed'
  | 'error';

class DeliveryService {
  async loadPackages(routeId: string): Promise<Package[]> {
    const routePackages = await routeService.getRoutePackages(routeId) as Package[] | { data?: Package[]; packages?: Package[] };
    
    const packagesArray = Array.isArray(routePackages) 
      ? routePackages 
      : ((routePackages as { data?: Package[]; packages?: Package[] }).data || (routePackages as { data?: Package[]; packages?: Package[] }).packages || []);
    
    return packagesArray.filter(
      (pkg: Package) => pkg.status === 'pending' || pkg.status === 'picked_up'
    );
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
    confirmed: boolean
  ): Promise<{ newStatus: 'pending' | 'delivered' }> {
    const newStatus = confirmed ? 'delivered' : 'pending';
    await routeService.updatePackageStatus(packageId, newStatus);
    return { newStatus };
  }
}

export const deliveryService = new DeliveryService();

