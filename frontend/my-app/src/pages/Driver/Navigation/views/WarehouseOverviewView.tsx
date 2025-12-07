import React from 'react';
import DriverHeader from '../../components/driverHeader';
import WarehousePackageOverview from '../../components/navigation/WarehousePackageOverview';
import { Route, Package } from '../../../../types';

interface WarehouseOverviewViewProps {
  warehouse: Route['warehouse'];
  packages: Package[];
  onBack: () => void;
  onConfirmCollection: () => void;
}

const WarehouseOverviewView: React.FC<WarehouseOverviewViewProps> = ({
  warehouse,
  packages,
  onBack,
  onConfirmCollection,
}) => {
  if (!warehouse) return null;

  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <WarehousePackageOverview
          warehouse={warehouse}
          packages={packages}
          onConfirmCollection={onConfirmCollection}
          onBack={onBack}
        />
      </div>
    </div>
  );
};

export default WarehouseOverviewView;

