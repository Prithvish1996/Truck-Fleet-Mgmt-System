import React from 'react';
import DriverHeader from '../../components/driverHeader';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

interface LoadingViewProps {
  onBack: () => void;
}

const LoadingView: React.FC<LoadingViewProps> = ({ onBack }) => {
  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <LoadingSpinner size="large" message="Loading packages..." />
      </div>
    </div>
  );
};

export default LoadingView;

