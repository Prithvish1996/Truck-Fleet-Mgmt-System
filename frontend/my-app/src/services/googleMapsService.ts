import { apiConfig } from '../config/apiConfig';

const GOOGLE_MAPS_API_KEY = process.env.REACT_APP_GOOGLE_MAPS_API_KEY || '';
const USE_GOOGLE_MAPS_API = false;

export interface GoogleMapsRouteEstimate {
  distance: {
    text: string;
    value: number;
  };
  duration: {
    text: string;
    value: number;
  };
  status: string;
}

export interface GoogleMapsDistanceMatrixResponse {
  destination_addresses: string[];
  origin_addresses: string[];
  rows: Array<{
    elements: GoogleMapsRouteEstimate[];
  }>;
  status: string;
}

class GoogleMapsService {
  private apiKey: string;
  private useApi: boolean;

  constructor(apiKey: string = GOOGLE_MAPS_API_KEY, useApi: boolean = USE_GOOGLE_MAPS_API) {
    this.apiKey = apiKey;
    this.useApi = useApi && !!apiKey;
    if (useApi && !apiKey) {
      console.warn('Google Maps API key not configured. Please set REACT_APP_GOOGLE_MAPS_API_KEY');
    }
  }

  private calculateDistance(origin: [number, number], destination: [number, number]): number {
    const R = 6371e3;
    const φ1 = origin[0] * Math.PI / 180;
    const φ2 = destination[0] * Math.PI / 180;
    const Δφ = (destination[0] - origin[0]) * Math.PI / 180;
    const Δλ = (destination[1] - origin[1]) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }

  async getTimeEstimate(
    origin: [number, number],
    destination: [number, number],
    mode: 'driving' | 'walking' | 'bicycling' | 'transit' = 'driving'
  ): Promise<{ durationInSeconds: number; distanceInMeters: number; durationText: string; distanceText: string }> {
    if (this.useApi) {
      try {
        return await this.getTimeEstimateFromAPI(origin, destination, mode);
      } catch (error) {
        console.warn('Google Maps API failed, falling back to local calculation:', error);
      }
    }

    return this.getTimeEstimateLocal(origin, destination, mode);
  }

  private async getTimeEstimateFromAPI(
    origin: [number, number],
    destination: [number, number],
    mode: 'driving' | 'walking' | 'bicycling' | 'transit'
  ): Promise<{ durationInSeconds: number; distanceInMeters: number; durationText: string; distanceText: string }> {
    if (!this.apiKey) {
      throw new Error('Google Maps API key is not configured');
    }

    const originStr = `${origin[0]},${origin[1]}`;
    const destStr = `${destination[0]},${destination[1]}`;

    const url = `https://maps.googleapis.com/maps/api/distancematrix/json?origins=${originStr}&destinations=${destStr}&mode=${mode}&key=${this.apiKey}`;

    const response = await fetch(url);
    
    if (!response.ok) {
      throw new Error(`Google Maps API error: ${response.status} ${response.statusText}`);
    }

    const data: GoogleMapsDistanceMatrixResponse = await response.json();

    if (data.status !== 'OK' || !data.rows || data.rows.length === 0) {
      throw new Error(`Google Maps API returned status: ${data.status}`);
    }

    const element = data.rows[0].elements[0];

    if (element.status !== 'OK') {
      throw new Error(`Route calculation failed: ${element.status}`);
    }

    return {
      durationInSeconds: element.duration.value,
      distanceInMeters: element.distance.value,
      durationText: element.duration.text,
      distanceText: element.distance.text
    };
  }

  private getTimeEstimateLocal(
    origin: [number, number],
    destination: [number, number],
    mode: 'driving' | 'walking' | 'bicycling' | 'transit'
  ): { durationInSeconds: number; distanceInMeters: number; durationText: string; distanceText: string } {
    const distanceInMeters = this.calculateDistance(origin, destination);
    
    const averageSpeed: { [key: string]: number } = {
      driving: 50,
      walking: 5,
      bicycling: 15,
      transit: 30
    };
    
    const speed = averageSpeed[mode] || 50;
    const distanceInKm = distanceInMeters / 1000;
    const durationInHours = distanceInKm / speed;
    const durationInSeconds = Math.round(durationInHours * 3600);
    
    let distanceText: string;
    if (distanceInMeters < 1000) {
      distanceText = `${Math.round(distanceInMeters)} m`;
    } else {
      distanceText = `${distanceInKm.toFixed(1)} km`;
    }
    
    let durationText: string;
    if (durationInSeconds < 60) {
      durationText = `${durationInSeconds} sec`;
    } else if (durationInSeconds < 3600) {
      const minutes = Math.round(durationInSeconds / 60);
      durationText = `${minutes} min`;
    } else {
      const hours = Math.floor(durationInSeconds / 3600);
      const minutes = Math.round((durationInSeconds % 3600) / 60);
      durationText = minutes > 0 ? `${hours} h ${minutes} min` : `${hours} h`;
    }

    return {
      durationInSeconds,
      distanceInMeters,
      durationText,
      distanceText
    };
  }

  async sendTimeEstimateToBackend(
    packageId: string,
    userLocation: [number, number],
    packageLocation: [number, number],
    estimatedDurationSeconds: number,
    estimatedDistanceMeters: number
  ): Promise<void> {
    try {
      const url = `${apiConfig.baseURL}/packages/${packageId}/delivery-estimate`;
      
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userLocation: {
            latitude: userLocation[0],
            longitude: userLocation[1]
          },
          packageLocation: {
            latitude: packageLocation[0],
            longitude: packageLocation[1]
          },
          estimatedDurationSeconds,
          estimatedDistanceMeters,
          timestamp: new Date().toISOString()
        }),
        ...apiConfig.useHTTPS ? {} : {},
        credentials: 'include'
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to send time estimate to backend: ${response.status} ${errorText}`);
      }

      console.log('Time estimate sent to backend successfully');
    } catch (error) {
      console.error('Error sending time estimate to backend:', error);
    }
  }

  generateNavigationDeepLink(
    destination: [number, number],
    destinationAddress?: string
  ): string {
    const lat = destination[0];
    const lng = destination[1];

    if (destinationAddress) {
      const encodedAddress = encodeURIComponent(destinationAddress);
      return `https://www.google.com/maps/dir/?api=1&destination=${encodedAddress}`;
    } else {
      return `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`;
    }
  }

  openNavigation(destination: [number, number], destinationAddress?: string): void {
    const deepLink = this.generateNavigationDeepLink(destination, destinationAddress);
    console.log('Opening Google Maps:', deepLink);
    window.open(deepLink, '_blank');
  }
}

export const googleMapsService = new GoogleMapsService();
