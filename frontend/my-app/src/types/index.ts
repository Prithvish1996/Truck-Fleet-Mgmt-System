// Type definitions for the TFMS application

export interface Package {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  address: string;
  city: string;
  postalCode: string;
  weight: number;
  status: 'pending' | 'picked_up' | 'delivered';
  deliveryInstructions?: string;
  recipientName: string;
  recipientPhone: string;
  estimatedTravelTime?: number;
}

export interface RouteBreak {
  id: string;
  type: 'break';
  name: string;
  duration: string;
  scheduledTime?: string;
  location?: {
    latitude: number;
    longitude: number;
    address?: string;
    city?: string;
    postalCode?: string;
  };
  packagesBetween?: {
    beforePackage: string;
    afterPackage: string;
  };
}

export interface Route {
  id: string;
  truckId: string;
  driverId: string;
  packages: Package[];
  breaks: RouteBreak[];
  startTime: string;
  duration: string;
  date: string;
  status: 'scheduled' | 'in_progress' | 'completed' | 'cancelled';
  totalDistance: number;
  estimatedFuelCost: number;
  priority: 'low' | 'medium' | 'high';
}

export interface Truck {
  id: string;
  licensePlate: string;
  model: string;
  capacity: number;
  fuelType: 'diesel' | 'electric' | 'hybrid';
  status: 'available' | 'in_use' | 'maintenance' | 'out_of_service';
  currentLocation?: {
    latitude: number;
    longitude: number;
  };
  driverId?: string;
  lastMaintenanceDate: string;
  nextMaintenanceDate: string;
}

export interface Driver {
  id: string;
  name: string;
  email: string;
  phone: string;
  licenseNumber: string;
  licenseExpiry: string;
  status: 'active' | 'inactive' | 'on_leave';
  currentRouteId?: string;
}

export interface RouteResponse {
  success: boolean;
  message: string;
  data: Route[];
  timestamp: string;
}

export interface TruckResponse {
  success: boolean;
  message: string;
  data: Truck[];
  timestamp: string;
}
