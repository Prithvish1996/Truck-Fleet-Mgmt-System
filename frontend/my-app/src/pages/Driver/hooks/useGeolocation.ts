import { useEffect, useRef, useState } from 'react';

interface UseGeolocationOptions {
  enableHighAccuracy?: boolean;
  timeout?: number;
  maximumAge?: number;
  watch?: boolean;
}

interface UseGeolocationReturn {
  location: [number, number] | null;
  error: string | null;
  loading: boolean;
}

export function useGeolocation(
  options: UseGeolocationOptions = {},
  watch: boolean = true
): UseGeolocationReturn {
  const [location, setLocation] = useState<[number, number] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const watchIdRef = useRef<number | null>(null);

  const {
    enableHighAccuracy = true,
    timeout = 10000,
    maximumAge = 5000,
  } = options;

  useEffect(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser');
      setLoading(false);
      return;
    }

    const successCallback = (position: GeolocationPosition) => {
      const { latitude, longitude } = position.coords;
      setLocation([latitude, longitude]);
      setError(null);
      setLoading(false);
    };

    const errorCallback = (err: GeolocationPositionError) => {
      setError(err.message);
      setLoading(false);
    };

    const geoOptions: PositionOptions = {
      enableHighAccuracy,
      timeout,
      maximumAge,
    };

    if (watch) {
      watchIdRef.current = navigator.geolocation.watchPosition(
        successCallback,
        errorCallback,
        geoOptions
      );
    } else {
      navigator.geolocation.getCurrentPosition(
        successCallback,
        errorCallback,
        geoOptions
      );
    }

    return () => {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current);
      }
    };
  }, [enableHighAccuracy, timeout, maximumAge, watch]);

  return {
    location,
    error,
    loading,
  };
}

