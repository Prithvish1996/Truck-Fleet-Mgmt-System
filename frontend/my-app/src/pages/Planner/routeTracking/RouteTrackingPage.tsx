import React, { useState } from 'react';
import { RouteAssignment } from '../../../types/index';
import RouteMapModal from '../components/RouteMapModal';
import './RouteTrackingPage.css';

interface RouteTrackingPageProps {
  assignments: RouteAssignment[];
  onReturn: () => void;
  onTrack: (assignment: RouteAssignment) => void;
  onTruckClick?: (truckPlateNo: string) => void;
}

// Get driver name from assignment
const getDriverName = (driverId: string | null): string => {
  if (!driverId) return 'Unassigned';
  const driverMap: { [key: string]: string } = {
    'tom': 'Tom',
    'jack': 'Jack',
    'frank': 'Frank',
    'bob': 'Bob'
  };
  return driverMap[driverId] || 'Tom'; // Default to Tom if not found
};

export default function RouteTrackingPage({ assignments, onReturn, onTrack, onTruckClick }: RouteTrackingPageProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [isMapModalOpen, setIsMapModalOpen] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<RouteAssignment | null>(null);
  const itemsPerPage = 12;
  const totalPages = Math.ceil(assignments.length / itemsPerPage);

  // Filter out assignments without drivers
  const assignedRoutes = assignments.filter(a => a.driverId !== null);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const getCurrentPageAssignments = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return assignedRoutes.slice(startIndex, endIndex);
  };

  const handleTrackClick = (assignment: RouteAssignment) => {
    setSelectedAssignment(assignment);
    setIsMapModalOpen(true);
    onTrack(assignment);
  };

  const handleCloseMap = () => {
    setIsMapModalOpen(false);
    setSelectedAssignment(null);
  };

  return (
    <div className="route-tracking-page">
      <div className="tracking-container">
        <h2 className="tracking-title">Route Tracking</h2>
        
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
              {getCurrentPageAssignments().map((assignment, index) => {
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
                    <td>{getDriverName(assignment.driverId)}</td>
                    <td>
                      <button
                        className="track-button"
                        onClick={() => handleTrackClick(assignment)}
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

        {/* Pagination */}
        <div className="pagination">
          <button
            className="pagination-button"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            Previous page
          </button>
                    <div className="pagination-numbers">
            {Array.from({ length: Math.min(7, totalPages) }, (_, i) => {     
              let pageNum: number;
              if (totalPages <= 7) {
                pageNum = i + 1;
              } else if (currentPage <= 4) {
                pageNum = i + 1;
              } else if (currentPage >= totalPages - 3) {
                pageNum = totalPages - 6 + i;
              } else {
                pageNum = currentPage - 3 + i;
              }
              
              return (
                <button
                  key={pageNum}
                  className={`pagination-number ${currentPage === pageNum ? 'active' : ''}`}
                  onClick={() => handlePageChange(pageNum)}
                >
                  {pageNum}
                </button>
              );
            })}
            {totalPages > 7 && <span className="pagination-ellipsis">...</span>}
          </div>
          <button
            className="pagination-button"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            Next page
          </button>
        </div>

        {/* Return Button */}
        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>

      {/* Route Map Modal */}
      <RouteMapModal
        isOpen={isMapModalOpen}
        assignment={selectedAssignment}
        onClose={handleCloseMap}
      />
    </div>
  );
}
