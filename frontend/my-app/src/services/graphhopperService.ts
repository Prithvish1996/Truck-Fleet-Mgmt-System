// Graphhopper API service for routing
const GRAPHHOPPER_API_KEY = 'fb0db7b9-2e13-4fc7-88f2-3e02849fe834'; // Replace with your actual Graphhopper API key
const GRAPHHOPPER_BASE_URL = 'https://graphhopper.com/api/1';

export interface GraphhopperRoute {
  paths: Array<{
    distance: number;
    time: number;
    points: string; // Encoded polyline
    instructions: Array<{
      text: string;
      distance: number;
      time: number;
      sign: number;
    }>;
  }>;
}

export interface RouteRequest {
  points: Array<[number, number]>; // [lat, lng] pairs
  vehicle?: 'car' | 'bike' | 'foot';
  instructions?: boolean;
  points_encoded?: boolean;
}

class GraphhopperService {
  private apiKey: string;

  constructor(apiKey: string = GRAPHHOPPER_API_KEY) {
    this.apiKey = apiKey;
  }

  /**
   * Get route between two or more points
   */
  async getRoute(request: RouteRequest): Promise<GraphhopperRoute> {
    const { points, vehicle = 'car', instructions = true, points_encoded = false } = request;
    
    if (points.length < 2) {
      throw new Error('At least 2 points are required for routing');
    }

    // Validate coordinates
    for (const point of points) {
      const [lat, lng] = point;
      if (lat < -90 || lat > 90) {
        throw new Error(`Invalid latitude: ${lat}. Must be between -90 and 90.`);
      }
      if (lng < -180 || lng > 180) {
        throw new Error(`Invalid longitude: ${lng}. Must be between -180 and 180.`);
      }
    }

    // Convert points to the format expected by Graphhopper API (latitude,longitude)
    const pointStrings = points.map(point => `${point[0]},${point[1]}`).join('&point=');
    
    const url = `${GRAPHHOPPER_BASE_URL}/route?key=${this.apiKey}&point=${pointStrings}&vehicle=${vehicle}&instructions=${instructions}&points_encoded=${points_encoded}`;
    
    try {
      console.log('Graphhopper API URL:', url);
      const response = await fetch(url);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Graphhopper API error response:', errorText);
        throw new Error(`Graphhopper API error: ${response.status} ${response.statusText}. Details: ${errorText}`);
      }
      
      const data = await response.json();
      console.log('Graphhopper API response:', data);
      
      if (data.paths && data.paths.length > 0) {
        return data;
      } else {
        throw new Error('No route found in response');
      }
    } catch (error) {
      console.error('Error fetching route from Graphhopper:', error);
      throw error;
    }
  }

  /**
   * Decode polyline string to coordinates
   */
  decodePolyline(encoded: string): Array<[number, number]> {
    const points: Array<[number, number]> = [];
    let index = 0;
    const len = encoded.length;
    let lat = 0;
    let lng = 0;

    while (index < len) {
      let b: number;
      let shift = 0;
      let result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlat = ((result & 1) !== 0 ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      const dlng = ((result & 1) !== 0 ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      points.push([lat / 1e5, lng / 1e5]);
    }

    return points;
  }

  /**
   * Get distance and duration between two points
   */
  async getDistanceAndDuration(from: [number, number], to: [number, number]): Promise<{distance: number, duration: number}> {
    try {
      const route = await this.getRoute({
        points: [from, to],
        instructions: false
      });
      
      if (route.paths && route.paths.length > 0) {
        return {
          distance: route.paths[0].distance,
          duration: route.paths[0].time
        };
      }
      
      throw new Error('No route data available');
    } catch (error) {
      console.error('Error getting distance and duration:', error);
      throw error;
    }
  }
}

export const graphhopperService = new GraphhopperService();
