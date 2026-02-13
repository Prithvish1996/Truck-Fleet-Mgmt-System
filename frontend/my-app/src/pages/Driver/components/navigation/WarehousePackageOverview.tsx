import React from 'react';
import { Warehouse, Package } from '../../../../types';
import './WarehousePackageOverview.css';

interface WarehousePackageOverviewProps {
  warehouse: Warehouse;
  packages: Package[];
  onConfirmCollection: () => void;
  onBack: () => void;
}

const WarehousePackageOverview: React.FC<WarehousePackageOverviewProps> = ({ 
  warehouse,
  packages,
  onConfirmCollection,
  onBack
}) => {
  const packagesToCollect = packages.filter(pkg => pkg.status === 'pending');

  return (
    <div className="warehouse-package-overview">
      <div className="warehouse-package-overview__header">
        <h2>📦 Warehouse Collection</h2>
        <p className="warehouse-package-overview__subtitle">
          Review and confirm collection of packages at {warehouse.city}
        </p>
      </div>

      <div className="warehouse-package-overview__warehouse-info">
        <div className="warehouse-package-overview__warehouse-details">
          <p><strong>Warehouse Address:</strong> {warehouse.address}</p>
          <p><strong>Location:</strong> {warehouse.city} {warehouse.postalCode}</p>
        </div>
      </div>

      <div className="warehouse-package-overview__packages-section">
        <h3 className="warehouse-package-overview__packages-title">
          Packages to Collect ({packagesToCollect.length})
        </h3>
        <div className="warehouse-package-overview__packages-list">
          {packagesToCollect.map((pkg, index) => (
            <div key={pkg.id} className="warehouse-package-overview__package-item">
              <div className="warehouse-package-overview__package-number">
                {index + 1}
              </div>
              <div className="warehouse-package-overview__package-details">
                <div className="warehouse-package-overview__package-name">
                  {pkg.name}
                </div>
                <div className="warehouse-package-overview__package-info">
                  <span>Recipient: {pkg.recipientName}</span>
                  <span>Weight: {pkg.weight} kg</span>
                </div>
                <div className="warehouse-package-overview__package-address">
                  {pkg.address}, {pkg.city} {pkg.postalCode}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="warehouse-package-overview__actions">
        <button
          onClick={onBack}
          className="warehouse-package-overview__button warehouse-package-overview__button--back"
        >
          ← Back
        </button>
        <button
          onClick={onConfirmCollection}
          className="warehouse-package-overview__button warehouse-package-overview__button--start"
        >
          ✓ Confirm Collection
        </button>
      </div>
    </div>
  );
};

export default WarehousePackageOverview;

