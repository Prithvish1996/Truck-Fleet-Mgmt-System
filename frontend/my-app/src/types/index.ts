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
  routeId?: number;
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

export interface Parcel {
  parcelId: number;
  name: string;
  weight: number;
  volume: number;
  status: string;
  createdAt: string | null;
  recipientName: string;
  recipientPhone: string;
  deliveryInstructions: string;
  deliveryAddress: string;
  deliveryPostalCode: string;
  deliveryCity: string;
  deliveryLatitude: number;
  deliveryLongitude: number;
  warehouseId: number;
  warehouseAddress: string;
  warehousePostalCode: string;
  warehouseCity: string;
  warehouseLatitude: number;
  warehouseLongitude: number;
}

export interface RouteStop {
  stopId: number;
  parcelsToDeliver: Parcel[];
  priority: number;
  stopType: string;
}

export interface RouteData {
  routeId?: number;
  driverId: number;
  driverUserName: string;
  driverEmail: string;
  truckId: number;
  truckPlateNumber: string;
  depotId: number | null;
  depotName: string | null;
  routeStops: RouteStop[];
  totalDistance: number;
  totalTransportTime: number;
  note: string;
  startTime: string;
  status: string;
  estimatedFuelCost: number;
  duration: string;
  driverAvailable: boolean;
}

export interface RouteByDriverResponse {
  success: boolean;
  message: string;
  data: {
    routes: RouteData[];
  };
  timestamp: string;
}
