import React, { useState, useEffect } from 'react';
import { RouteAssignment, Driver } from '../types';

import './RouteAssignmentPage.css';

interface RouteAssignmentPageProps {
  selectedParcelIds: string[];
  onReturn: () => void;
  onSubmit: (assignments: RouteAssignment[]) => void;
  onTruckClick?: (truckPlateNo: string) => void;
}

// Mock drivers for dropdown
const mockDrivers: Driver[] = [
  { id: 'tom', name: 'Tom', email: 'tom@example.com', phone: '', licenseNumber: '', licenseExpiry: '', status: 'active' },
  { id: 'jack', name: 'Jack', email: 'jack@example.com', phone: '', licenseNumber: '', licenseExpiry: '', status: 'active' },
  { id: 'frank', name: 'Frank', email: 'frank@example.com', phone: '', licenseNumber: '', licenseExpiry: '', status: 'active' },
  { id: 'bob', name: 'Bob', email: 'bob@example.com', phone: '', licenseNumber: '', licenseExpiry: '', status: 'active' }
];

// Generate mock truck plate numbers (deterministic based on index)
const generateTruckPlateNo = (index: number): string => {
  const prefixes = ['R', 'K', 'T', 'H', 'B', 'V', 'N', 'P', 'Z', 'J', 'D'];
  // Use deterministic values based on index to ensure consistency
  const numbers = [965, 381, 947, 520, 194, 803, 672, 415, 209, 738, 856, 852];
  const letters = [
    ['F', 'K'], ['L', 'P'], ['M', 'J'], ['Z', 'R'], ['X', 'N'], ['G', 'C'],
    ['F', 'D'], ['H', 'V'], ['K', 'R'], ['L', 'S'], ['W', 'T'], ['W', 'X']
  ];
  
  const prefix = prefixes[index % prefixes.length];
  const number = numbers[index % numbers.length];
  const letterPair = letters[index % letters.length];
  
  return `${prefix}-${number}-${letterPair[0]}${letterPair[1]}`;
};

export default function RouteAssignmentPage({ selectedParcelIds, onReturn, onSubmit, onTruckClick }: RouteAssignmentPageProps) {
  const [assignments, setAssignments] = useState<RouteAssignment[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const itemsPerPage = 12;

  useEffect(() => {
    // Generate route assignments based on selected parcels
    // For demo, we'll generate 12 assignments (matching the image)
    const generateAssignments = (): RouteAssignment[] => {
      const count = 12; // Total assignments to show
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(17, 0, 0, 0); // Set to 17:00
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      const month = monthNames[tomorrow.getMonth()];
      const day = tomorrow.getDate();
      const dateStr = `${month} ${day}, 17:00`;

      const newAssignments: RouteAssignment[] = [];
      
      // First 4 assignments have pre-assigned drivers
      const preAssignedDrivers = ['tom', 'jack', 'frank', 'bob'];
      
      for (let i = 0; i < count; i++) {
        newAssignments.push({
          id: `assignment-${i + 1}`,
          truckPlateNo: generateTruckPlateNo(i),
          date: dateStr,
          numberOfParcels: 25,
          driverId: i < 4 ? preAssignedDrivers[i] : null
        });
      }
      
      return newAssignments;
    };

    const generated = generateAssignments();
    setAssignments(generated);
    setTotalPages(Math.ceil(generated.length / itemsPerPage));
  }, [selectedParcelIds]);

  const handleDriverChange = (assignmentId: string, driverId: string | null) => {
    setAssignments(prev =>
      prev.map(assignment =>
        assignment.id === assignmentId
          ? { ...assignment, driverId }
          : assignment
      )
    );
  };

  const handleSubmit = () => {
    onSubmit(assignments);
  };

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const getCurrentPageAssignments = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return assignments.slice(startIndex, endIndex);
  };

  const getDriverName = (driverId: string | null): string => {
    if (!driverId) return '';
    const driver = mockDrivers.find(d => d.id === driverId);
    return driver ? driver.name : '';
  };

  return (
    <div className="route-assignment-page">
      <div className="assignment-container">
        <h2 className="assignment-title">Route Assignment</h2>
        
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
                    <td>
                      <select
                        className="driver-select"
                        value={assignment.driverId || ''}
                        onChange={(e) => handleDriverChange(assignment.id, e.target.value === '' ? null : e.target.value)}
                      >
                        <option value=""></option>
                        {mockDrivers.map(driver => (
                          <option key={driver.id} value={driver.id}>
                            {driver.name}
                          </option>
                        ))}
                      </select>
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

        {/* Action Buttons */}
        <div className="action-buttons">
          <button className="return-home-button" onClick={onReturn}>
            Return
          </button>
          <button className="submit-button" onClick={handleSubmit}>
            Submit
          </button>
        </div>
      </div>
    </div>
  );
}
