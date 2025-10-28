import { graphhopperService, GraphhopperRoute } from './graphhopperService';
import { routeCacheService } from './routeCacheService';
import { Route, Package } from '../types';

export interface LocationData {
  latitude: number;
  longitude: number;
  accuracy?: number;
  timestamp: number;
}

export interface NavigationState {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  currentRoute: Route | null;
  firstPackage: Package | null;
  routeData: GraphhopperRoute | null;
  loading: boolean;
  error: string | null;
  mapReady: boolean;
}

export interface NavigationCallbacks {
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate: (route: GraphhopperRoute | null) => void;
  onStateChange: (state: Partial<NavigationState>) => void;
}

class NavigationService {
  private watchId: number | null = null;
  private routeFetchTimeout: NodeJS.Timeout | null = null;
  private lastLocation: [number, number] | null = null;
  private callbacks: NavigationCallbacks | null = null;
  private state: NavigationState = {
    userLocation: null,
    destination: null,
    currentRoute: null,
    firstPackage: null,
    routeData: null,
    loading: true,
    error: null,
    mapReady: false
  };

  constructor() {
    this.throttledLocationUpdate = this.throttle(this.handleLocationUpdate.bind(this), 2000);
  }

  setCallbacks(callbacks: NavigationCallbacks) {
    this.callbacks = callbacks;
  }

  private throttle<T extends (...args: any[]) => any>(
    func: T,
    delay: number
  ): (...args: Parameters<T>) => void {
    let lastCall = 0;
    return (...args: Parameters<T>) => {
      const now = Date.now();
      if (now - lastCall >= delay) {
        lastCall = now;
        func(...args);
      }
    };
  }

  private updateState(updates: Partial<NavigationState>) {
    this.state = { ...this.state, ...updates };
    if (this.callbacks) {
      this.callbacks.onStateChange(updates);
    }
  }

  private handleLocationUpdate(position: GeolocationPosition) {
    const { latitude, longitude } = position.coords;
    const newLocation: [number, number] = [latitude, longitude];
    
    // Check if location has changed significantly (at least 10 meters)
    if (this.lastLocation) {
      const distance = Math.sqrt(
        Math.pow(newLocation[0] - this.lastLocation[0], 2) +
        Math.pow(newLocation[1] - this.lastLocation[1], 2)
      );
      // Skip if distance is less than ~10 meters (roughly 0.0001 degrees)
      if (distance < 0.0001) {
        return;
      }
    }
    
    this.lastLocation = newLocation;
    this.updateState({ userLocation: newLocation });
    
    if (this.callbacks) {
      this.callbacks.onLocationUpdate(newLocation);
    }
  }

  private throttledLocationUpdate: (position: GeolocationPosition) => void;

  async startLocationTracking(): Promise<void> {
    if (!navigator.geolocation) {
      throw new Error('Geolocation is not supported by this browser');
    }

    return new Promise((resolve, reject) => {
      this.watchId = navigator.geolocation.watchPosition(
        (position) => {
          this.throttledLocationUpdate(position);
          resolve();
        },
        (error) => {
          console.error('Geolocation error:', error);
          this.updateState({ error: 'Location access denied' });
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 5000
        }
      );
    });
  }

  stopLocationTracking(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }

  async requestLocationPermission(): Promise<[number, number]> {
    if (!navigator.geolocation) {
      throw new Error('Geolocation is not supported by this browser');
    }

    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location: [number, number] = [position.coords.latitude, position.coords.longitude];
          this.updateState({ userLocation: location });
          resolve(location);
        },
        (error) => {
          console.error('Geolocation error:', error);
          this.updateState({ error: 'Location access denied' });
          reject(error);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 5000
        }
      );
    });
  }

  async fetchRoute(userLocation: [number, number], destination: [number, number]): Promise<GraphhopperRoute> {
    // Handle destination changes
    routeCacheService.clearCacheOnDestinationChange(destination);

    // Check if we have a cached route
    const cachedRoute = routeCacheService.getCachedRoute(userLocation, destination);
    if (cachedRoute) {
      this.updateState({ routeData: cachedRoute });
      if (this.callbacks) {
        this.callbacks.onRouteUpdate(cachedRoute);
      }
      return cachedRoute;
    }

    // Clear any existing timeout
    if (this.routeFetchTimeout) {
      clearTimeout(this.routeFetchTimeout);
    }

    // Debounce route fetching to prevent rapid API calls
    return new Promise((resolve, reject) => {
      this.routeFetchTimeout = setTimeout(async () => {
        try {
          console.log('Fetching new route from Graphhopper...');
          const route = await graphhopperService.getRoute({
            points: [userLocation, destination],
            vehicle: 'car',
            instructions: true,
            points_encoded: true
          });

          // Cache the route
          routeCacheService.cacheRoute(userLocation, destination, route);
          this.updateState({ routeData: route });
          
          if (this.callbacks) {
            this.callbacks.onRouteUpdate(route);
          }
          
          resolve(route);
        } catch (error) {
          console.error('Error fetching route from Graphhopper:', error);
          reject(error);
        }
      }, 500); // Debounce delay
    });
  }

  async loadRouteData(): Promise<void> {
    try {
      this.updateState({ loading: true });
      
      // Import routeService dynamically to avoid circular dependencies
      const { routeService } = await import('./routeService');
      
      // Get the first in-progress route or the first scheduled route
      const routes = await routeService.getDriverRoutes();
      const activeRoute = routes.find(route => route.status === 'in_progress') || 
                         routes.find(route => route.status === 'scheduled');
      
      if (activeRoute && activeRoute.packages.length > 0) {
        const firstPkg = activeRoute.packages[0];
        this.updateState({
          currentRoute: activeRoute,
          firstPackage: firstPkg,
          destination: [firstPkg.latitude, firstPkg.longitude]
        });
      } else {
        this.updateState({ error: 'No active route or packages found' });
      }
    } catch (err) {
      console.error('Error loading route data:', err);
      this.updateState({ error: 'Failed to load route data' });
    } finally {
      this.updateState({ loading: false });
    }
  }

  setMapReady(ready: boolean): void {
    this.updateState({ mapReady: ready });
  }

  getState(): NavigationState {
    return { ...this.state };
  }

  cleanup(): void {
    this.stopLocationTracking();
    if (this.routeFetchTimeout) {
      clearTimeout(this.routeFetchTimeout);
      this.routeFetchTimeout = null;
    }
  }
}

// Export a singleton instance
export const navigationService = new NavigationService();

