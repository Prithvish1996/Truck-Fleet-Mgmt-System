/**
 * Route Simulator Test File
 * 
 * This file demonstrates how to use the RouteSimulator service independently
 * for testing navigation functionality without the UI components.
 */

import { routeSimulator } from '../services/routeSimulator';
import { graphhopperService } from '../services/graphhopperService';

// Example usage of the RouteSimulator
export class RouteSimulatorTest {
  private isRunning = false;

  /**
   * Test the route simulator with a sample route
   */
  async testRouteSimulation() {
    try {
      // Example route from Amsterdam to Utrecht
      const startPoint: [number, number] = [52.3676, 4.9041]; // Amsterdam
      const endPoint: [number, number] = [52.0907, 5.1214]; // Utrecht

      console.log('Fetching route from Graphhopper...');
      
      // Get route from Graphhopper
      const route = await graphhopperService.getRoute({
        points: [startPoint, endPoint],
        vehicle: 'car',
        instructions: true,
        points_encoded: true
      });

      console.log('Route fetched successfully:', route);

      // Set up simulator callbacks
      routeSimulator.setCallbacks({
        onLocationUpdate: (location) => {
          console.log('Location updated:', location);
        },
        onHeadingUpdate: (heading) => {
          console.log('Heading updated:', heading);
        },
        onSimulationComplete: () => {
          console.log('Simulation completed!');
          this.isRunning = false;
        },
        onError: (error) => {
          console.error('Simulation error:', error);
          this.isRunning = false;
        }
      });

      // Load route into simulator
      routeSimulator.loadRoute(route);
      
      // Configure simulation speed (10x normal speed)
      routeSimulator.setSpeedMultiplier(10);

      // Start simulation
      console.log('Starting route simulation...');
      routeSimulator.startSimulation();
      this.isRunning = true;

      // Monitor progress
      const progressInterval = setInterval(() => {
        if (!this.isRunning) {
          clearInterval(progressInterval);
          return;
        }

        const state = routeSimulator.getState();
        console.log(`Progress: ${Math.round(state.progress * 100)}% - Point ${state.currentIndex}/${state.totalPoints}`);
      }, 1000);

    } catch (error) {
      console.error('Test failed:', error);
    }
  }

  /**
   * Test with custom speed multiplier
   */
  async testWithCustomSpeed(speedMultiplier: number = 20) {
    console.log(`Testing with ${speedMultiplier}x speed...`);
    
    // You can modify the speed during simulation
    routeSimulator.setSpeedMultiplier(speedMultiplier);
    
    if (!routeSimulator.isRunning()) {
      await this.testRouteSimulation();
    }
  }

  /**
   * Test jumping to specific points in the route
   */
  testJumpToPoint(pointIndex: number) {
    console.log(`Jumping to point ${pointIndex}...`);
    routeSimulator.jumpToPoint(pointIndex);
    
    const state = routeSimulator.getState();
    console.log('Current state:', state);
  }

  /**
   * Stop the current simulation
   */
  stopSimulation() {
    console.log('Stopping simulation...');
    routeSimulator.stopSimulation();
    this.isRunning = false;
  }

  /**
   * Get current simulation state
   */
  getSimulationState() {
    return routeSimulator.getState();
  }

  /**
   * Cleanup resources
   */
  cleanup() {
    routeSimulator.cleanup();
    console.log('Test cleanup completed');
  }
}

// Export for use in other files
export const routeSimulatorTest = new RouteSimulatorTest();

// Example usage (uncomment to run):
/*
// Test basic simulation
routeSimulatorTest.testRouteSimulation();

// Test with different speeds
setTimeout(() => {
  routeSimulatorTest.testWithCustomSpeed(25);
}, 5000);

// Test jumping to points
setTimeout(() => {
  routeSimulatorTest.testJumpToPoint(50);
}, 10000);

// Stop simulation after 30 seconds
setTimeout(() => {
  routeSimulatorTest.stopSimulation();
  routeSimulatorTest.cleanup();
}, 30000);
*/
