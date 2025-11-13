import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../types';
import { plannerService, RouteResponse, DriverResponse } from '../services/plannerService';
import { formatDate, countParcelsInRoute, extractParcelId } from '../utils/dataTransformers';
import './RouteAssignmentPage.css';

interface RouteAssignmentPageProps {
  selectedParcelIds: string[];
  onReturn: () => void;
  onSubmit: (assignments: RouteAssignment[]) => void;
  onTruckClick?: (truckPlateNo: string) => void;
}

export default function RouteAssignmentPage({ selectedParcelIds, onReturn, onSubmit, onTruckClick }: RouteAssignmentPageProps) {
  const [assignments, setAssignments] = useState<RouteAssignment[]>([]);
  const [availableDrivers, setAvailableDrivers] = useState<DriverResponse[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const itemsPerPage = 12;

  useEffect(() => {
    loadUnassignedRoutes();
    loadAvailableDrivers();
  }, [selectedParcelIds]);

  const loadUnassignedRoutes = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await plannerService.getUnassignedRoutes();
      
      // Convert RouteResponse[] to RouteAssignment[]
      // Use unAssignedRoute (status=PLANNED) from the response
      const routesToAssign = data.unAssignedRoute || [];
      
      if (routesToAssign.length === 0) {
        setError('No unassigned routes available. Routes may have already been generated.');
        setLoading(false);
        return;
      }

      const routeAssignments: RouteAssignment[] = routesToAssign.map((route, index) => {
        const numberOfParcels = countParcelsInRoute(route);
        const date = route.startTime ? formatDate(route.startTime) : 'TBD';
        
        return {
          id: `assignment-${route.routeId || index}`,
          truckPlateNo: route.truckPlateNumber || `Truck-${route.truckId || index}`,
          date,
          numberOfParcels,
          driverId: route.driverId?.toString() || null,
          routeId: route.routeId,
          truckId: route.truckId
        };
      });

      setAssignments(routeAssignments);
      setTotalPages(Math.ceil(routeAssignments.length / itemsPerPage));
    } catch (err: any) {
      console.error('Error loading unassigned routes:', err);
      setError(err.message || 'Failed to load routes. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const loadAvailableDrivers = async () => {
    try {
      const drivers = await plannerService.getAvailableDrivers();
      setAvailableDrivers(drivers);
    } catch (err: any) {
      console.error('Error loading available drivers:', err);
    }
  };

  const handleDriverChange = (assignmentId: string, driverId: string | null) => {
    setAssignments(prev =>
      prev.map(assignment =>
        assignment.id === assignmentId
          ? { ...assignment, driverId }
          : assignment
      )
    );
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError('');

    try {
      // Filter assignments that have drivers selected and required IDs
      const assignmentsToSubmit = assignments.filter(
        assignment => assignment.driverId && assignment.routeId && assignment.truckId
      );

      if (assignmentsToSubmit.length === 0) {
        setError('Please select at least one driver for the routes.');
        setLoading(false);
        return;
      }

      // Assign drivers to all routes
      const assignmentPromises = assignmentsToSubmit.map(assignment =>
        plannerService.assignDriverToRoute({
          routId: assignment.routeId!,
          truckId: assignment.truckId!,
          driverId: parseInt(assignment.driverId!, 10)
        })
      );

      await Promise.all(assignmentPromises);
      
      // Pass only the assignments with drivers assigned to parent
      onSubmit(assignmentsToSubmit);
    } catch (err: any) {
      console.error('Error assigning drivers:', err);
      setError(err.message || 'Failed to assign drivers. Please try again.');
      setLoading(false);
    }
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
    const driver = availableDrivers.find(d => d.id.toString() === driverId);
    return driver ? (driver.userName || driver.Name || `Driver ${driver.id}`) : '';
  };

  return (
    <div className="route-assignment-page">
      <div className="assignment-container">
        <h2 className="assignment-title">Route Assignment</h2>
        
        {loading && !assignments.length && (
          <div style={{ padding: '20px', textAlign: 'center' }}>Loading routes...</div>
        )}

        {error && (
          <div style={{ padding: '20px', color: 'red', textAlign: 'center' }}>{error}</div>
        )}

        {!loading && assignments.length === 0 && !error && (
          <div style={{ padding: '20px', textAlign: 'center' }}>No unassigned routes available.</div>
        )}

        {assignments.length > 0 && (
          <>
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
                            disabled={loading}
                          >
                            <option value="">Select Driver</option>
                            {availableDrivers.map(driver => (
                              <option key={driver.id} value={driver.id.toString()}>
                                {driver.userName || driver.Name || `Driver ${driver.id}`}
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
          </>
        )}

        {/* Action Buttons */}
        <div className="action-buttons">
          <button className="return-home-button" onClick={onReturn} disabled={loading}>
            Return
          </button>
          <button 
            className="submit-button" 
            onClick={handleSubmit}
            disabled={loading || assignments.length === 0 || !assignments.some(a => a.driverId)}
          >
            {loading ? 'Submitting...' : 'Submit'}
          </button>
        </div>
      </div>
    </div>
  );
}
