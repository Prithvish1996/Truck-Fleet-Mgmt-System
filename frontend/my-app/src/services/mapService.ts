import L from 'leaflet';
import { GraphhopperRoute } from './graphhopperService';
import { graphhopperService } from './graphhopperService';
import { navigationService } from './navigationService';

export interface MapServiceConfig {
  throttleDelay: number;
  minDistanceThreshold: number;
  liveTrackingZoom: number;
  overviewZoom: number;
  routeDebounceDelay: number;
}

export interface MapServiceCallbacks {
  onLocationUpdate: (location: [number, number]) => void;
  onRouteUpdate: (route: GraphhopperRoute | null) => void;
  onHeadingUpdate?: (heading: number) => void;
  onError?: (error: string) => void;
}

class MapService {
  private config: MapServiceConfig;
  private callbacks: MapServiceCallbacks | null = null;
  private map: L.Map | null = null;
  
  // Route management
  private currentRoute: GraphhopperRoute | null = null;
  private routeLoaded: boolean = false;
  private routeFetchTimeout: NodeJS.Timeout | null = null;
  
  // Location tracking
  private watchId: number | null = null;
  private lastLocation: [number, number] | null = null;
  private lastUpdate: number = 0;
  
  // Map elements
  private routeLayer: L.Polyline | null = null;
  private markers: L.Marker[] = [];
  private liveLocationMarker: L.Marker | null = null;
  
  // State
  private liveTrackingMode: boolean = false;
  private navigationMode: boolean = false;
  private isInitialized: boolean = false;

  constructor(config: Partial<MapServiceConfig> = {}) {
    this.config = {
      throttleDelay: 5000,
      minDistanceThreshold: 0.0005, // ~50 meters
      liveTrackingZoom: 19,
      overviewZoom: 18,
      routeDebounceDelay: 2000,
      ...config
    };
  }

  /**
   * Set callbacks for the service
   */
  setCallbacks(callbacks: MapServiceCallbacks): void {
    this.callbacks = callbacks;
  }

  /**
   * Initialize the map service with a Leaflet map instance
   */
  initialize(map: L.Map): void {
    if (this.isInitialized) {
      console.warn('MapService already initialized');
      return;
    }

    this.map = map;
    this.isInitialized = true;
    
    // Initialize navigation service
    navigationService.initialize(map);
    
    console.log('MapService initialized with map:', !!map);
  }

  /**
   * Start location tracking
   */
  startLocationTracking(): void {
    if (!navigator.geolocation) {
      this.callbacks?.onError?.('Geolocation is not supported');
      return;
    }

    if (this.watchId !== null) {
      console.log('Location tracking already active');
      return;
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => this.handleLocationUpdate(position),
      (error) => this.handleLocationError(error),
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 5000
      }
    );

    console.log('Location tracking started');
  }

  /**
   * Stop location tracking
   */
  stopLocationTracking(): void {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
      console.log('Location tracking stopped');
    }
  }

  /**
   * Set live tracking mode
   */
  setLiveTrackingMode(enabled: boolean): void {
    if (this.liveTrackingMode === enabled) {
      return;
    }

    this.liveTrackingMode = enabled;
    
    if (enabled && this.lastLocation && this.map) {
      this.createLiveLocationMarker();
      this.map.setView(this.lastLocation, this.config.liveTrackingZoom);
    } else if (!enabled && this.liveLocationMarker && this.map) {
      this.map.removeLayer(this.liveLocationMarker);
      this.liveLocationMarker = null;
    }
  }

  /**
   * Set navigation mode (Google Maps-style)
   */
  setNavigationMode(enabled: boolean): void {
    if (this.navigationMode === enabled) {
      return;
    }

    this.navigationMode = enabled;
    
    if (enabled) {
      // Stop regular location tracking
      this.stopLocationTracking();
      
      // Setup navigation service callbacks
      navigationService.setCallbacks({
        onLocationUpdate: (location) => {
          this.lastLocation = location;
          this.callbacks?.onLocationUpdate(location);
        },
        onHeadingUpdate: (heading) => {
          this.callbacks?.onHeadingUpdate?.(heading);
        },
        onError: (error) => {
          this.callbacks?.onError?.(error);
        }
      });
      
      // Start navigation
      navigationService.startNavigation();
      
      console.log('Navigation mode enabled');
    } else {
      // Stop navigation
      navigationService.stopNavigation();
      
      // Resume regular location tracking
      this.startLocationTracking();
      
      console.log('Navigation mode disabled');
    }
  }

  /**
   * Load route for given start and destination
   */
  async loadRoute(
    start: [number, number], 
    destination: [number, number]
  ): Promise<void> {
    console.log('loadRoute called with:', { start, destination, mapInitialized: !!this.map });
    
    if (!this.map) {
      console.error('Map not initialized when trying to load route');
      this.callbacks?.onError?.('Map not initialized');
      return;
    }

    // Check if route is already loaded and we're in live tracking mode
    if (this.routeLoaded && this.liveTrackingMode) {
      console.log('Route already loaded, using cached route for live tracking');
      return;
    }

    // Check if we already have a valid route
    if (this.currentRoute && this.isValidRoute(this.currentRoute)) {
      console.log('Using existing route, skipping API call');
      this.routeLoaded = true;
      return;
    }

    // Clear any existing timeout
    if (this.routeFetchTimeout) {
      clearTimeout(this.routeFetchTimeout);
    }

    // Debounce route fetching
    this.routeFetchTimeout = setTimeout(async () => {
      try {
        console.log('Fetching new route from Graphhopper...');
        const route = await graphhopperService.getRoute({
          points: [start, destination],
          vehicle: 'car',
          instructions: true,
          points_encoded: true
        });

        this.currentRoute = route;
        this.routeLoaded = true;
        this.callbacks?.onRouteUpdate(route);
        
        await this.displayRoute(route, start, destination);
      } catch (error) {
        console.error('Error fetching route from Graphhopper:', error);
        this.callbacks?.onError?.('Failed to load route');
        await this.displayFallbackRoute(start, destination);
      }
    }, this.config.routeDebounceDelay);
  }

  /**
   * Update user location and handle map view
   */
  private handleLocationUpdate(position: GeolocationPosition): void {
    const now = Date.now();
    if (now - this.lastUpdate < this.config.throttleDelay) {
      return; // Skip this update due to throttling
    }

    const { latitude, longitude } = position.coords;
    const newLocation: [number, number] = [latitude, longitude];

    // Check if location has changed significantly
    if (this.lastLocation && this.hasLocationChangedSignificantly(newLocation)) {
      return; // Skip insignificant location changes
    }

    this.lastLocation = newLocation;
    this.lastUpdate = now;
    this.callbacks?.onLocationUpdate(newLocation);

    // Update live location marker
    if (this.liveTrackingMode) {
      this.updateLiveLocationMarker(newLocation);
    }

    // Update map view if user isn't interacting with it (but not in navigation mode)
    if (this.map && !this.isUserInteractingWithMap() && !this.navigationMode) {
      const zoom = this.liveTrackingMode ? this.config.liveTrackingZoom : this.config.overviewZoom;
      console.log('MapService: Setting map view to', newLocation, 'zoom:', zoom, 'navigationMode:', this.navigationMode);
      this.map.setView(newLocation, zoom);
    }
  }

  /**
   * Handle geolocation errors
   */
  private handleLocationError(error: GeolocationPositionError): void {
    console.error('Geolocation error:', error);
    this.callbacks?.onError?.('Location tracking failed');
  }

  /**
   * Check if location has changed significantly
   */
  private hasLocationChangedSignificantly(newLocation: [number, number]): boolean {
    if (!this.lastLocation) return true;

    const distance = Math.sqrt(
      Math.pow(newLocation[0] - this.lastLocation[0], 2) +
      Math.pow(newLocation[1] - this.lastLocation[1], 2)
    );

    return distance < this.config.minDistanceThreshold;
  }

  /**
   * Check if user is interacting with the map
   */
  private isUserInteractingWithMap(): boolean {
    if (!this.map) return false;
    
    const container = this.map.getContainer();
    return container.classList.contains('leaflet-dragging') || 
           container.classList.contains('leaflet-zoom-anim');
  }

  /**
   * Create live location marker
   */
  private createLiveLocationMarker(): void {
    if (!this.map || !this.lastLocation) return;

    this.liveLocationMarker = L.marker(this.lastLocation, {
      icon: L.divIcon({
        className: 'map-marker map-marker--live',
        html: '<div class="map-marker__content map-marker__content--live"></div>',
        iconSize: [24, 24],
        iconAnchor: [12, 12]
      })
    }).addTo(this.map);
  }

  /**
   * Update live location marker position
   */
  private updateLiveLocationMarker(location: [number, number]): void {
    if (this.liveLocationMarker) {
      this.liveLocationMarker.setLatLng(location);
    } else {
      this.createLiveLocationMarker();
    }
  }

  /**
   * Display route on map
   */
  private async displayRoute(
    route: GraphhopperRoute, 
    start: [number, number], 
    destination: [number, number]
  ): Promise<void> {
    if (!this.map || !route.paths || route.paths.length === 0) return;

    const path = route.paths[0];
    const coordinates = graphhopperService.decodePolyline(path.points);
    
    // Clear existing route
    this.clearRoute();
    
    // Create route polyline
    this.routeLayer = L.polyline(coordinates, {
      color: '#3388ff',
      weight: 6,
      opacity: 0.8,
      smoothFactor: 1
    }).addTo(this.map);

    // Add start and end markers
    this.addRouteMarkers(start, destination);

    // Fit map to show the entire route (only if not in navigation mode)
    const group = new L.FeatureGroup([this.routeLayer, ...this.markers]);
    if (!this.navigationMode) {
      this.map.fitBounds(group.getBounds().pad(0.1));
    }

    console.log('Route displayed successfully');
  }

  /**
   * Display fallback route (straight line)
   */
  private async displayFallbackRoute(
    start: [number, number], 
    destination: [number, number]
  ): Promise<void> {
    if (!this.map) return;

    this.clearRoute();

    this.routeLayer = L.polyline([start, destination], {
      color: '#ff6b6b',
      weight: 4,
      opacity: 0.6,
      dashArray: '10, 10'
    }).addTo(this.map);

    this.addRouteMarkers(start, destination);

    const group = new L.FeatureGroup([this.routeLayer, ...this.markers]);
    if (!this.navigationMode) {
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  /**
   * Add start and end markers
   */
  private addRouteMarkers(start: [number, number], destination: [number, number]): void {
    if (!this.map) return;

    const startMarker = L.marker(start, {
      icon: L.divIcon({
        className: 'map-marker map-marker--start',
        html: '<div class="map-marker__content map-marker__content--start"></div>',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      })
    }).addTo(this.map);

    const endMarker = L.marker(destination, {
      icon: L.divIcon({
        className: 'map-marker map-marker--end',
        html: '<div class="map-marker__content map-marker__content--end"></div>',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      })
    }).addTo(this.map);

    this.markers = [startMarker, endMarker];
  }

  /**
   * Clear route from map
   */
  private clearRoute(): void {
    if (this.routeLayer && this.map) {
      this.map.removeLayer(this.routeLayer);
      this.routeLayer = null;
    }

    this.markers.forEach(marker => {
      if (this.map) {
        this.map.removeLayer(marker);
      }
    });
    this.markers = [];
  }

  /**
   * Check if route is valid
   */
  private isValidRoute(route: GraphhopperRoute): boolean {
    return route.paths && 
           route.paths.length > 0 && 
           Math.abs(route.paths[0]?.distance - 0) < 1000;
  }

  /**
   * Get current route
   */
  getCurrentRoute(): GraphhopperRoute | null {
    return this.currentRoute;
  }

  /**
   * Check if route is loaded
   */
  isRouteLoaded(): boolean {
    return this.routeLoaded;
  }

  /**
   * Check if live tracking is enabled
   */
  isLiveTrackingEnabled(): boolean {
    return this.liveTrackingMode;
  }

  /**
   * Get current user location
   */
  getCurrentLocation(): [number, number] | null {
    return this.lastLocation;
  }

  /**
   * Check if navigation mode is enabled
   */
  isNavigationModeEnabled(): boolean {
    return this.navigationMode;
  }

  /**
   * Get current heading from navigation service
   */
  getCurrentHeading(): number {
    return navigationService.getCurrentHeading();
  }

  /**
   * Get navigation state
   */
  getNavigationState() {
    return navigationService.getState();
  }

  /**
   * Cleanup resources
   */
  cleanup(): void {
    this.stopLocationTracking();
    
    // Stop navigation if active
    if (this.navigationMode) {
      navigationService.stopNavigation();
    }
    
    if (this.routeFetchTimeout) {
      clearTimeout(this.routeFetchTimeout);
      this.routeFetchTimeout = null;
    }

    this.clearRoute();

    if (this.liveLocationMarker && this.map) {
      this.map.removeLayer(this.liveLocationMarker);
      this.liveLocationMarker = null;
    }

    // Cleanup navigation service
    navigationService.cleanup();

    this.map = null;
    this.isInitialized = false;
    console.log('MapService cleaned up');
  }
}

// Export singleton instance
export const mapService = new MapService();
