import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import RoutePlanningModal from '../../components/RoutePlanningModal';
import RouteAssignmentPage from '../../components/RouteAssignmentPage';
import RouteTrackingPage from '../../components/RouteTrackingPage';
import TruckDetailPage from '../../components/TruckDetailPage';
import ParcelDetailPage from '../../components/ParcelDetailPage';
import { RouteAssignment } from '../../types';
import homeIcon from '../../assets/home icon.png';
import scheduleIcon from '../../assets/schedule icon.png';
import smallLogo from '../../assets/small logo.png';
import './PlannerDashboard.css';

type PriorityLevel = 'High' | 'Medium' | 'Low';

type ScheduleParcel = {
  id: string;
  receiver: string;
  location: string;
  warehouse: string;
  status: 'Pending' | 'Scheduled';
  selectable: boolean;
};

type DashboardRequest = {
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  priority: PriorityLevel;
};

export default function PlannerDashboard() {
  const navigate = useNavigate();
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

  const [activeView, setActiveView] = useState<'dashboard' | 'schedule'>('dashboard');
  const [newRequests, setNewRequests] = useState<DashboardRequest[]>([
    { truckPlateId: 'R-965-FK', deliveryDate: 'Oct 10, 17:00', parcels: 15, warehouse: 'Amazon DUS2', priority: 'High' },
    { truckPlateId: 'K-381-LP', deliveryDate: 'Oct 10, 17:00', parcels: 20, warehouse: 'Amazon DUS2', priority: 'Low' },
    { truckPlateId: 'T-947-MJ', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Low' },
    { truckPlateId: 'H-520-ZR', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Medium' },
    { truckPlateId: 'B-194-XN', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Medium' },
    { truckPlateId: 'V-803-GC', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'High' },
    { truckPlateId: 'N-672-FD', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Low' },
    { truckPlateId: 'P-415-HV', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Low' },
    { truckPlateId: 'Z-209-KR', deliveryDate: 'Oct 10, 17:00', parcels: 22, warehouse: 'Amazon DUS2', priority: 'Medium' }
  ]);

  const scheduleParcels: ScheduleParcel[] = [
    { id: 'P1234-1', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-2', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-3', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-4', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-5', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-6', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-7', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true },
    { id: 'P1234-8', receiver: 'Amsterdam', location: 'Kwadrantweg 2-12, 1042 AG Amsterdam', warehouse: 'Amazon DNL1', status: 'Pending', selectable: true }
  ];

  const [selectedScheduleParcels, setSelectedScheduleParcels] = useState<string[]>([]);
  const [scheduleDate, setScheduleDate] = useState('');
  const [scheduleTruck, setScheduleTruck] = useState('R-965-FK');
  const [schedulePriority, setSchedulePriority] = useState<PriorityLevel>('High');
  const [scheduleError, setScheduleError] = useState('');

  const truckOptions = ['R-965-FK', 'K-381-LP', 'T-947-MJ', 'H-520-ZR', 'B-194-XN'];
  const priorityOptions: PriorityLevel[] = ['High', 'Medium', 'Low'];

  const handleGenerateRouteClick = () => {
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

  const handleScheduleParcelToggle = (parcelId: string) => {
    setSelectedScheduleParcels(prev =>
      prev.includes(parcelId) ? prev.filter(id => id !== parcelId) : [...prev, parcelId]
    );
    setScheduleError('');
  };

  const resetScheduleForm = () => {
    setSelectedScheduleParcels([]);
    setScheduleDate('');
    setScheduleTruck(truckOptions[0]);
    setSchedulePriority('High');
    setScheduleError('');
  };

  const handleScheduleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!scheduleDate || selectedScheduleParcels.length === 0) {
      setScheduleError('Select at least one parcel and pick a delivery date.');
      return;
    }

    const deliveryDate = new Date(scheduleDate);
    const formattedDate = deliveryDate.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    });

    const newRequest: DashboardRequest = {
      truckPlateId: scheduleTruck,
      deliveryDate: `${formattedDate}, 10:00`,
      parcels: selectedScheduleParcels.length,
      warehouse: 'Amazon DUS2',
      priority: schedulePriority
    };

    setNewRequests(prev => [newRequest, ...prev]);
    resetScheduleForm();
    setActiveView('dashboard');
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

  const summaryCards = [
    { title: "Today's Requests", value: '12', delta: '-1%', trend: 'down' as const },
    { title: 'Available Drivers', value: '5', delta: '+2%', trend: 'up' as const },
    { title: 'Process', value: '14', delta: '+12%', trend: 'up' as const },
    { title: 'Exceptions', value: '1', delta: 'Warning', trend: 'warning' as const }
  ];

  const availableDrivers = [
    { name: 'Tom', licenses: ['B', 'C1'], workWindow: { start: '7:30', end: '17:30' } },
    { name: 'Jack', licenses: ['B', 'BE'], workWindow: { start: '8:30', end: '17:30' } },
    { name: 'Frank', licenses: ['B', 'C1'], workWindow: { start: '9:00', end: '18:00' } },
    { name: 'Bob', licenses: ['B'], workWindow: { start: '6:30', end: '16:30' } },
    { name: 'Spensor', licenses: ['B'], workWindow: { start: '8:30', end: '17:30' } }
  ];

  const statusMonitoring = [
    { requestId: 'R1233-1', driver: 'Driver A', vehicle: 'Ford Transit', ect: 'Oct 10, 17:00', status: 'Delivery', location: '2354SD' },
    { requestId: 'R1233-2', driver: 'Driver B', vehicle: 'Mercedes Benz Sprinter', ect: 'Oct 11, 11:00', status: 'Delivery', location: '2144DC' },
    { requestId: 'R1233-3', driver: 'Driver C', vehicle: 'VW Transporter', ect: 'Oct 12, 13:00', status: 'Exceptions', location: '/' },
    { requestId: 'R1233-4', driver: 'Driver D', vehicle: 'VW Crafter', ect: 'Oct 13, 11:00', status: 'Processing', location: '/' },
    { requestId: 'R1233-5', driver: 'Driver E', vehicle: 'Renault Master', ect: 'Oct 14, 14:00', status: 'Processing', location: '/' }
  ];

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

  return (
    <div className="planner-dashboard">
      <header className="top-bar">
        <div className="top-bar-brand">
          <img src={smallLogo} alt="Driver GO" className="small-logo" />
        </div>
        <div className="top-bar-controls">
          <div className="search-input">
            <span className="search-icon" aria-hidden />
            <input type="search" placeholder="Search..." aria-label="Search dashboard" />
          </div>
          <button className="language-switch" type="button" aria-label="Change language">
            <span className="globe-icon" aria-hidden />
            EN
            <span className="chevron" aria-hidden />
          </button>
          <div className="user-pill">
            <div className="user-avatar" aria-hidden />
            <span>Planner 1</span>
          </div>
          <button
            className="logout-button"
            type="button"
            onClick={handleLogout}
            disabled={isLoggingOut}
          >
            {isLoggingOut ? 'Logging out...' : 'Logout'}
          </button>
        </div>
      </header>

      <div className="content-shell">
        <aside className="dashboard-sidebar">
          <nav className="sidebar-nav">
            <button
              type="button"
              className={`nav-item ${activeView === 'dashboard' ? 'active' : ''}`}
              onClick={() => {
                setActiveView('dashboard');
                setScheduleError('');
              }}
            >
              <img src={homeIcon} alt="" aria-hidden className="nav-icon" />
              <span className="nav-label">Planner Dashboard</span>
            </button>
            <button
              type="button"
              className={`nav-item schedule ${activeView === 'schedule' ? 'active' : ''}`}
              onClick={() => {
                setScheduleError('');
                setActiveView('schedule');
              }}
            >
              <img src={scheduleIcon} alt="" aria-hidden className="nav-icon" />
              <span className="nav-label">Schedule</span>
            </button>
          </nav>
        </aside>

        <main className={`dashboard-main ${activeView === 'schedule' ? 'schedule-view' : ''}`}>
          {activeView === 'dashboard' ? (
            <>
              <section className="summary-cards">
                {summaryCards.map(card => (
                  <div key={card.title} className="summary-card">
                    <div className="summary-card-top">
                      <span className="summary-card-title">{card.title}</span>
                      <span className={`summary-card-delta summary-card-delta-${card.trend}`}>
                        {card.delta}
                      </span>
                    </div>
                    <span className="summary-card-value">{card.value}</span>
                  </div>
                ))}
              </section>

              <section className="dashboard-grid">
                <div className="panel new-requests">
                  <div className="panel-header">
                    <h2>New Requests</h2>
                  </div>
                  <div className="table-wrapper">
                    <table className="new-requests-table">
                      <thead>
                        <tr>
                          <th>Truck Plate ID</th>
                          <th>Delivery Date</th>
                          <th>No. of Parcels</th>
                          <th>Warehouse</th>
                          <th>Priority</th>
                        </tr>
                      </thead>
                      <tbody>
                        {newRequests.map(request => (
                          <tr key={`${request.truckPlateId}-${request.deliveryDate}`}>
                            <td>{request.truckPlateId}</td>
                            <td>{request.deliveryDate}</td>
                            <td>{request.parcels}</td>
                            <td>{request.warehouse}</td>
                            <td>
                              <span className={`priority-badge priority-${request.priority.toLowerCase()}`}>
                                {request.priority}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div className="panel-footer">
                    <div className="pagination">
                      <button type="button" className="pagination-btn" aria-label="Previous page">
                        ‹
                      </button>
                      {[1, 2, 3, 4, 5].map(page => (
                        <button
                          key={page}
                          type="button"
                          className={`pagination-btn ${page === 1 ? 'active' : ''}`}
                        >
                          {page}
                        </button>
                      ))}
                      <button type="button" className="pagination-btn" aria-label="Next page">
                        ›
                      </button>
                    </div>
                    <button
                      className="primary-action"
                      type="button"
                      onClick={handleGenerateRouteClick}
                    >
                      Generate Route
                    </button>
                  </div>
                </div>

                <div className="panel available-drivers">
                  <div className="panel-header">
                    <h2>Available Drivers</h2>
                  </div>
                  <ul className="available-drivers-list">
                    {availableDrivers.map(driver => (
                      <li key={driver.name} className="driver-item">
                        <div className="driver-avatar" aria-hidden>
                          {driver.name.charAt(0)}
                        </div>
                        <div className="driver-details">
                          <span className="driver-name">{driver.name}</span>
                          <div className="driver-licenses">
                            {driver.licenses.map(license => (
                              <span key={license} className="license-pill">
                                {license}
                              </span>
                            ))}
                          </div>
                        </div>
                        <div className="driver-window">
                          <span className="work-window">{driver.workWindow.start}</span>
                          <span className="work-window-divider">-</span>
                          <span className="work-window">{driver.workWindow.end}</span>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </section>

              <section className="panel status-monitoring">
                <div className="panel-header">
                  <h2>Status Monitoring</h2>
                </div>
                <div className="table-wrapper">
                  <table className="status-monitoring-table">
                    <thead>
                      <tr>
                        <th>Request ID</th>
                        <th>Driver</th>
                        <th>Vehicle</th>
                        <th>ECT</th>
                        <th>Status</th>
                        <th>GPS Location</th>
                      </tr>
                    </thead>
                    <tbody>
                      {statusMonitoring.map(item => (
                        <tr key={item.requestId}>
                          <td>{item.requestId}</td>
                          <td>{item.driver}</td>
                          <td>{item.vehicle}</td>
                          <td>{item.ect}</td>
                          <td>
                            <span className={`status-badge status-${item.status.toLowerCase()}`}>
                              {item.status}
                            </span>
                          </td>
                          <td>{item.location}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            </>
          ) : (
            <section className="schedule-page">
              <form className="schedule-panel" onSubmit={handleScheduleSubmit}>
                <div className="schedule-header">
                  <h1>Parcel Pool</h1>
                  <p>Select Parcels for New Request</p>
                </div>

                <div className="schedule-table-wrapper">
                  <table className="schedule-table">
                    <thead>
                      <tr>
                        <th aria-label="Select parcel" />
                        <th>Parcel ID</th>
                        <th>Receiver</th>
                        <th>Delivery Location</th>
                        <th>Warehouse</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {scheduleParcels.map(parcel => {
                        const isSelected = selectedScheduleParcels.includes(parcel.id);
                        return (
                          <tr key={parcel.id} className={!parcel.selectable ? 'schedule-row-disabled' : ''}>
                            <td>
                              <input
                                type="checkbox"
                                checked={isSelected}
                                onChange={() => handleScheduleParcelToggle(parcel.id)}
                                disabled={!parcel.selectable}
                                aria-label={`Select parcel ${parcel.id}`}
                              />
                            </td>
                            <td>{parcel.id}</td>
                            <td>{parcel.receiver}</td>
                            <td>{parcel.location}</td>
                            <td>{parcel.warehouse}</td>
                            <td className="schedule-status pending">Pending</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                <div className="schedule-form-grid">
                  <label className="schedule-field">
                    <span>Schedule Delivery Date:</span>
                    <input
                      type="date"
                      lang="en"
                      value={scheduleDate}
                      onChange={event => {
                        setScheduleDate(event.target.value);
                        setScheduleError('');
                      }}
                      required
                    />
                  </label>
                  <label className="schedule-field">
                    <span>Assign Truck:</span>
                    <select
                      value={scheduleTruck}
                      onChange={event => {
                        setScheduleTruck(event.target.value);
                        setScheduleError('');
                      }}
                    >
                      {truckOptions.map(option => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="schedule-field">
                    <span>Priority:</span>
                    <select
                      value={schedulePriority}
                      onChange={event => {
                        setSchedulePriority(event.target.value as PriorityLevel);
                        setScheduleError('');
                      }}
                    >
                      {priorityOptions.map(option => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <div className="schedule-footer">
                  <div className="schedule-count">
                    <span>Number of Parcel:</span>
                    <strong>{selectedScheduleParcels.length}</strong>
                  </div>
                  {scheduleError && <span className="schedule-error">{scheduleError}</span>}
                  <button type="submit" className="schedule-submit">Submit</button>
                </div>
              </form>
            </section>
          )}
        </main>
      </div>

      <RoutePlanningModal
        isOpen={isRoutePlanningModalOpen}
        onClose={() => setIsRoutePlanningModalOpen(false)}
        onGenerateRoute={handleGenerateRoute}
      />
    </div>
  );
}