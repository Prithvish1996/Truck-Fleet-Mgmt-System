import React from 'react';

type ScheduleParcel = {
  id: string;
  parcelId: number;
  receiver: string;
  location: string;
  warehouse: string;
  status: 'Pending' | 'Scheduled';
  selectable: boolean;
  weight?: number;
  volume?: number;
  phone?: string;
  deliveryInstructions?: string;
  createdAt?: string;
};

interface ScheduleTableProps {
  parcels: ScheduleParcel[];
  selectedParcels: string[];
  onParcelToggle: (parcelId: string) => void;
}

export default function ScheduleTable({ parcels, selectedParcels, onParcelToggle }: ScheduleTableProps) {
  if (parcels.length === 0) {
    return (
      <div className="schedule-message" style={{ 
        padding: '40px', 
        textAlign: 'center',
        color: '#61716d'
      }}>
        No parcels found. Please try adjusting your filters or search criteria.
      </div>
    );
  }

  return (
    <div className="schedule-table-wrapper">
      <table className="schedule-table">
        <thead>
          <tr>
            <th aria-label="Select parcel" />
            <th>Parcel ID</th>
            <th>Receiver</th>
            <th>Delivery Location</th>
            <th>Warehouse</th>
            <th>Status</th>
            <th>Weight</th>
            <th>Volume</th>
            <th>Phone</th>
          </tr>
        </thead>
        <tbody>
          {parcels.map(parcel => {
            const isSelected = selectedParcels.includes(parcel.id);
            return (
              <tr key={parcel.id} className={!parcel.selectable ? 'schedule-row-disabled' : ''}>
                <td>
                  <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={() => onParcelToggle(parcel.id)}
                    disabled={!parcel.selectable}
                    aria-label={`Select parcel ${parcel.id}`}
                  />
                </td>
                <td>{parcel.id}</td>
                <td>{parcel.receiver}</td>
                <td>{parcel.location}</td>
                <td>{parcel.warehouse}</td>
                <td className={`schedule-status ${parcel.status.toLowerCase()}`}>
                  {parcel.status}
                </td>
                <td>{parcel.weight ? `${parcel.weight} kg` : 'N/A'}</td>
                <td>{parcel.volume ? `${parcel.volume} m³` : 'N/A'}</td>
                <td>{parcel.phone || 'N/A'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

