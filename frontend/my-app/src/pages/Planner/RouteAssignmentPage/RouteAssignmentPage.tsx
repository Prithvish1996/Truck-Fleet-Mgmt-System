import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../../../types';
import { plannerService, DriverResponse, StopDto } from '../../../services/plannerService';
import { formatDate, countParcelsInRoute } from '../../../utils/dataTransformers';
import { requestCache } from '../../../utils/requestCache';
import AssignmentTable from './AssignmentTable';
import Pagination from '../../../components/common/Pagination';
import './RouteAssignmentPage.css';

interface RouteAssignmentPageProps {
  selectedParcelIds: string[];
  onReturn: () => void;
  onSubmit: (assignments: RouteAssignment[]) => void;
  onTruckClick?: (truckPlateNo: string) => void;
  submittedAssignments?: RouteAssignment[];
  routeStopOrderMap?: Map<number, StopDto[]>;
}

export default function RouteAssignmentPage({ selectedParcelIds, onReturn, onSubmit, onTruckClick, submittedAssignments = [], routeStopOrderMap = new Map() }: RouteAssignmentPageProps) {
  const [assignments, setAssignments] = useState<RouteAssignment[]>([]);
  const [availableDrivers, setAvailableDrivers] = useState<DriverResponse[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const itemsPerPage = 12;

  useEffect(() => {
    requestCache.invalidate('getUnassignedRoutes');
    loadUnassignedRoutes();
  }, [selectedParcelIds]);

  useEffect(() => {
    const loadDrivers = async () => {
      try {
        const drivers = await requestCache.get(
          'availableDrivers',
          () => plannerService.getAvailableDrivers()
        );
        
        const assignedDriverIds = new Set<number>();
        
        submittedAssignments.forEach(a => {
          if (a.driverId) {
            assignedDriverIds.add(parseInt(a.driverId, 10));
          }
        });
        
        const filtered = drivers.filter(d => 
          d.isAvailable && !assignedDriverIds.has(d.id)
        );
        
        const currentPageSelectedDriverIds = new Set<number>();
        assignments.forEach(a => {
          if (a.driverId) {
            currentPageSelectedDriverIds.add(parseInt(a.driverId, 10));
          }
        });
        
        const finalDrivers = [...filtered];
        currentPageSelectedDriverIds.forEach(driverId => {
          const driver = drivers.find(d => d.id === driverId);
          if (driver && !finalDrivers.some(d => d.id === driverId)) {
            finalDrivers.push(driver);
          }
        });
        
        console.log('Loaded available drivers for RouteAssignmentPage:', {
          total: drivers.length,
          submittedAssigned: Array.from(assignedDriverIds),
          currentPageSelected: Array.from(currentPageSelectedDriverIds),
          filtered: finalDrivers.length
        });
        
        setAvailableDrivers(finalDrivers);
      } catch (err: any) {
        console.error('Error loading available drivers:', err);
      }
    };
    
    loadDrivers();
  }, [selectedParcelIds, submittedAssignments, assignments]);

  useEffect(() => {
    console.log('Available drivers updated in RouteAssignmentPage:', availableDrivers.length);
  }, [availableDrivers]);

  const loadUnassignedRoutes = async () => {
    setLoading(true);
    setError('');
    try {
      console.log('Loading unassigned routes...');
      
      const data = await requestCache.get(
        'getUnassignedRoutes',
        () => plannerService.getUnassignedRoutes()
      );
      
      console.log('Unassigned routes data:', data);
      console.log('unAssignedRoute count:', data.unAssignedRoute?.length || 0);
      console.log('assignRoutes count:', data.assignRoutes?.length || 0);
      
      const routesToAssign = Array.isArray(data.unAssignedRoute) ? data.unAssignedRoute.filter(Boolean) : [];
      
      if (routesToAssign.length === 0) {
        console.warn('No unassigned routes found. Full response:', data);
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

      const assignmentPromises = assignmentsToSubmit.map(assignment => {
        // Get stop order if available (from modal), otherwise backend uses default order
        const stops = routeStopOrderMap.get(assignment.routeId || 0);
        
        return plannerService.assignDriverToRoute({
          routId: assignment.routeId!,
          truckId: assignment.truckId!,
          driverId: parseInt(assignment.driverId!, 10),
          // Include stops if available (backend accepts this as optional)
          stops: stops?.map(stop => ({
            stopId: stop.stopId!,
            priority: stop.priority,
            stopType: stop.stopType,
            parcelsToDeliver: stop.parcelsToDeliver,
            location: stop.location,
          })) || undefined,
        });
      });

      await Promise.all(assignmentPromises);
      console.log('All assignments submitted successfully');
      
      const assignedDriverIds = assignmentsToSubmit.map(a => parseInt(a.driverId!, 10));
      console.log('Removing assigned drivers from local state:', assignedDriverIds);
      setAvailableDrivers(prev => {
        const updated = prev.filter(driver => !assignedDriverIds.includes(driver.id));
        console.log('Updated local available drivers:', updated.length);
        return updated;
      });
      
      requestCache.invalidate('availableDrivers');
      requestCache.invalidate('getUnassignedRoutes');
      
      console.log('Calling onSubmit with assignments:', assignmentsToSubmit);
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

