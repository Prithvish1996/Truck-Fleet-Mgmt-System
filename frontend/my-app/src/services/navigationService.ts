import L from 'leaflet';

export interface NavigationConfig {
  navigationZoom: number;
  headingThreshold: number; // Minimum change in degrees to update rotation
  locationThreshold: number; // Minimum distance in meters to update position
  rotationSmoothness: number; // Smoothing factor for rotation (0-1)
}

export interface NavigationCallbacks {
  onLocationUpdate: (location: [number, number]) => void;
  onHeadingUpdate: (heading: number) => void;
  onError?: (error: string) => void;
}

export interface NavigationState {
  isNavigating: boolean;
  currentLocation: [number, number] | null;
  currentHeading: number;
  isLocationTracking: boolean;
  isHeadingTracking: boolean;
}

class NavigationService {
  private config: NavigationConfig;
  private callbacks: NavigationCallbacks | null = null;
  private map: L.Map | null = null;
  
  // State
  private state: NavigationState = {
    isNavigating: false,
    currentLocation: null,
    currentHeading: 0,
    isLocationTracking: false,
    isHeadingTracking: false
  };
  
  // Tracking
  private locationWatchId: number | null = null;
  private orientationWatchId: number | null = null;
  private lastLocation: [number, number] | null = null;
  private lastHeading: number = 0;
  private lastUpdate: number = 0;
  
  // Smoothing
  private targetHeading: number = 0;
  private currentMapHeading: number = 0;

  constructor(config: Partial<NavigationConfig> = {}) {
    this.config = {
      navigationZoom: 20,
      headingThreshold: 5, // 5 degrees
      locationThreshold: 10, // 10 meters
      rotationSmoothness: 0.1, // Smooth rotation
      ...config
    };
  }

  /**
   * Initialize the navigation service with a Leaflet map instance
   */
  initialize(map: L.Map): void {
    this.map = map;
    console.log('NavigationService initialized');
  }

  /**
   * Set callbacks for the service
   */
  setCallbacks(callbacks: NavigationCallbacks): void {
    this.callbacks = callbacks;
  }

  /**
   * Start navigation mode
   */
  startNavigation(): void {
    if (this.state.isNavigating) {
      console.log('Navigation already active');
      return;
    }

    this.state.isNavigating = true;
    this.startLocationTracking();
    this.startHeadingTracking();
    
    console.log('Navigation started');
  }

  /**
   * Stop navigation mode
   */
  stopNavigation(): void {
    if (!this.state.isNavigating) {
      return;
    }

    this.state.isNavigating = false;
    this.stopLocationTracking();
    this.stopHeadingTracking();
    
    // Reset map rotation
    if (this.map) {
      if ((this.map as any).setBearing) {
        (this.map as any).setBearing(0);
      } else {
        // Fallback: reset map container rotation
        const container = this.map.getContainer();
        container.style.transform = 'rotate(0deg)';
      }
    }
    
    console.log('Navigation stopped');
  }

  /**
   * Start location tracking for navigation
   */
  private startLocationTracking(): void {
    if (!navigator.geolocation) {
      this.callbacks?.onError?.('Geolocation is not supported');
      return;
    }

    if (this.locationWatchId !== null) {
      return;
    }

    this.locationWatchId = navigator.geolocation.watchPosition(
      (position) => this.handleLocationUpdate(position),
      (error) => this.handleLocationError(error),
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 1000 // More frequent updates for navigation
      }
    );

    this.state.isLocationTracking = true;
    console.log('Location tracking started for navigation');
  }

  /**
   * Stop location tracking
   */
  private stopLocationTracking(): void {
    if (this.locationWatchId !== null) {
      navigator.geolocation.clearWatch(this.locationWatchId);
      this.locationWatchId = null;
    }
    this.state.isLocationTracking = false;
    console.log('Location tracking stopped');
  }

  /**
   * Start heading tracking using device orientation
   */
  private startHeadingTracking(): void {
    if (!window.DeviceOrientationEvent) {
      console.warn('Device orientation not supported, using GPS heading');
      this.startGPSHeadingTracking();
      return;
    }

    // Request permission for iOS 13+
    if (typeof (DeviceOrientationEvent as any).requestPermission === 'function') {
      (DeviceOrientationEvent as any).requestPermission()
        .then((response: string) => {
          if (response === 'granted') {
            this.setupOrientationTracking();
          } else {
            console.warn('Orientation permission denied, using GPS heading');
            this.startGPSHeadingTracking();
          }
        })
        .catch(() => {
          console.warn('Failed to request orientation permission, using GPS heading');
          this.startGPSHeadingTracking();
        });
    } else {
      this.setupOrientationTracking();
    }
  }

  /**
   * Setup device orientation tracking
   */
  private setupOrientationTracking(): void {
    const handleOrientation = (event: DeviceOrientationEvent) => {
      if (event.alpha !== null) {
        // Convert device orientation to map heading
        let heading = event.alpha;
        
        // Adjust for different device orientations
        if (event.gamma !== null && event.beta !== null) {
          // More sophisticated heading calculation could be added here
          heading = event.alpha;
        }
        
        this.updateHeading(heading);
      }
    };

    window.addEventListener('deviceorientation', handleOrientation);
    this.orientationWatchId = window.setInterval(() => {
      // Keep reference for cleanup
    }, 1000);

    this.state.isHeadingTracking = true;
    console.log('Device orientation tracking started');
  }

  /**
   * Start GPS-based heading tracking (fallback)
   */
  private startGPSHeadingTracking(): void {
    if (!navigator.geolocation) {
      this.callbacks?.onError?.('No heading tracking available');
      return;
    }

    let lastPosition: GeolocationPosition | null = null;
    
    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        if (lastPosition) {
          const heading = this.calculateHeadingFromGPS(lastPosition, position);
          if (heading !== null) {
            this.updateHeading(heading);
          }
        }
        lastPosition = position;
      },
      (error) => console.error('GPS heading error:', error),
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 1000
      }
    );

    this.orientationWatchId = watchId;
    this.state.isHeadingTracking = true;
    console.log('GPS heading tracking started');
  }

  /**
   * Stop heading tracking
   */
  private stopHeadingTracking(): void {
    if (this.orientationWatchId !== null) {
      if (this.state.isHeadingTracking) {
        navigator.geolocation.clearWatch(this.orientationWatchId);
      }
      this.orientationWatchId = null;
    }
    this.state.isHeadingTracking = false;
    console.log('Heading tracking stopped');
  }

  /**
   * Handle location updates during navigation
   */
  private handleLocationUpdate(position: GeolocationPosition): void {
    const now = Date.now();
    const { latitude, longitude } = position.coords;
    const newLocation: [number, number] = [latitude, longitude];

    // Check if location has changed significantly
    if (this.lastLocation && this.hasLocationChangedSignificantly(newLocation)) {
      return;
    }

    this.lastLocation = newLocation;
    this.lastUpdate = now;
    this.state.currentLocation = newLocation;
    
    this.callbacks?.onLocationUpdate(newLocation);
    this.updateMapView(newLocation);
  }

  /**
   * Handle location errors
   */
  private handleLocationError(error: GeolocationPositionError): void {
    console.error('Navigation location error:', error);
    this.callbacks?.onError?.('Location tracking failed during navigation');
  }

  /**
   * Update heading and rotate map
   */
  private updateHeading(heading: number): void {
    // Normalize heading to 0-360
    heading = ((heading % 360) + 360) % 360;
    
    // Check if heading has changed significantly
    if (Math.abs(heading - this.lastHeading) < this.config.headingThreshold) {
      return;
    }

    this.lastHeading = heading;
    this.state.currentHeading = heading;
    this.targetHeading = heading;
    
    this.callbacks?.onHeadingUpdate(heading);
    this.updateMapRotation();
  }

  /**
   * Update map view to center on user location
   */
  private updateMapView(location: [number, number]): void {
    if (!this.map || !this.state.isNavigating) {
      return;
    }

    // Always center on user location with navigation zoom
    console.log('NavigationService: Setting map view to', location, 'zoom:', this.config.navigationZoom);
    this.map.setView(location, this.config.navigationZoom);
  }

  /**
   * Update map rotation smoothly
   */
  private updateMapRotation(): void {
    if (!this.map || !this.state.isNavigating) {
      return;
    }

    // Smooth rotation towards target heading
    const diff = this.targetHeading - this.currentMapHeading;
    const shortestDiff = ((diff + 180) % 360) - 180;
    
    this.currentMapHeading += shortestDiff * this.config.rotationSmoothness;
    this.currentMapHeading = ((this.currentMapHeading % 360) + 360) % 360;

    // Apply rotation to map (using Leaflet rotation plugin if available)
    if ((this.map as any).setBearing) {
      (this.map as any).setBearing(this.currentMapHeading);
    } else {
      // Fallback: rotate the map container
      const container = this.map.getContainer();
      container.style.transform = `rotate(${this.currentMapHeading}deg)`;
    }
  }

  /**
   * Calculate heading from GPS coordinates
   */
  private calculateHeadingFromGPS(
    from: GeolocationPosition, 
    to: GeolocationPosition
  ): number | null {
    const lat1 = from.coords.latitude * Math.PI / 180;
    const lat2 = to.coords.latitude * Math.PI / 180;
    const deltaLng = (to.coords.longitude - from.coords.longitude) * Math.PI / 180;

    const y = Math.sin(deltaLng) * Math.cos(lat2);
    const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng);

    const heading = Math.atan2(y, x) * 180 / Math.PI;
    return ((heading % 360) + 360) % 360;
  }

  /**
   * Check if location has changed significantly
   */
  private hasLocationChangedSignificantly(newLocation: [number, number]): boolean {
    if (!this.lastLocation) return true;

    const distance = this.calculateDistance(this.lastLocation, newLocation);
    return distance < this.config.locationThreshold;
  }

  /**
   * Calculate distance between two coordinates in meters
   */
  private calculateDistance(
    coord1: [number, number], 
    coord2: [number, number]
  ): number {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = coord1[0] * Math.PI / 180;
    const φ2 = coord2[0] * Math.PI / 180;
    const Δφ = (coord2[0] - coord1[0]) * Math.PI / 180;
    const Δλ = (coord2[1] - coord1[1]) * Math.PI / 180;

    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c;
  }

  /**
   * Get current navigation state
   */
  getState(): NavigationState {
    return { ...this.state };
  }

  /**
   * Check if navigation is active
   */
  isNavigating(): boolean {
    return this.state.isNavigating;
  }

  /**
   * Get current location
   */
  getCurrentLocation(): [number, number] | null {
    return this.state.currentLocation;
  }

  /**
   * Get current heading
   */
  getCurrentHeading(): number {
    return this.state.currentHeading;
  }

  /**
   * Cleanup resources
   */
  cleanup(): void {
    this.stopNavigation();
    this.map = null;
    console.log('NavigationService cleaned up');
  }
}

// Export singleton instance
export const navigationService = new NavigationService();