import React, { useState, useEffect } from 'react';
import { ParcelRequest } from '../types';
import './RoutePlanningModal.css';

interface RoutePlanningModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerateRoute: (selectedParcels: string[]) => void;
}

const mockParcelRequests: ParcelRequest[] = [
  {
    id: 'R1234-1',
    customer: 'Amazon',
    warehouse: 'The Hague',
    deliveryLocation: 'Deventer',
    priority: 'High'
  },
  {
    id: 'R1234-2',
    customer: 'Amazon',
    warehouse: 'The Hague',
    deliveryLocation: 'Deventer',
    priority: 'Low'
  },
  {
    id: 'R1234-3',
    customer: 'Amazon',
    warehouse: 'The Hague',
    deliveryLocation: 'Deventer',
    priority: 'Low'
  },
  {
    id: 'R1234-4',
    customer: 'Amazon',
    warehouse: 'Amsterdam',
    deliveryLocation: 'Deventer',
    priority: 'Medium'
  },
  {
    id: 'R1234-5',
    customer: 'Amazon',
    warehouse: 'Hoofddorp',
    deliveryLocation: 'Deventer',
    priority: 'Medium'
  },
  {
    id: 'R1234-6',
    customer: 'Amazon',
    warehouse: 'The Hague',
    deliveryLocation: 'Deventer',
    priority: 'High'
  },
  {
    id: 'R1234-7',
    customer: 'Amazon',
    warehouse: 'Amsterdam',
    deliveryLocation: 'Deventer',
    priority: 'Low'
  },
  {
    id: 'R1234-8',
    customer: 'Amazon',
    warehouse: 'Rotterdam',
    deliveryLocation: 'Deventer',
    priority: 'Low'
  },
  {
    id: 'R1234-9',
    customer: 'Amazon',
    warehouse: 'Rotterdam',
    deliveryLocation: 'Deventer',
    priority: 'Medium'
  }
];

export default function RoutePlanningModal({ isOpen, onClose, onGenerateRoute }: RoutePlanningModalProps) {
  const [parcels, setParcels] = useState<ParcelRequest[]>([]);
  const [selectedParcels, setSelectedParcels] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (isOpen) {
      // Load tomorrow's requests (mock data for now)
      setParcels(mockParcelRequests);
      setSelectedParcels(new Set()); // Reset selection when modal opens
    }
  }, [isOpen]);

  const handleCheckboxChange = (parcelId: string) => {
    setSelectedParcels(prev => {
      const newSet = new Set(prev);
      if (newSet.has(parcelId)) {
        newSet.delete(parcelId);
      } else {
        newSet.add(parcelId);
      }
      return newSet;
    });
  };

  const handleSelectAll = () => {
    if (selectedParcels.size === parcels.length) {
      setSelectedParcels(new Set());
    } else {
      setSelectedParcels(new Set(parcels.map(p => p.id)));
    }
  };

  const handleGenerateRoute = () => {
    if (selectedParcels.size > 0) {
      onGenerateRoute(Array.from(selectedParcels));
      onClose();
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'High':
        return '#ff4444'; // Red
      case 'Medium':
        return '#4CAF50'; // Green
      case 'Low':
        return '#2196F3'; // Blue
      default:
        return '#666';
    }
  };

  if (!isOpen) return null;

  return (
    <div className="route-planning-modal-overlay" onClick={onClose}>
      <div className="route-planning-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Tomorrow's Requests</h2>
          <button className="modal-close-button" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-content">
          <div className="parcel-table-container">
            <table className="parcel-table">
              <thead>
                <tr>
                  <th>
                    <input
                      type="checkbox"
                      checked={selectedParcels.size === parcels.length && parcels.length > 0}
                      onChange={handleSelectAll}
                      className="select-all-checkbox"
                    />
                  </th>
                  <th>Request ID</th>
                  <th>Customer</th>
                  <th>Warehouse</th>
                  <th>Delivery Location</th>
                  <th>Priority</th>
                </tr>
              </thead>
              <tbody>
                {parcels.map((parcel) => (
                  <tr key={parcel.id} className={selectedParcels.has(parcel.id) ? 'selected' : ''}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedParcels.has(parcel.id)}
                        onChange={() => handleCheckboxChange(parcel.id)}
                        className="parcel-checkbox"
                      />
                    </td>
                    <td>{parcel.id}</td>
                    <td>{parcel.customer}</td>
                    <td>{parcel.warehouse}</td>
                    <td>{parcel.deliveryLocation}</td>
                    <td>
                      <span 
                        className="priority-badge"
                        style={{ color: getPriorityColor(parcel.priority) }}
                      >
                        {parcel.priority}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          
          <div className="modal-footer">
            <div className="selected-count">
              {selectedParcels.size > 0 
                ? `${selectedParcels.size} parcel${selectedParcels.size > 1 ? 's' : ''} selected`
                : 'No parcels selected'}
            </div>
            <button 
              className="generate-route-button"
              onClick={handleGenerateRoute}
              disabled={selectedParcels.size === 0}
            >
              Generate Route
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
