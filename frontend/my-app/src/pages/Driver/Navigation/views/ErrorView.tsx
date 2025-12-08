import React from 'react';
import DriverHeader from '../../components/driverHeader';
import ErrorMessage from '../../components/ui/ErrorMessage';

interface ErrorViewProps {
  error: string;
  onBack: () => void;
  onRetry: () => void;
}

const ErrorView: React.FC<ErrorViewProps> = ({ error, onBack, onRetry }) => {
  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <ErrorMessage message={error} onRetry={onRetry} />
      </div>
    </div>
  );
};

export default ErrorView;

