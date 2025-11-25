import React from 'react';
import PriorityBadge from '../common/PriorityBadge';

interface RequestItem {
  id: string;
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  priority: 'High' | 'Medium' | 'Low';
  parcelIds: number[];
}

interface RequestTableProps {
  requests: RequestItem[];
  selectedRequests: Set<string>;
  onCheckboxChange: (requestId: string) => void;
  onSelectAll: () => void;
}

export default function RequestTable({
  requests,
  selectedRequests,
  onCheckboxChange,
  onSelectAll
}: RequestTableProps) {
  return (
    <div className="parcel-table-container">
      <table className="parcel-table">
        <thead>
          <tr>
            <th className="checkbox-column">
              <input
                type="checkbox"
                checked={selectedRequests.size === requests.length && requests.length > 0}
                onChange={onSelectAll}
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
          {requests.map((request) => (
            <tr key={request.id} className={selectedRequests.has(request.id) ? 'selected' : ''}>
              <td className="checkbox-column">
                <input
                  type="checkbox"
                  checked={selectedRequests.has(request.id)}
                  onChange={() => onCheckboxChange(request.id)}
                  className="parcel-checkbox"
                />
              </td>
              <td>{request.truckPlateId}</td>
              <td>{request.deliveryDate}</td>
              <td>{request.parcels}</td>
              <td>{request.warehouse}</td>
              <td>
                <PriorityBadge priority={request.priority} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

