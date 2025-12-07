import React from 'react';
import { Depot } from '../../../../types';
import './DepotInfo.css';

interface DepotInfoProps {
  depot: Depot;
}

const DepotInfo: React.FC<DepotInfoProps> = ({ 
  depot
}) => {
  return (
    <div className="depot-info">
      <div className="depot-info__header">
        <h2>🏢 Return to Depot</h2>
      </div>
      <div className="depot-info__details">
        <p><strong>Depot Address:</strong> {depot.address}</p>
        <p><strong>Location:</strong> {depot.city} {depot.postalCode}</p>
        {depot.name && (
          <p><strong>Depot Name:</strong> {depot.name}</p>
        )}
      </div>
    </div>
  );
};

export default DepotInfo;

