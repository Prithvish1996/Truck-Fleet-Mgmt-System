import React from 'react';
import { Warehouse } from '../../../../types';
import './WarehouseInfo.css';

interface WarehouseInfoProps {
  warehouse: Warehouse;
  packageCount: number;
}

const WarehouseInfo: React.FC<WarehouseInfoProps> = ({ 
  warehouse,
  packageCount
}) => {
  return (
    <div className="warehouse-info">
      <div className="warehouse-info__header">
        <h2>📦 Warehouse Collection</h2>
      </div>
      <div className="warehouse-info__details">
        <p><strong>Warehouse Address:</strong> {warehouse.address}</p>
        <p><strong>Location:</strong> {warehouse.city} {warehouse.postalCode}</p>
        <p><strong>Packages to collect:</strong> {packageCount}</p>
      </div>
    </div>
  );
};

export default WarehouseInfo;

