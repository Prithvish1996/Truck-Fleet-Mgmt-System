const STORAGE_KEY_PREFIX = 'delivery_state_';

export interface DeliveryStateData {
  routeId: string;
  currentPackageIndex: number;
  deliveryState: string;
  timestamp: number;
}

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

export const loadDeliveryState = (routeId: string): DeliveryStateData | null => {
  try {
    const key = `${STORAGE_KEY_PREFIX}${routeId}`;
    const data = localStorage.getItem(key);
    if (!data) return null;

    const stateData = JSON.parse(data) as DeliveryStateData;
    
    const maxAge = 24 * 60 * 60 * 1000;
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

export const clearDeliveryState = (routeId: string): void => {
  try {
    const key = `${STORAGE_KEY_PREFIX}${routeId}`;
    localStorage.removeItem(key);
  } catch (error) {
    console.warn('Failed to clear delivery state from localStorage:', error);
  }
};
