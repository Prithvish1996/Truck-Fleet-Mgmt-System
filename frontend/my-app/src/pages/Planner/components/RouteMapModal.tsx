import React from 'react';
import { RouteAssignment } from '../../../types/index';
import './RouteMapModal.css';

interface RouteMapModalProps {
  isOpen: boolean;
  assignment: RouteAssignment | null;
  onClose: () => void;
}

export default function RouteMapModal({ isOpen, assignment, onClose }: RouteMapModalProps) {
  if (!isOpen || !assignment) return null;

  return (
    <div className="route-map-modal-overlay" onClick={onClose}>
      <div className="route-map-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="route-map-header">
          <div className="route-map-truck-plate">{assignment.truckPlateNo}</div>
          <button className="route-map-close-button" onClick={onClose}>
            ×
          </button>
        </div>
        <div className="route-map-container">
          <div className="route-map-placeholder">MAP</div>
        </div>
      </div>
    </div>
  );
}
