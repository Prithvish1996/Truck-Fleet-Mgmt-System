import React, { useState, useEffect } from 'react';
import './NavigationControls.css';

interface NavigationControlsProps {
  currentHeading: number;
  routeData?: any; // GraphhopperRoute
}

const NavigationControls: React.FC<NavigationControlsProps> = ({
  currentHeading,
  routeData
}) => {
  const [headingDisplay, setHeadingDisplay] = useState<string>('N');

  // Update heading display
  useEffect(() => {
    const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
    const index = Math.round(currentHeading / 45) % 8;
    setHeadingDisplay(directions[index]);
  }, [currentHeading]);

  // Format route information
  const formatDistance = (meters: number): string => {
    if (meters < 1000) {
      return `${Math.round(meters)}m`;
    }
    return `${(meters / 1000).toFixed(1)}km`;
  };

  const formatTime = (milliseconds: number): string => {
    const totalSeconds = Math.round(milliseconds / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  };

  return (
    <div className="navigation-controls">
      {/* Heading Display */}
      <div className="navigation-controls__section navigation-controls__section--heading">
        <div className="navigation-controls__heading">
          <div className="navigation-controls__heading-direction">
            {headingDisplay}
          </div>
          <div className="navigation-controls__heading-degrees">
            {Math.round(currentHeading)}°
          </div>
        </div>
      </div>

      {/* Route Information */}
      {routeData && (
        <div className="navigation-controls__section navigation-controls__section--route">
          <div className="navigation-controls__route-info">
            <div className="navigation-controls__route-distance">
              {formatDistance(routeData.paths[0]?.distance || 0)}
            </div>
            <div className="navigation-controls__route-time">
              {formatTime(routeData.paths[0]?.time || 0)}
            </div>
          </div>
        </div>
      )}

      {/* Status Indicator */}
      <div className="navigation-controls__section">
        <div className="navigation-controls__status navigation-controls__status--navigating">
          <div className="navigation-controls__status-dot"></div>
          <span className="navigation-controls__status-text">
            Navigating
          </span>
        </div>
      </div>
    </div>
  );
};

export default NavigationControls;
