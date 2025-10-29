import { GraphhopperRoute } from './graphhopperService';
import { graphhopperService } from './graphhopperService';

export interface SimulationConfig {
  speedMultiplier: number; // How much faster than normal (e.g., 10x speed)
  updateInterval: number; // How often to update position (ms)
  enableHeadingSimulation: boolean; // Whether to simulate heading changes
}

export interface SimulationCallbacks {
  onLocationUpdate: (location: [number, number]) => void;
  onHeadingUpdate: (heading: number) => void;
  onSimulationComplete: () => void;
  onError?: (error: string) => void;
}

export interface SimulationState {
  isRunning: boolean;
  currentIndex: number;
  totalPoints: number;
  progress: number; // 0-1
  currentLocation: [number, number] | null;
  currentHeading: number;
}

class RouteSimulator {
  private config: SimulationConfig;
  private callbacks: SimulationCallbacks | null = null;
  
  // State
  private state: SimulationState = {
    isRunning: false,
    currentIndex: 0,
    totalPoints: 0,
    progress: 0,
    currentLocation: null,
    currentHeading: 0
  };
  
  // Route data
  private route: GraphhopperRoute | null = null;
  private routePoints: [number, number][] = [];
  
  // Simulation
  private simulationInterval: NodeJS.Timeout | null = null;
  private lastUpdateTime: number = 0;

  constructor(config: Partial<SimulationConfig> = {}) {
    this.config = {
      speedMultiplier: 1, // 1x normal speed (realistic driving)
      updateInterval: 200, // Update every 200ms to reduce map reloading
      enableHeadingSimulation: true,
      ...config
    };
  }

  /**
   * Set callbacks for the simulator
   */
  setCallbacks(callbacks: SimulationCallbacks): void {
    this.callbacks = callbacks;
  }

  /**
   * Load route for simulation
   */
  loadRoute(route: GraphhopperRoute): void {
    if (!route.paths || route.paths.length === 0) {
      this.callbacks?.onError?.('Invalid route data');
      return;
    }

    this.route = route;
    this.routePoints = graphhopperService.decodePolyline(route.paths[0].points);
    this.state.totalPoints = this.routePoints.length;
    this.state.currentIndex = 0;
    this.state.progress = 0;
    
    console.log(`Route loaded with ${this.routePoints.length} points`);
  }

  /**
   * Start route simulation
   */
  startSimulation(): void {
    if (this.state.isRunning) {
      console.log('Simulation already running');
      return;
    }

    if (!this.route || this.routePoints.length === 0) {
      this.callbacks?.onError?.('No route loaded for simulation');
      return;
    }

    this.state.isRunning = true;
    this.state.currentIndex = 0;
    this.state.progress = 0;
    this.state.currentLocation = this.routePoints[0]; // Start at first point
    this.state.currentHeading = 0;
    this.lastUpdateTime = Date.now();

    // Send initial location update
    this.callbacks?.onLocationUpdate(this.state.currentLocation);
    this.callbacks?.onHeadingUpdate(this.state.currentHeading);

    // Start simulation loop
    this.simulationInterval = setInterval(() => {
      this.updateSimulation();
    }, this.config.updateInterval);

    console.log('Route simulation started at point 0:', this.state.currentLocation);
  }

  /**
   * Stop route simulation
   */
  stopSimulation(): void {
    if (!this.state.isRunning) {
      return;
    }

    this.state.isRunning = false;
    
    if (this.simulationInterval) {
      clearInterval(this.simulationInterval);
      this.simulationInterval = null;
    }

    console.log('Route simulation stopped');
  }

  /**
   * Pause simulation
   */
  pauseSimulation(): void {
    if (this.simulationInterval) {
      clearInterval(this.simulationInterval);
      this.simulationInterval = null;
    }
    console.log('Route simulation paused');
  }

  /**
   * Resume simulation
   */
  resumeSimulation(): void {
    if (!this.state.isRunning) {
      return;
    }

    this.simulationInterval = setInterval(() => {
      this.updateSimulation();
    }, this.config.updateInterval);

    console.log('Route simulation resumed');
  }

  /**
   * Update simulation state with smooth interpolation
   */
  private updateSimulation(): void {
    if (!this.state.isRunning || this.routePoints.length === 0) {
      console.log('Simulation not running or no route points:', {
        isRunning: this.state.isRunning,
        routePointsLength: this.routePoints.length
      });
      return;
    }

    const now = Date.now();
    const deltaTime = now - this.lastUpdateTime;
    this.lastUpdateTime = now;

    // Calculate movement speed based on speed multiplier
    // Base speed: realistic driving speed (50 km/h = ~13.9 m/s)
    // Route points are typically ~10-50 meters apart
    // So we move about 1 point every 2-5 seconds at normal speed
    const baseSpeed = 0.3; // points per second (very slow, realistic)
    const currentSpeed = baseSpeed * this.config.speedMultiplier;
    
    // Calculate how much to advance based on time elapsed
    const advanceAmount = (currentSpeed * deltaTime) / 1000; // Convert to points
    
    // Update the current position with interpolation
    this.state.currentIndex += advanceAmount;
    
    // Check if we've reached the end
    if (this.state.currentIndex >= this.routePoints.length - 1) {
      this.completeSimulation();
      return;
    }

    // Interpolate between current and next point for smooth movement
    const currentIndexFloor = Math.floor(this.state.currentIndex);
    const currentIndexCeil = Math.min(currentIndexFloor + 1, this.routePoints.length - 1);
    const interpolationFactor = this.state.currentIndex - currentIndexFloor;

    // Linear interpolation between two points
    const currentPoint = this.routePoints[currentIndexFloor];
    const nextPoint = this.routePoints[currentIndexCeil];
    
    const interpolatedLocation: [number, number] = [
      currentPoint[0] + (nextPoint[0] - currentPoint[0]) * interpolationFactor,
      currentPoint[1] + (nextPoint[1] - currentPoint[1]) * interpolationFactor
    ];

    // Update state
    this.state.currentLocation = interpolatedLocation;
    this.state.progress = this.state.currentIndex / (this.routePoints.length - 1);

    // Calculate heading based on direction of movement
    if (this.config.enableHeadingSimulation && currentIndexCeil > currentIndexFloor) {
      this.state.currentHeading = this.calculateHeading(currentPoint, nextPoint);
    }

    // Notify callbacks with smooth interpolated location
    this.callbacks?.onLocationUpdate(this.state.currentLocation);
    this.callbacks?.onHeadingUpdate(this.state.currentHeading);
  }


  /**
   * Complete the simulation
   */
  private completeSimulation(): void {
    this.state.isRunning = false;
    this.state.progress = 1;
    this.state.currentIndex = this.routePoints.length - 1;
    this.state.currentLocation = this.routePoints[this.routePoints.length - 1];

    if (this.simulationInterval) {
      clearInterval(this.simulationInterval);
      this.simulationInterval = null;
    }

    this.callbacks?.onSimulationComplete();
    console.log('Route simulation completed');
  }

  /**
   * Calculate heading between two points
   */
  private calculateHeading(from: [number, number], to: [number, number]): number {
    const lat1 = from[0] * Math.PI / 180;
    const lat2 = to[0] * Math.PI / 180;
    const deltaLng = (to[1] - from[1]) * Math.PI / 180;

    const y = Math.sin(deltaLng) * Math.cos(lat2);
    const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng);

    const heading = Math.atan2(y, x) * 180 / Math.PI;
    return ((heading % 360) + 360) % 360;
  }

  /**
   * Calculate distance between two coordinates in meters
   */
  private calculateDistance(coord1: [number, number], coord2: [number, number]): number {
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
   * Set simulation speed multiplier
   */
  setSpeedMultiplier(multiplier: number): void {
    this.config.speedMultiplier = Math.max(1, multiplier);
    console.log(`Speed multiplier set to ${this.config.speedMultiplier}x`);
  }

  /**
   * Jump to a specific point in the route
   */
  jumpToPoint(index: number): void {
    if (index < 0 || index >= this.routePoints.length) {
      this.callbacks?.onError?.('Invalid point index');
      return;
    }

    this.state.currentIndex = index;
    this.state.progress = index / (this.routePoints.length - 1);
    this.state.currentLocation = this.routePoints[index];

    // Calculate heading
    if (this.config.enableHeadingSimulation && index > 0) {
      this.state.currentHeading = this.calculateHeading(
        this.routePoints[index - 1],
        this.routePoints[index]
      );
    }

    this.callbacks?.onLocationUpdate(this.state.currentLocation);
    this.callbacks?.onHeadingUpdate(this.state.currentHeading);
  }

  /**
   * Get current simulation state
   */
  getState(): SimulationState {
    return { ...this.state };
  }

  /**
   * Check if simulation is running
   */
  isRunning(): boolean {
    return this.state.isRunning;
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
   * Get simulation progress (0-1)
   */
  getProgress(): number {
    return this.state.progress;
  }

  /**
   * Cleanup resources
   */
  cleanup(): void {
    this.stopSimulation();
    this.route = null;
    this.routePoints = [];
    this.state = {
      isRunning: false,
      currentIndex: 0,
      totalPoints: 0,
      progress: 0,
      currentLocation: null,
      currentHeading: 0
    };
    console.log('RouteSimulator cleaned up');
  }
}

// Export singleton instance
export const routeSimulator = new RouteSimulator();
