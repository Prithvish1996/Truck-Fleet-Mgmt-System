// Navigation configuration
// This file allows you to easily switch between different navigation implementations

export interface NavigationConfig {
  useGraphhopper: boolean;
  fallbackOnError: boolean;
  debugMode: boolean;
}

export const navigationConfig: NavigationConfig = {
  // Set to true to use GraphhopperNavigation, false to use NavigationFallback
  useGraphhopper: true,
  
  // Set to true to automatically fallback to original navigation if Graphhopper fails
  fallbackOnError: true,
  
  // Set to true to enable debug logging
  debugMode: false
};

// Helper function to get the current navigation mode
export const getNavigationMode = (): 'graphhopper' | 'fallback' => {
  return navigationConfig.useGraphhopper ? 'graphhopper' : 'fallback';
};

// Helper function to log debug information
export const debugLog = (message: string, ...args: any[]) => {
  if (navigationConfig.debugMode) {
    console.log(`[Navigation Debug] ${message}`, ...args);
  }
};
