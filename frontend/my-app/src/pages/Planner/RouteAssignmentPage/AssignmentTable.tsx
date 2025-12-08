import React from 'react';
import { RouteAssignment } from '../../../types';
import { DriverResponse } from '../../../services/plannerService';
import DriverSelect from './DriverSelect';

interface AssignmentTableProps {
  assignments: RouteAssignment[];
  currentPage: number;
  itemsPerPage: number;
  availableDrivers: DriverResponse[];
  loading: boolean;
  onTruckClick?: (truckPlateNo: string) => void;
  onDriverChange: (assignmentId: string, driverId: string | null) => void;
}

export default function AssignmentTable({
  assignments,
  currentPage,
  itemsPerPage,
  availableDrivers,
  loading,
  onTruckClick,
  onDriverChange
}: AssignmentTableProps) {
  const getAvailableDriversForRow = (currentAssignmentId: string) => {
    const currentAssignment = assignments.find(a => a.id === currentAssignmentId);
    const currentSelectedDriverId = currentAssignment?.driverId ? parseInt(currentAssignment.driverId, 10) : null;
    
    const otherAssignedDriverIds = assignments
      .filter(a => a.id !== currentAssignmentId && a.driverId)
      .map(a => parseInt(a.driverId!, 10));
    
    const filtered = availableDrivers.filter(driver => 
      !otherAssignedDriverIds.includes(driver.id) && driver.isAvailable
    );
    
    if (currentSelectedDriverId) {
      const selectedDriver = availableDrivers.find(d => d.id === currentSelectedDriverId);
      if (selectedDriver && !filtered.some(d => d.id === currentSelectedDriverId)) {
        filtered.push(selectedDriver);
      }
    }
    
    console.log(`Available drivers for row ${currentAssignmentId}:`, {
      totalAvailable: availableDrivers.length,
      otherAssignedIds: otherAssignedDriverIds,
      filteredCount: filtered.length,
      filteredIds: filtered.map(d => d.id),
      currentSelectedDriverId
    });
    
    return filtered;
  };

  return (
    <div className="assignment-table-container">
      <table className="assignment-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>Truck Plate ID</th>
            <th>Date</th>
            <th>No. of Parcels</th>
            <th>Driver</th>
          </tr>
        </thead>
        <tbody>
          {assignments.map((assignment, index) => {
            const rowNumber = (currentPage - 1) * itemsPerPage + index + 1;
            const driversForThisRow = getAvailableDriversForRow(assignment.id);
            
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
                  <DriverSelect
                    value={assignment.driverId}
                    drivers={driversForThisRow}
                    disabled={loading}
                    onChange={(driverId) => onDriverChange(assignment.id, driverId)}
                  />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

