import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import RoutePlanningModal from './RoutePlanningModal';
import RouteAssignmentPage from './RouteAssignmentPage';
import RouteTrackingPage from './RouteTrackingPage';
import TruckDetailPage from './TruckDetailPage';
import ParcelDetailPage from './ParcelDetailPage';
import { RouteAssignment } from '../types';
import './PlannerDashboard.css';

export default function PlannerDashboard() {
  const navigate = useNavigate();
  const [expandedSections, setExpandedSections] = useState<{ [key: string]: boolean }>({});
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [isRoutePlanningModalOpen, setIsRoutePlanningModalOpen] = useState(false);
  const [showRouteAssignment, setShowRouteAssignment] = useState(false);
  const [showRouteTracking, setShowRouteTracking] = useState(false);
  const [showTruckDetail, setShowTruckDetail] = useState(false);
  const [showParcelDetail, setShowParcelDetail] = useState(false);
  const [selectedParcelIds, setSelectedParcelIds] = useState<string[]>([]);
  const [submittedAssignments, setSubmittedAssignments] = useState<RouteAssignment[]>([]);
  const [selectedTruckPlateNo, setSelectedTruckPlateNo] = useState<string>('');
  const [selectedParcelId, setSelectedParcelId] = useState<string>('');
  const [truckDetailPreviousPage, setTruckDetailPreviousPage] = useState<'assignment' | 'tracking' | null>(null);

  const toggleSection = (section: string) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const handleRoutePlanningClick = () => {
    setIsRoutePlanningModalOpen(true);
  };

  const handleGenerateRoute = (selectedParcels: string[]) => {
    console.log('Generating route for parcels:', selectedParcels);
    setSelectedParcelIds(selectedParcels);
    setIsRoutePlanningModalOpen(false);
    setShowRouteAssignment(true);
  };

  const handleReturnFromAssignment = () => {
    // Return to previous page (Route Planning Modal)
    setShowRouteAssignment(false);
    setIsRoutePlanningModalOpen(true);
    // Don't clear selectedParcelIds, so user can regenerate route if needed
  };

  const handleReturnHome = () => {
    // Return to Dashboard (when closing modal)
    setShowRouteAssignment(false);
    setSelectedParcelIds([]);
  };

  const handleSubmitAssignments = (assignments: RouteAssignment[]) => {
    console.log('Submitting route assignments:', assignments);
    // Store submitted assignments and show tracking page
    setSubmittedAssignments(assignments);
    setShowRouteAssignment(false);
    setShowRouteTracking(true);
  };

  const handleReturnFromTracking = () => {
    // Return to Route Assignment page instead of Dashboard
    setShowRouteTracking(false);
    setShowRouteAssignment(true);
    // Keep submittedAssignments and selectedParcelIds so user can see their assignments
  };

  const handleTrackRoute = (assignment: RouteAssignment) => {
    console.log('Tracking route:', assignment);
    // Route map modal is handled by RouteTrackingPage component
  };

  const handleTruckClick = (truckPlateNo: string) => {
    setSelectedTruckPlateNo(truckPlateNo);
    // Determine which page we're coming from
    if (showRouteAssignment) {
      setTruckDetailPreviousPage('assignment');
      setShowRouteAssignment(false);
    } else if (showRouteTracking) {
      setTruckDetailPreviousPage('tracking');
      setShowRouteTracking(false);
    }
    setShowTruckDetail(true);
  };

  const handleReturnFromTruckDetail = () => {
    setShowTruckDetail(false);
    // Return to the previous page
    if (truckDetailPreviousPage === 'assignment') {
      setShowRouteAssignment(true);
    } else if (truckDetailPreviousPage === 'tracking') {
      setShowRouteTracking(true);
    }
    setTruckDetailPreviousPage(null);
    setSelectedTruckPlateNo('');
  };

  const handleParcelClick = (parcelId: string) => {
    setSelectedParcelId(parcelId);
    setShowTruckDetail(false);
    setShowParcelDetail(true);
  };

  const handleReturnFromParcelDetail = () => {
    setShowParcelDetail(false);
    setShowTruckDetail(true);
    setSelectedParcelId('');
  };

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      const token = authService.getToken();
      if (token) {
        await authService.logout(token);
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      authService.removeToken();
      navigate('/');
    }
  };

  useEffect(() => {
    // Check if user is authenticated and is planner
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'PLANNER') {
      navigate('/');
    }
  }, [navigate]);

  // Show parcel detail page if active
  if (showParcelDetail) {
    return (
      <ParcelDetailPage
        parcelId={selectedParcelId}
        onReturn={handleReturnFromParcelDetail}
      />
    );
  }

  // Show truck detail page if active
  if (showTruckDetail) {
    return (
      <TruckDetailPage
        truckPlateNo={selectedTruckPlateNo}
        onReturn={handleReturnFromTruckDetail}
        onParcelClick={handleParcelClick}
      />
    );
  }

  // Show route tracking page if active
  if (showRouteTracking) {
    return (
      <RouteTrackingPage
        assignments={submittedAssignments}
        onReturn={handleReturnFromTracking}
        onTrack={handleTrackRoute}
        onTruckClick={handleTruckClick}
      />
    );
  }

  // Show route assignment page if active
  if (showRouteAssignment) {
    return (
      <RouteAssignmentPage
        selectedParcelIds={selectedParcelIds}
        onReturn={handleReturnFromAssignment}
        onSubmit={handleSubmitAssignments}
        onTruckClick={handleTruckClick}
      />
    );
  }

  return (
    <div className="planner-dashboard">
      <header className="dashboard-header">
        <h1 className="dashboard-title">📋 Planner Dashboard</h1>
        <button 
          className="logout-button" 
          onClick={handleLogout}
          disabled={isLoggingOut}
        >
          {isLoggingOut ? 'Logging out...' : 'Logout'}
        </button>
      </header>
      
      <div className="dashboard-content">
        <p className="dashboard-description">
          Welcome to the Planner Dashboard. Optimize routes, schedule deliveries, and coordinate the fleet.
        </p>
        
        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('routes')}
          >
            🗺️ Route Planning
          </button>
          {expandedSections.routes && (
            <div className="collapsible-content">
              <p>Plan and optimize delivery routes for maximum efficiency.</p>
              <p>Consider traffic patterns, delivery windows, and vehicle capacity.</p>
              <p>Assign routes to drivers and track progress.</p>
              <button 
                className="route-planning-button"
                onClick={handleRoutePlanningClick}
              >
                View Tomorrow's Requests
              </button>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('schedule')}
          >
            📅 Delivery Scheduling
          </button>
          {expandedSections.schedule && (
            <div className="collapsible-content">
              <p>Schedule deliveries and manage time slots.</p>
              <p>Coordinate with customers for delivery windows.</p>
              <p>Handle rescheduling and priority deliveries.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('fleet')}
          >
            🚛 Fleet Coordination
          </button>
          {expandedSections.fleet && (
            <div className="collapsible-content">
              <p>Monitor fleet status and vehicle availability.</p>
              <p>Assign vehicles to drivers and routes.</p>
              <p>Track maintenance schedules and vehicle capacity.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('analytics')}
          >
            📊 Planning Analytics
          </button>
          {expandedSections.analytics && (
            <div className="collapsible-content">
              <p>Analyze delivery patterns and optimization opportunities.</p>
              <p>Generate reports on route efficiency and performance.</p>
              <p>Identify areas for improvement in planning processes.</p>
            </div>
          )}
        </div>
      </div>

      <RoutePlanningModal
        isOpen={isRoutePlanningModalOpen}
        onClose={() => setIsRoutePlanningModalOpen(false)}
        onGenerateRoute={handleGenerateRoute}
      />
    </div>
  );
}