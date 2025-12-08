import React from 'react';
import DriverHeader from '../../components/driverHeader';
import LocationPermission from '../../components/navigation/LocationPermission';

interface LocationPermissionViewProps {
  onBack: () => void;
  onLocationGranted: (location: [number, number]) => void;
  onError: (error: string) => void;
}

const LocationPermissionView: React.FC<LocationPermissionViewProps> = ({
  onBack,
  onLocationGranted,
  onError,
}) => {
  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <LocationPermission
          onLocationGranted={onLocationGranted}
          onError={onError}
        />
      </div>
    </div>
  );
};

export default LocationPermissionView;

