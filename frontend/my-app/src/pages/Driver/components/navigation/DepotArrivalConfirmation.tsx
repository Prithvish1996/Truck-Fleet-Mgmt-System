import React from 'react';
import { Depot } from '../../../../types';
import './DepotArrivalConfirmation.css';

interface DepotArrivalConfirmationProps {
  depot: Depot;
  onConfirm: (confirmed: boolean) => void;
}

const DepotArrivalConfirmation: React.FC<DepotArrivalConfirmationProps> = ({ 
  depot,
  onConfirm 
}) => {
  return (
    <div className="depot-arrival-confirmation">
      <div className="depot-arrival-confirmation__content">
        <h2 className="depot-arrival-confirmation__title">
          Confirm Depot Arrival
        </h2>
        <p className="depot-arrival-confirmation__question">
          Have you successfully arrived at the depot?
        </p>
        <div className="depot-arrival-confirmation__depot-info">
          <p><strong>Depot Address:</strong> {depot.address}</p>
          <p><strong>Location:</strong> {depot.city} {depot.postalCode}</p>
          {depot.name && (
            <p><strong>Depot Name:</strong> {depot.name}</p>
          )}
        </div>
        <div className="depot-arrival-confirmation__actions">
          <button
            onClick={() => onConfirm(true)}
            className="depot-arrival-confirmation__button depot-arrival-confirmation__button--confirm"
          >
            ✓ Yes, Arrived
          </button>
          <button
            onClick={() => onConfirm(false)}
            className="depot-arrival-confirmation__button depot-arrival-confirmation__button--deny"
          >
            ✗ Not Yet
          </button>
        </div>
      </div>
    </div>
  );
};

export default DepotArrivalConfirmation;

