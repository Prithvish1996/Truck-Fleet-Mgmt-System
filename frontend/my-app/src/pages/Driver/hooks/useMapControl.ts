import { useEffect, useRef } from 'react';
import { useMap } from 'react-leaflet';
import L from 'leaflet';

interface UseMapControlOptions {
  userLocation: [number, number] | null;
  destination: [number, number] | null;
  onLocationUpdate: (location: [number, number]) => void;
  zoom?: number;
}

export function useMapControl({
  userLocation,
  destination,
  onLocationUpdate,
  zoom = 12,
}: UseMapControlOptions): void {
  const map = useMap();
  const watchIdRef = useRef<number | null>(null);

  useEffect(() => {
    if (!navigator.geolocation) {
      console.warn('Geolocation is not supported');
      return;
    }

    watchIdRef.current = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const location: [number, number] = [latitude, longitude];
        onLocationUpdate(location);
        
        if (userLocation) {
          map.setView(location, zoom, { animate: true });
        }
      },
      (error) => {
        console.error('Geolocation error:', error);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 5000
      }
    );

    return () => {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
      }
    };
  }, [map, zoom, onLocationUpdate, userLocation]);

  useEffect(() => {
    if (userLocation) {
      map.setView(userLocation, zoom, { animate: true });
    } else if (destination) {
      map.setView(destination, zoom, { animate: true });
    }
  }, [userLocation, destination, map, zoom]);

  useEffect(() => {
    if (userLocation && destination) {
      const bounds = L.latLngBounds([userLocation, destination]);
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [userLocation, destination, map]);
}

