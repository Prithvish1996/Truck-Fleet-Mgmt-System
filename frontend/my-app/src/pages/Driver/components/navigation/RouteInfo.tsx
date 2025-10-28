import React from 'react';
import { Package } from '../../../../types';
import { GraphhopperRoute } from '../../../../services/graphhopperService';
import './RouteInfo.css';

interface RouteInfoProps {
  package: Package;
  routeData: GraphhopperRoute | null;
  className?: string;
}

// Helper function to format time from milliseconds to hours and minutes
const formatTime = (timeInMs: number): string => {
  const totalSeconds = Math.round(timeInMs / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  } else {
    return `${minutes}m`;
  }
};

const RouteInfo: React.FC<RouteInfoProps> = ({ package: pkg, routeData, className = '' }) => {
  return (
    <div className={`route-info ${className}`}>
      <div className="route-info__header">
        <h3 className="route-info__title">Next Delivery</h3>
      </div>
      
      <div className="route-info__package">
        <h4 className="route-info__package-name">{pkg.name}</h4>
        <p className="route-info__package-address">
          {pkg.address}, {pkg.city}
        </p>
        <p className="route-info__package-recipient">
          Recipient: {pkg.recipientName}
        </p>
      </div>
      
      <div className="route-info__stats">
        <h4 className="route-info__stats-title">Route Information</h4>
        
        {routeData && routeData.paths && routeData.paths.length > 0 ? (
          <div className="route-info__stats-grid">
            <div className="route-info__stat">
              <span className="route-info__stat-icon">📏</span>
              <span className="route-info__stat-label">Distance:</span>
              <span className="route-info__stat-value">
                {(routeData.paths[0].distance / 1000).toFixed(1)} km
              </span>
            </div>
            <div className="route-info__stat">
              <span className="route-info__stat-icon">⏱️</span>
              <span className="route-info__stat-label">Time:</span>
              <span className="route-info__stat-value">
                {formatTime(routeData.paths[0].time)}
              </span>
            </div>
          </div>
        ) : (
          <div className="route-info__loading">
            <span className="route-info__loading-icon">🔄</span>
            <span className="route-info__loading-text">Calculating route...</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default RouteInfo;
