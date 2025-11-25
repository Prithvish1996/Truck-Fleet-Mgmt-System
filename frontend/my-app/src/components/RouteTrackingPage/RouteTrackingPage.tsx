import React, { useState, useEffect } from 'react';
import { RouteAssignment } from '../../types';
import { plannerService, DriverResponse } from '../../services/plannerService';
import RouteMapModal from '../RouteMapModal/RouteMapModal';
import TrackingTable from './TrackingTable';
import Pagination from '../common/Pagination';
import '../RouteTrackingPage.css';

interface RouteTrackingPageProps {
  assignments: RouteAssignment[];
  onReturn: () => void;
  onTrack: (assignment: RouteAssignment) => void;
  onTruckClick?: (truckPlateNo: string) => void;
}

export default function RouteTrackingPage({ assignments, onReturn, onTrack, onTruckClick }: RouteTrackingPageProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [isMapModalOpen, setIsMapModalOpen] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<RouteAssignment | null>(null);
  const [drivers, setDrivers] = useState<DriverResponse[]>([]);
  const itemsPerPage = 12;
  const assignedRoutes = assignments.filter(a => a.driverId !== null);
  const totalPages = Math.ceil(assignedRoutes.length / itemsPerPage);

  useEffect(() => {
    const loadDrivers = async () => {
      try {
        const driverList = await plannerService.getAvailableDrivers();
        setDrivers(driverList);
      } catch (error) {
        console.error('Error loading drivers:', error);
      }
    };
    loadDrivers();
  }, []);

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
        
        <TrackingTable
          assignments={getCurrentPageAssignments()}
          currentPage={currentPage}
          itemsPerPage={itemsPerPage}
          drivers={drivers}
          onTruckClick={onTruckClick}
          onTrackClick={handleTrackClick}
        />

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />

        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>

      <RouteMapModal
        isOpen={isMapModalOpen}
        assignment={selectedAssignment}
        onClose={handleCloseMap}
      />
    </div>
  );
}

