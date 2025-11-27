import React from 'react';
import { RouteAssignment } from '../../../types';
import DriverNameCell from '../../../components/common/DriverNameCell';
import { DriverResponse } from '../../../services/plannerService';

interface TrackingTableProps {
  assignments: RouteAssignment[];
  currentPage: number;
  itemsPerPage: number;
  drivers: DriverResponse[];
  onTruckClick?: (truckPlateNo: string) => void;
  onTrackClick: (assignment: RouteAssignment) => void;
}

export default function TrackingTable({
  assignments,
  currentPage,
  itemsPerPage,
  drivers,
  onTruckClick,
  onTrackClick
}: TrackingTableProps) {
  return (
    <div className="tracking-table-container">
      <table className="tracking-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>Truck Plate No.</th>
            <th>Date</th>
            <th>No. of Parcels</th>
            <th>Driver</th>
            <th>Track</th>
          </tr>
        </thead>
        <tbody>
          {assignments.map((assignment, index) => {
            const rowNumber = (currentPage - 1) * itemsPerPage + index + 1;
            return (
              <tr key={assignment.id}>
                <td>{rowNumber}</td>
                <td>
                  {onTruckClick ? (
                    <button
                      className="truck-plate-link"
                      onClick={() => onTruckClick(assignment.truckPlateNo)}
                    >
                      {assignment.truckPlateNo}
                    </button>
                  ) : (
                    assignment.truckPlateNo
                  )}
                </td>
                <td>{assignment.date}</td>
                <td>{assignment.numberOfParcels}</td>
                <td>
                  <DriverNameCell driverId={assignment.driverId} drivers={drivers} />
                </td>
                <td>
                  <button
                    className="track-button"
                    onClick={() => onTrackClick(assignment)}
                  >
                    Track
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

