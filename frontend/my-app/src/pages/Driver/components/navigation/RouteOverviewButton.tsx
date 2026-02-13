import React from 'react';
import './RouteOverviewButton.css';

interface RouteOverviewButtonProps {
  onNavigate: () => void;
}

const RouteOverviewButton: React.FC<RouteOverviewButtonProps> = ({ onNavigate }) => {
  return (
    <div className="route-overview-button-container">
      <button
        onClick={onNavigate}
        className="route-overview-button"
      >
        Route Overview
      </button>
    </div>
  );
};

export default RouteOverviewButton;

