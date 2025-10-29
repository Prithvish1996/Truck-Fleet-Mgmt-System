import { Route, Truck, Driver, Package, RouteResponse, TruckResponse, RouteBreak } from '../types';
import { authService } from './authService';
import { processRouteWithBreaks } from './breakCalculationService';
import { IoHome } from "react-icons/io5";

// Mock data for drivers - using backend default users
const mockDrivers: Driver[] = [
  {
    id: 'driver@tfms.com', // Using email as ID to match backend
    name: 'Driver User',
    email: 'driver@tfms.com',
    phone: '+31 6 1234 5678',
    licenseNumber: 'DL123456789',
    licenseExpiry: '2025-12-31',
    status: 'active',
    currentRouteId: 'R1223-01'
  },
  {
    id: 'test@example.com', // Using email as ID to match backend
    name: 'Test Driver',
    email: 'test@example.com',
    phone: '+31 6 2345 6789',
    licenseNumber: 'DL987654321',
    licenseExpiry: '2026-03-15',
    status: 'active',
    currentRouteId: 'R3213-03'
  },
  {
    id: 'D003',
    name: 'Mike Wilson',
    email: 'mike.wilson@company.com',
    phone: '+31 6 3456 7890',
    licenseNumber: 'DL456789123',
    licenseExpiry: '2025-08-20',
    status: 'active'
  }
];

// Mock data for trucks
const mockTrucks: Truck[] = [
  {
    id: 'T321',
    licensePlate: '12-ABC-34',
    model: 'Mercedes Sprinter',
    capacity: 3500,
    fuelType: 'diesel',
    status: 'in_use',
    currentLocation: {
      latitude: 52.3676,
      longitude: 4.9041
    },
    driverId: 'driver@tfms.com',
    lastMaintenanceDate: '2024-01-15',
    nextMaintenanceDate: '2024-07-15'
  },
  {
    id: 'T5441',
    licensePlate: '45-DEF-67',
    model: 'Ford Transit',
    capacity: 2800,
    fuelType: 'diesel',
    status: 'in_use',
    currentLocation: {
      latitude: 52.3702,
      longitude: 4.8952
    },
    driverId: 'test@example.com',
    lastMaintenanceDate: '2024-02-10',
    nextMaintenanceDate: '2024-08-10'
  },
  {
    id: 'T789',
    licensePlate: '78-GHI-90',
    model: 'Volkswagen Crafter',
    capacity: 4000,
    fuelType: 'electric',
    status: 'available',
    lastMaintenanceDate: '2024-03-01',
    nextMaintenanceDate: '2024-09-01'
  }
];

// Mock data for packages
const mockPackages: Package[] = [
  // Packages for Route R1223-01
  {
    id: 'P001',
    name: 'Electronics Package',
    latitude: 52.3676,
    longitude: 4.9041,
    address: 'Damrak 1',
    city: 'Amsterdam',
    postalCode: '1012 LP',
    weight: 2.5,
    status: 'pending',
    deliveryInstructions: 'Leave at front door if no answer',
    recipientName: 'Jan de Vries',
    recipientPhone: '+31 6 1111 2222',
    estimatedTravelTime: 2400 // 40 min
  },
  {
    id: 'P002',
    name: 'Furniture Package',
    latitude: 52.3702,
    longitude: 4.8952,
    address: 'Kalverstraat 92',
    city: 'Amsterdam',
    postalCode: '1012 PH',
    weight: 15.0,
    status: 'pending',
    deliveryInstructions: 'Call recipient before delivery',
    recipientName: 'Maria Garcia',
    recipientPhone: '+31 6 3333 4444',
    estimatedTravelTime: 2100 // 35 min
  },
  {
    id: 'P003',
    name: 'Clothing Package',
    latitude: 52.3731,
    longitude: 4.8903,
    address: 'Nieuwendijk 123',
    city: 'Amsterdam',
    postalCode: '1012 MD',
    weight: 1.2,
    status: 'pending',
    deliveryInstructions: 'Deliver to reception',
    recipientName: 'Ahmed Hassan',
    recipientPhone: '+31 6 5555 6666',
    estimatedTravelTime: 1800 // 30 min
  },
  {
    id: 'P004',
    name: 'Books Package',
    latitude: 52.3756,
    longitude: 4.8854,
    address: 'Spui 25',
    city: 'Amsterdam',
    postalCode: '1012 WX',
    weight: 3.8,
    status: 'pending',
    deliveryInstructions: 'Handle with care',
    recipientName: 'Lisa Anderson',
    recipientPhone: '+31 6 7777 8888',
    estimatedTravelTime: 2700 // 45 min
  },
  {
    id: 'P005',
    name: 'Sports Equipment',
    latitude: 52.3781,
    longitude: 4.8805,
    address: 'Leidseplein 12',
    city: 'Amsterdam',
    postalCode: '1017 PT',
    weight: 8.5,
    status: 'pending',
    deliveryInstructions: 'Deliver to side entrance',
    recipientName: 'Tom Brown',
    recipientPhone: '+31 6 9999 0000',
    estimatedTravelTime: 3000 // 50 min
  },
  // Packages for Route R3213-03
  {
    id: 'P006',
    name: 'Office Supplies',
    latitude: 52.3806,
    longitude: 4.8756,
    address: 'Rembrandtplein 15',
    city: 'Amsterdam',
    postalCode: '1017 CT',
    weight: 4.2,
    status: 'pending',
    deliveryInstructions: 'Deliver to office building',
    recipientName: 'Emma Wilson',
    recipientPhone: '+31 6 1111 3333',
    estimatedTravelTime: 1080
  },
  {
    id: 'P007',
    name: 'Medical Supplies',
    latitude: 52.3831,
    longitude: 4.8707,
    address: 'Museumplein 1',
    city: 'Amsterdam',
    postalCode: '1071 DJ',
    weight: 6.7,
    status: 'pending',
    deliveryInstructions: 'Temperature controlled delivery',
    recipientName: 'Dr. Johnson',
    recipientPhone: '+31 6 2222 4444',
    estimatedTravelTime: 1320
  },
  {
    id: 'P008',
    name: 'Food Package',
    latitude: 52.3856,
    longitude: 4.8658,
    address: 'Vondelpark 1',
    city: 'Amsterdam',
    postalCode: '1071 AA',
    weight: 2.1,
    status: 'pending',
    deliveryInstructions: 'Keep refrigerated',
    recipientName: 'Sophie Martin',
    recipientPhone: '+31 6 3333 5555',
    estimatedTravelTime: 540
  }
];

// Mock data for routes (breaks will be calculated automatically)
const rawMockRoutes: Omit<Route, 'breaks'>[] = [
  {
    id: 'R1223-01',
    truckId: 'T321',
    driverId: 'driver@tfms.com',
    packages: mockPackages.filter(p => ['P001', 'P002', 'P003', 'P004', 'P005', 'P006', 'P007', 'P008'].includes(p.id)),
    startTime: '07:30',
    duration: '8 hours',
    date: new Date().toISOString().split('T')[0],
    status: 'scheduled',
    totalDistance: 45.2,
    estimatedFuelCost: 28.50,
    priority: 'high'
  },
  {
    id: 'R3213-03',
    truckId: 'T5441',
    driverId: 'test@example.com',
    packages: mockPackages.filter(p => ['P006', 'P007', 'P008'].includes(p.id)),
    startTime: '14:00',
    duration: '3 hours',
    date: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    status: 'scheduled',
    totalDistance: 18.7,
    estimatedFuelCost: 12.30,
    priority: 'medium'
  },
  {
    id: 'R4567-02',
    truckId: 'T789',
    driverId: 'driver@tfms.com',
    packages: mockPackages.filter(p => ['P006', 'P007', 'P008'].includes(p.id)),
    startTime: '09:00',
    duration: '6 hours',
    date: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    status: 'scheduled',
    totalDistance: 0,
    estimatedFuelCost: 0,
    priority: 'low'
  }
];

// Process routes to add automatically calculated breaks
const mockRoutes: Route[] = rawMockRoutes.map(processRouteWithBreaks);

class MockDataService {
  private routes: Route[] = [...mockRoutes];
  private trucks: Truck[] = [...mockTrucks];
  private drivers: Driver[] = [...mockDrivers];
  private packages: Package[] = [...mockPackages];

  // Get routes for a specific driver
  async getRoutesByDriverId(driverId: string): Promise<RouteResponse> {
    const driverRoutes = this.routes.filter(route => route.driverId === driverId);
    
    return {
      success: true,
      message: `Found ${driverRoutes.length} routes for driver ${driverId}`,
      data: driverRoutes,
      timestamp: new Date().toISOString()
    };
  }

  // Get all routes
  async getAllRoutes(): Promise<RouteResponse> {
    return {
      success: true,
      message: `Found ${this.routes.length} routes`,
      data: this.routes,
      timestamp: new Date().toISOString()
    };
  }

  // Get a specific route by ID
  async getRouteById(routeId: string): Promise<Route | null> {
    return this.routes.find(route => route.id === routeId) || null;
  }

  // Get trucks
  async getTrucks(): Promise<TruckResponse> {
    return {
      success: true,
      message: `Found ${this.trucks.length} trucks`,
      data: this.trucks,
      timestamp: new Date().toISOString()
    };
  }

  // Get trucks by driver ID
  async getTrucksByDriverId(driverId: string): Promise<TruckResponse> {
    const driverTrucks = this.trucks.filter(truck => truck.driverId === driverId);
    
    return {
      success: true,
      message: `Found ${driverTrucks.length} trucks for driver ${driverId}`,
      data: driverTrucks,
      timestamp: new Date().toISOString()
    };
  }

  // Get packages for a specific route
  async getPackagesByRouteId(routeId: string): Promise<Package[]> {
    const route = this.routes.find(r => r.id === routeId);
    return route ? route.packages : [];
  }

  // Update route status
  async updateRouteStatus(routeId: string, status: Route['status']): Promise<boolean> {
    const route = this.routes.find(r => r.id === routeId);
    if (route) {
      route.status = status;
      return true;
    }
    return false;
  }

  // Update package status
  async updatePackageStatus(packageId: string, status: Package['status']): Promise<boolean> {
    const package_ = this.packages.find(p => p.id === packageId);
    if (package_) {
      package_.status = status;
      return true;
    }
    return false;
  }

  // Get driver by ID
  async getDriverById(driverId: string): Promise<Driver | null> {
    return this.drivers.find(driver => driver.id === driverId) || null;
  }

  // Get current user ID (mock implementation)
  getCurrentUserId(): string {
    // Try to get user from auth service
    const userEmail = authService.getUserEmail();
    if (userEmail) {
      return userEmail;
    }
    
    // Fallback to default driver for development
    return 'driver@tfms.com';
  }

  // Add a new route (for testing)
  async addRoute(route: Omit<Route, 'id'>): Promise<Route> {
    const newRoute: Route = {
      ...route,
      id: `R${Date.now()}-${Math.floor(Math.random() * 100)}`
    };
    this.routes.push(newRoute);
    return newRoute;
  }

  // Add a new package to a route
  async addPackageToRoute(routeId: string, package_: Omit<Package, 'id'>): Promise<Package> {
    const newPackage: Package = {
      ...package_,
      id: `P${Date.now()}-${Math.floor(Math.random() * 100)}`
    };
    
    const route = this.routes.find(r => r.id === routeId);
    if (route) {
      route.packages.push(newPackage);
      this.packages.push(newPackage);
    }
    
    return newPackage;
  }
}

export const mockDataService = new MockDataService();
