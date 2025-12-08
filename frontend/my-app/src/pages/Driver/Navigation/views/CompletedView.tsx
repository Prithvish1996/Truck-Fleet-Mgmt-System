import React from 'react';
import DriverHeader from '../../components/driverHeader';
import CompletedState from '../../components/navigation/CompletedState';

interface CompletedViewProps {
  onBack: () => void;
  onComplete: () => Promise<void>;
}

const CompletedView: React.FC<CompletedViewProps> = ({ onBack, onComplete }) => {
  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <CompletedState onComplete={onComplete} />
      </div>
    </div>
  );
};

export default CompletedView;

