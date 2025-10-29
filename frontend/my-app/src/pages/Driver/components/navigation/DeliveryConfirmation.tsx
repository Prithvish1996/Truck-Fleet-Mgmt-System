import React from 'react';
import { Package } from '../../../../types';
import './DeliveryConfirmation.css';

interface DeliveryConfirmationProps {
  package: Package;
  onConfirm: (confirmed: boolean) => void;
}

const DeliveryConfirmation: React.FC<DeliveryConfirmationProps> = ({ 
  package: pkg,
  onConfirm 
}) => {
  return (
    <div className="delivery-confirmation">
      <div className="delivery-confirmation__content">
        <h2 className="delivery-confirmation__title">
          Confirm Delivery
        </h2>
        <p className="delivery-confirmation__question">
          Have you successfully delivered the package to{' '}
          <strong>{pkg.recipientName}</strong>?
        </p>
        <div className="delivery-confirmation__package-info">
          <p><strong>Package:</strong> {pkg.name}</p>
          <p><strong>Address:</strong> {pkg.address}, {pkg.city}</p>
        </div>
        <div className="delivery-confirmation__actions">
          <button
            onClick={() => onConfirm(true)}
            className="delivery-confirmation__button delivery-confirmation__button--confirm"
          >
            ✓ Yes, Delivered
          </button>
          <button
            onClick={() => onConfirm(false)}
            className="delivery-confirmation__button delivery-confirmation__button--deny"
          >
            ✗ Not Delivered
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeliveryConfirmation;
