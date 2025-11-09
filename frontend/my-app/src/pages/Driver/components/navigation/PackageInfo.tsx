import React from 'react';
import { Package } from '../../../../types';
import { formatTravelTime } from '../../../../utils/timeFormatter';
import './PackageInfo.css';

interface PackageInfoProps {
  package: Package;
  packageNumber: number;
  totalPackages: number;
  estimatedTime?: number | null;
}

const PackageInfo: React.FC<PackageInfoProps> = ({ 
  package: pkg,
  packageNumber,
  totalPackages,
  estimatedTime
}) => {
  return (
    <div className="package-info">
      <div className="package-info__header">
        <h2>Package {packageNumber} of {totalPackages}</h2>
      </div>
      <div className="package-info__details">
        <p><strong>Recipient:</strong> {pkg.recipientName}</p>
        <p><strong>Address:</strong> {pkg.address}, {pkg.city}</p>
        {pkg.deliveryInstructions && (
          <p><strong>Instructions:</strong> {pkg.deliveryInstructions}</p>
        )}
        {estimatedTime && (
          <p className="package-info__estimate">
            <strong>Estimated time:</strong> {formatTravelTime(estimatedTime)}
          </p>
        )}
      </div>
    </div>
  );
};

export default PackageInfo;

