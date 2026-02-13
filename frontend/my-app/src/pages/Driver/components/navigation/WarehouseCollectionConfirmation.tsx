import React from 'react';
import { Warehouse } from '../../../../types';
import './WarehouseCollectionConfirmation.css';

interface WarehouseCollectionConfirmationProps {
  warehouse: Warehouse;
  packageCount: number;
  onConfirm: (confirmed: boolean) => void;
}

const WarehouseCollectionConfirmation: React.FC<WarehouseCollectionConfirmationProps> = ({ 
  warehouse,
  packageCount,
  onConfirm 
}) => {
  return (
    <div className="warehouse-collection-confirmation">
      <div className="warehouse-collection-confirmation__content">
        <h2 className="warehouse-collection-confirmation__title">
          Confirm Package Collection
        </h2>
        <p className="warehouse-collection-confirmation__question">
          Have you successfully collected all packages from the warehouse?
        </p>
        <div className="warehouse-collection-confirmation__warehouse-info">
          <p><strong>Warehouse Address:</strong> {warehouse.address}</p>
          <p><strong>Location:</strong> {warehouse.city} {warehouse.postalCode}</p>
          <p><strong>Packages to collect:</strong> {packageCount}</p>
        </div>
        <div className="warehouse-collection-confirmation__actions">
          <button
            onClick={() => onConfirm(true)}
            className="warehouse-collection-confirmation__button warehouse-collection-confirmation__button--confirm"
          >
            ✓ Yes, Collected
          </button>
          <button
            onClick={() => onConfirm(false)}
            className="warehouse-collection-confirmation__button warehouse-collection-confirmation__button--deny"
          >
            ✗ Not Collected
          </button>
        </div>
      </div>
    </div>
  );
};

export default WarehouseCollectionConfirmation;

