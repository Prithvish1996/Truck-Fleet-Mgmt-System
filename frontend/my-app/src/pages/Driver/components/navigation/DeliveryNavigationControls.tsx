import React from 'react';
import './DeliveryNavigationControls.css';

interface DeliveryNavigationControlsProps {
  state: 'showing_navigation' | 'waiting_confirmation';
  onOpenNavigation?: () => void;
}

const DeliveryNavigationControls: React.FC<DeliveryNavigationControlsProps> = ({
  state,
  onOpenNavigation
}) => {
  if (state === 'showing_navigation') {
    return (
      <div className="delivery-navigation-controls">
        <button
          onClick={onOpenNavigation}
          className="delivery-navigation-controls__button delivery-navigation-controls__button--primary"
        >
          Open in Google Maps
        </button>
        <p className="delivery-navigation-controls__help-text">
          Click to open Google Maps app and navigate to the delivery address
        </p>
      </div>
    );
  }

  return null;
};

export default DeliveryNavigationControls;

