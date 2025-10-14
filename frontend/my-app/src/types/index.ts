// Type definitions for the TFMS application

export interface Package {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  address: string;
  city: string;
  postalCode: string;
  weight: number; // in kg
  status: 'pending' | 'picked_up' | 'delivered';
  deliveryInstructions?: string;
  recipientName: string;
  recipientPhone: string;
}

export interface RouteBreak {
  id: string;
  type: 'break';
  name: string;
  duration: string; // e.g., "15 min", "30 min"
  scheduledTime?: string; // HH:MM format - when the break is scheduled
  location?: {
    latitude: number;
    longitude: number;
    address?: string;
    city?: string;
    postalCode?: string;
  };
  packagesBetween?: {
    beforePackage: string; // ID of the package before the break
    afterPackage: string; // ID of the package after the break
  };
}

export interface Route {
  id: string;
  truckId: string;
  driverId: string;
  packages: Package[];
  breaks: RouteBreak[]; // Breaks scheduled by the backend
  startTime: string; // HH:MM format
  duration: string; // e.g., "8 hours"
  date: string; // YYYY-MM-DD format
  status: 'scheduled' | 'in_progress' | 'completed' | 'cancelled';
  totalDistance: number; // in km
  estimatedFuelCost: number; // in euros
  priority: 'low' | 'medium' | 'high';
}

export interface Truck {
  id: string;
  licensePlate: string;
  model: string;
  capacity: number; // in kg
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
