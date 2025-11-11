import React, { useState, useEffect } from 'react';
import './RoutePlanningModal.css';

interface RoutePlanningModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerateRoute: (selectedParcels: string[]) => void;
}

const mockParcelRequests = [
  {
    id: 'R-965-FK',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '15',
    priority: 'High'
  },
  {
    id: 'K-381-LP',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '20',
    priority: 'Low'
  },
  {
    id: 'T-947-MJ',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Low'
  },
  {
    id: 'H-520-ZR',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Medium'
  },
  {
    id: 'B-194-XN',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Medium'
  },
  {
    id: 'V-803-GC',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'High'
  },
  {
    id: 'N-672-FD',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Low'
  },
  {
    id: 'P-415-HV',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Low'
  },
  {
    id: 'Z-209-KR',
    customer: "Oct 10, 17:00",
    warehouse: 'Amazon DUS2',
    deliveryLocation: '22',
    priority: 'Medium'
  }
];

export default function RoutePlanningModal({ isOpen, onClose, onGenerateRoute }: RoutePlanningModalProps) {
  const [parcels, setParcels] = useState<any[]>([]);
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
        return '#ff4444';
      case 'Medium':
        return '#2f8b56';
      case 'Low':
        return '#2196F3';
      default:
        return '#666';
    }
  };

  if (!isOpen) return null;

  return (
    <div className="route-planning-modal-overlay" onClick={onClose}>
      <div className="route-planning-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">New Requests</h2>
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
                  <th>Truck Plate ID</th>
                  <th>Delivery Date</th>
                  <th>No. of Parcels</th>
                  <th>Warehouse</th>
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
                    <td>{parcel.deliveryLocation}</td>
                    <td>{parcel.warehouse}</td>
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
                ? `${selectedParcels.size} request${selectedParcels.size > 1 ? 's' : ''} selected`
                : 'No requests selected'}
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
