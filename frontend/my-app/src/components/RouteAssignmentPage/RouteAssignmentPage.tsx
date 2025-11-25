import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../../types';
import { plannerService, DriverResponse } from '../../services/plannerService';
import { formatDate, countParcelsInRoute } from '../../utils/dataTransformers';
import AssignmentTable from './AssignmentTable';
import Pagination from '../common/Pagination';
import '../RouteAssignmentPage.css';

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
      const routesToAssign = data.unAssignedRoute || [];
      
      if (routesToAssign.length === 0) {
        setError('No request can be assigned a driver');
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
      if (err.message && err.message.includes('No unassign route available')) {
        setError('No request can be assigned a driver');
      } else {
        setError(err.message || 'Failed to load routes. Please try again.');
      }
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
      const assignmentsToSubmit = assignments.filter(
        assignment => assignment.driverId && assignment.routeId && assignment.truckId
      );

      if (assignmentsToSubmit.length === 0) {
        setError('Please select at least one driver for the routes.');
        setLoading(false);
        return;
      }

      const assignmentPromises = assignmentsToSubmit.map(assignment =>
        plannerService.assignDriverToRoute({
          routId: assignment.routeId!,
          truckId: assignment.truckId!,
          driverId: parseInt(assignment.driverId!, 10)
        })
      );

      await Promise.all(assignmentPromises);
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
          <div style={{ padding: '20px', textAlign: 'center' }}>No request can be assigned a driver</div>
        )}

        {assignments.length > 0 && (
          <>
            <AssignmentTable
              assignments={getCurrentPageAssignments()}
              currentPage={currentPage}
              itemsPerPage={itemsPerPage}
              availableDrivers={availableDrivers}
              loading={loading}
              onTruckClick={onTruckClick}
              onDriverChange={handleDriverChange}
            />

            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </>
        )}

        <div className="action-buttons">
          <button className="return-home-button" onClick={onReturn} disabled={loading}>
            Return
          </button>
          <button 
            className="submit-button" 
            onClick={handleSubmit}
            disabled={loading || assignments.length === 0 || !assignments.some(a => a.driverId) || error === 'No request can be assigned a driver'}
          >
            {loading ? 'Submitting...' : 'Submit'}
          </button>
        </div>
      </div>
    </div>
  );
}

