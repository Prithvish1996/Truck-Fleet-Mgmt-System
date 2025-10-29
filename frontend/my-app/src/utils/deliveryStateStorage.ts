// Utility for persisting delivery state locally (as backup/optimization)
// The primary source of truth should be the database

const STORAGE_KEY_PREFIX = 'delivery_state_';

export interface DeliveryStateData {
  routeId: string;
  currentPackageIndex: number;
  deliveryState: string;
  timestamp: number; // When this state was saved
}

/**
 * Save delivery state to localStorage
 */
export const saveDeliveryState = (routeId: string, data: Partial<DeliveryStateData>): void => {
  try {
    const key = `${STORAGE_KEY_PREFIX}${routeId}`;
    const stateData: DeliveryStateData = {
      routeId,
      currentPackageIndex: data.currentPackageIndex ?? 0,
      deliveryState: data.deliveryState ?? 'loading',
      timestamp: Date.now(),
      ...data
    };
    localStorage.setItem(key, JSON.stringify(stateData));
  } catch (error) {
    console.warn('Failed to save delivery state to localStorage:', error);
  }
};

/**
 * Load delivery state from localStorage
 */
export const loadDeliveryState = (routeId: string): DeliveryStateData | null => {
  try {
    const key = `${STORAGE_KEY_PREFIX}${routeId}`;
    const data = localStorage.getItem(key);
    if (!data) return null;

    const stateData = JSON.parse(data) as DeliveryStateData;
    
    // Check if state is still valid (within 24 hours)
    const maxAge = 24 * 60 * 60 * 1000; // 24 hours
    if (Date.now() - stateData.timestamp > maxAge) {
      clearDeliveryState(routeId);
      return null;
    }

    return stateData;
  } catch (error) {
    console.warn('Failed to load delivery state from localStorage:', error);
    return null;
  }
};

/**
 * Clear delivery state from localStorage
 */
export const clearDeliveryState = (routeId: string): void => {
  try {
    const key = `${STORAGE_KEY_PREFIX}${routeId}`;
    localStorage.removeItem(key);
  } catch (error) {
    console.warn('Failed to clear delivery state from localStorage:', error);
  }
};
