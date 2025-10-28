import React from 'react';
import './LocationPermission.css';

interface LocationPermissionProps {
  onLocationGranted: (location: [number, number]) => void;
  onError: (error: string) => void;
  className?: string;
}

const LocationPermission: React.FC<LocationPermissionProps> = ({ 
  onLocationGranted, 
  onError,
  className = ''
}) => {
  const handleEnableLocation = async () => {
    if (!navigator.geolocation) {
      onError('Geolocation is not supported by this browser');
      return;
    }

    try {
      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 5000
        });
      });

      const location: [number, number] = [position.coords.latitude, position.coords.longitude];
      onLocationGranted(location);
    } catch (error) {
      console.error('Geolocation error:', error);
      onError('Location access denied');
    }
  };

  return (
    <div className={`location-permission ${className}`}>
      <div className="location-permission__icon">📍</div>
      <h3 className="location-permission__title">Location Access Required</h3>
      <p className="location-permission__description">
        To provide navigation guidance, we need access to your current location.
      </p>
      <button 
        className="location-permission__button"
        onClick={handleEnableLocation}
      >
        Enable Location
      </button>
    </div>
  );
};

export default LocationPermission;
