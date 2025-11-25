import React, { useState, useEffect } from 'react';
import { plannerService, ParcelResponse } from '../../services/plannerService';
import { formatDate } from '../../utils/dataTransformers';
import RequestTable from './RequestTable';
import '../RoutePlanningModal.css';

interface RoutePlanningModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerateRoute: (selectedParcelIds: string[]) => void;
}

interface RequestItem {
  id: string;
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  priority: 'High' | 'Medium' | 'Low';
  parcelIds: number[];
}

export default function RoutePlanningModal({ isOpen, onClose, onGenerateRoute }: RoutePlanningModalProps) {
  const [requests, setRequests] = useState<RequestItem[]>([]);
  const [selectedRequests, setSelectedRequests] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      loadScheduledDeliveries();
      setSelectedRequests(new Set());
    }
  }, [isOpen]);

  const loadScheduledDeliveries = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await plannerService.getScheduledDeliveries(undefined, 0, 100);
      
      const grouped = new Map<string, ParcelResponse[]>();
      data.data.forEach(parcel => {
        const key = `${parcel.warehouseId}-${parcel.plannedDeliveryDate || 'no-date'}`;
        if (!grouped.has(key)) {
          grouped.set(key, []);
        }
        grouped.get(key)!.push(parcel);
      });

      const requestItems: RequestItem[] = Array.from(grouped.entries()).map(([key, parcels], index) => {
        const firstParcel = parcels[0];
        const deliveryDate = firstParcel.plannedDeliveryDate 
          ? formatDate(firstParcel.plannedDeliveryDate)
          : 'TBD';
        
        return {
          id: `request-${key}-${index}`,
          truckPlateId: 'TBD',
          deliveryDate,
          parcels: parcels.length,
          warehouse: firstParcel.warehouseCity || 'Unknown',
          priority: 'Medium' as const,
          parcelIds: parcels.map(p => p.parcelId)
        };
      });

      setRequests(requestItems);
    } catch (err: any) {
      console.error('Error loading scheduled deliveries:', err);
      setError(err.message || 'Failed to load requests. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckboxChange = (requestId: string) => {
    setSelectedRequests(prev => {
      const newSet = new Set(prev);
      if (newSet.has(requestId)) {
        newSet.delete(requestId);
      } else {
        newSet.add(requestId);
      }
      return newSet;
    });
  };

  const handleSelectAll = () => {
    if (selectedRequests.size === requests.length) {
      setSelectedRequests(new Set());
    } else {
      setSelectedRequests(new Set(requests.map(r => r.id)));
    }
  };

  const handleGenerateRoute = async () => {
    if (selectedRequests.size > 0) {
      setLoading(true);
      setError('');
      try {
        const selectedRequestItems = requests.filter(r => selectedRequests.has(r.id));
        const allParcelIds = selectedRequestItems.flatMap(r => r.parcelIds);
        
        if (allParcelIds.length === 0) {
          setError('No parcels selected.');
          return;
        }

        const depotId = 1;

        await plannerService.generateRoutes({
          depot_id: depotId,
          parcelIds: allParcelIds
        });

        const parcelIdStrings = allParcelIds.map(id => `P${id}`);
        onGenerateRoute(parcelIdStrings);
        onClose();
      } catch (err: any) {
        console.error('Error generating routes:', err);
        setError(err.message || 'Failed to generate routes. Please try again.');
      } finally {
        setLoading(false);
      }
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
          {loading && (
            <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
          )}
          
          {error && (
            <div style={{ padding: '20px', color: 'red', textAlign: 'center' }}>{error}</div>
          )}

          {!loading && !error && (
            <RequestTable
              requests={requests}
              selectedRequests={selectedRequests}
              onCheckboxChange={handleCheckboxChange}
              onSelectAll={handleSelectAll}
            />
          )}
          
          <div className="modal-footer">
            <button 
              className="generate-route-button"
              onClick={handleGenerateRoute}
              disabled={selectedRequests.size === 0 || loading}
            >
              Generate Route
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

