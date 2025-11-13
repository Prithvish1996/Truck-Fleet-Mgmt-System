import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { plannerService, ParcelResponse, DriverResponse } from '../../services/plannerService';
import { formatDate, formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
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
  parcelId: number;
  receiver: string;
  location: string;
  warehouse: string;
  status: 'Pending' | 'Scheduled';
  selectable: boolean;
  weight?: number;
  volume?: number;
  phone?: string;
  deliveryInstructions?: string;
  createdAt?: string;
};

type DashboardRequest = {
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  priority: PriorityLevel;
  parcelIds: number[];
  warehouseId: number;
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
  const [newRequests, setNewRequests] = useState<DashboardRequest[]>([]);
  const [scheduleParcels, setScheduleParcels] = useState<ScheduleParcel[]>([]);
  const [selectedScheduleParcels, setSelectedScheduleParcels] = useState<string[]>([]);
  const [scheduleDate, setScheduleDate] = useState('');
  const [scheduleTruck, setScheduleTruck] = useState('');
  const [schedulePriority, setSchedulePriority] = useState<PriorityLevel>('High');
  const [scheduleError, setScheduleError] = useState('');
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState<any[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<number | null>(null);
  const [availableTrucks, setAvailableTrucks] = useState<string[]>([]);
  const [availableDrivers, setAvailableDrivers] = useState<DriverResponse[]>([]);
  const [statusMonitoring, setStatusMonitoring] = useState<Array<{ driver: string; status: string; route: string }>>([]);

  // Filtering and sorting states
  const [filterStatus, setFilterStatus] = useState<'All' | 'Pending' | 'Scheduled'>('All');
  const [sortBy, setSortBy] = useState<'id' | 'receiver' | 'location' | 'warehouse'>('id');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [searchText, setSearchText] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);

  const priorityOptions: PriorityLevel[] = ['High', 'Medium', 'Low'];

  // Load warehouses on mount
  useEffect(() => {
    const loadWarehouses = async () => {
      try {
        const data = await plannerService.getWarehouses(0, 100);
        // 添加安全检查
        if (data && data.content && Array.isArray(data.content)) {
          setWarehouses(data.content);
          if (data.content.length > 0) {
            setSelectedWarehouseId(data.content[0].id);
          }
        } else {
          console.warn('Invalid warehouses data:', data);
          console.warn('Data type:', typeof data);
          console.warn('Data keys:', data ? Object.keys(data) : 'null');
          setWarehouses([]); // 设置为空数组
        }
      } catch (error: any) {
        console.error('Error loading warehouses:', error);
        console.error('Error details:', error?.message, error?.stack);
        setWarehouses([]); // 错误时设置为空数组
      }
    };
    loadWarehouses();
  }, []);

  // Load scheduled parcels for Schedule page
  useEffect(() => {
    const loadScheduleParcels = async () => {
      if (activeView === 'schedule' && selectedWarehouseId) {
        setLoading(true);
        setScheduleError(''); // Clear previous errors
        try {
          // Use backend API searchText parameter instead of frontend filtering
          const data = await plannerService.getAllParcels(
            selectedWarehouseId, 
            0, 
            100, 
            searchText || undefined
          );
          const parcels: ScheduleParcel[] = (data.content || [])
            .filter(p => p.status === 'PENDING')
            .map((p, index) => ({
              id: formatParcelId(p.parcelId, index),
              parcelId: p.parcelId,
              receiver: p.recipientName || 'Unknown',
              location: getFullDeliveryAddress(p),
              warehouse: p.warehouseCity || 'Unknown',
              status: 'Pending' as const,
              selectable: true,
              weight: p.weight,
              volume: p.volume,
              phone: p.recipientPhone,
              deliveryInstructions: p.deliveryInstructions,
              createdAt: p.createdAt
            }));
          setScheduleParcels(parcels);
        } catch (error: any) {
          console.error('Error loading schedule parcels:', error);
          const errorMessage = error?.message || 'Failed to load parcels. Please try again.';
          setScheduleError(errorMessage);
          setScheduleParcels([]); // Reset to empty array on error
        } finally {
          setLoading(false);
        }
      } else if (activeView === 'schedule' && !selectedWarehouseId) {
        // Reset parcels when no warehouse is selected
        setScheduleParcels([]);
        setScheduleError('');
      }
    };
    loadScheduleParcels();
  }, [activeView, selectedWarehouseId, searchText]);

  // Load available drivers
  useEffect(() => {
    const loadAvailableDrivers = async () => {
      if (activeView === 'dashboard') {
        try {
          const drivers = await plannerService.getAvailableDrivers();
          setAvailableDrivers(drivers.filter(d => d.isAvailable));
        } catch (error) {
          console.error('Error loading available drivers:', error);
          setAvailableDrivers([]);
        }
      }
    };
    loadAvailableDrivers();
  }, [activeView]);

  // Load status monitoring data
  useEffect(() => {
    const loadStatusMonitoring = async () => {
      if (activeView === 'dashboard') {
        try {
          // Step 1: Get all available drivers
          const drivers = await plannerService.getAvailableDrivers();
          const statusData: Array<{ driver: string; status: string; route: string }> = [];
          
          // Step 2: For each driver, get their ASSIGNED routes
          await Promise.all(
            drivers.map(async (driver) => {
              try {
                // Get routes for this driver (only ASSIGNED routes are returned by this API)
                const routeData = await plannerService.getRouteByDriverId(driver.id);
                
                // Process each route
                if (routeData.routes && Array.isArray(routeData.routes)) {
                  routeData.routes.forEach(route => {
                    const driverName = route.driverUserName || route.driverEmail || driver.userName || driver.email || 'Unknown Driver';
                    statusData.push({
                      driver: driverName,
                      status: route.status || 'ASSIGNED',
                      route: `Route ${route.routeId || 'N/A'} - ${route.truckPlateNumber || 'N/A'}`
                    });
                  });
                }
              } catch (error: any) {
                // If driver has no assigned routes, API returns error - that's OK, just skip
                // Only log if it's not a "No assigned routes" error
                if (!error.message?.includes('No assigned routes')) {
                  console.warn(`Error loading routes for driver ${driver.id}:`, error);
                }
              }
            })
          );
          
          setStatusMonitoring(statusData);
        } catch (error) {
          console.error('Error loading status monitoring:', error);
          setStatusMonitoring([]);
        }
      }
    };
    loadStatusMonitoring();
  }, [activeView]);

  // Load scheduled deliveries for New Requests
  useEffect(() => {
    const loadScheduledDeliveries = async () => {
      if (activeView === 'dashboard') {
        setLoading(true);
        try {
          const data = await plannerService.getScheduledDeliveries(undefined, 0, 100);
          
          // Add safety check for data structure
          if (!data || !data.data || !Array.isArray(data.data)) {
            console.warn('Invalid scheduled deliveries data format:', data);
            setNewRequests([]);
            return;
          }
          
          // Group parcels by warehouse and planned delivery date
          const grouped = new Map<string, ParcelResponse[]>();
          data.data.forEach(parcel => {
            const key = `${parcel.warehouseId}-${parcel.plannedDeliveryDate || 'no-date'}`;
            if (!grouped.has(key)) {
              grouped.set(key, []);
            }
            grouped.get(key)!.push(parcel);
          });

          // Convert to DashboardRequest format
          const requests: DashboardRequest[] = Array.from(grouped.entries()).map(([key, parcels]) => {
            const firstParcel = parcels[0];
            const truckPlateId = scheduleTruck || 'TBD';
            const deliveryDate = firstParcel.plannedDeliveryDate 
              ? formatDate(firstParcel.plannedDeliveryDate)
              : 'TBD';
            
            return {
              truckPlateId,
              deliveryDate,
              parcels: parcels.length,
              warehouse: firstParcel.warehouseCity || 'Unknown',
              priority: schedulePriority,
              parcelIds: parcels.map(p => p.parcelId),
              warehouseId: firstParcel.warehouseId
            };
          });

          setNewRequests(requests);

          // Load available trucks
          try {
            const routeData = await plannerService.getUnassignedRoutes();
            const trucks = routeData.trucks
              .filter(t => t.isAvailable)
              .map(t => t.plateNumber);
            setAvailableTrucks(trucks);
            if (trucks.length > 0 && !scheduleTruck) {
              setScheduleTruck(trucks[0]);
            }
          } catch (error) {
            console.error('Error loading trucks:', error);
            // Don't set error state, just log it
          }
        } catch (error: any) {
          console.error('Error loading scheduled deliveries:', error);
          console.error('Error details:', error?.message, error?.stack);
          // Set empty requests instead of crashing
          setNewRequests([]);
          // Don't show error to user if it's a server error (500)
          // This allows the page to still render with other data
        } finally {
          setLoading(false);
        }
      }
    };
    loadScheduledDeliveries();
  }, [activeView, scheduleTruck, schedulePriority]);

  const handleGenerateRouteClick = () => {
    setIsRoutePlanningModalOpen(true);
  };

  const handleGenerateRoute = async (selectedParcelIds: string[]) => {
    setSelectedParcelIds(selectedParcelIds);
    setIsRoutePlanningModalOpen(false);
    setShowRouteAssignment(true);
  };

  const handleReturnFromAssignment = () => {
    setShowRouteAssignment(false);
    setIsRoutePlanningModalOpen(true);
  };

  const handleSubmitAssignments = (assignments: RouteAssignment[]) => {
    setSubmittedAssignments(assignments);
    setShowRouteAssignment(false);
    setShowRouteTracking(true);
  };

  const handleReturnFromTracking = () => {
    setShowRouteTracking(false);
    setShowRouteAssignment(true);
  };

  const handleTrackRoute = (assignment: RouteAssignment) => {
    // Route map modal is handled by RouteTrackingPage component
  };

  const handleTruckClick = (truckPlateNo: string) => {
    setSelectedTruckPlateNo(truckPlateNo);
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

  // Filter and sort parcels (frontend filtering and sorting only, search is done via backend API)
  const filteredAndSortedParcels = useMemo(() => {
    let filtered = scheduleParcels;
    
    // Apply status filter (frontend filtering since backend doesn't support status filter parameter)
    if (filterStatus !== 'All') {
      filtered = filtered.filter(p => 
        filterStatus === 'Pending' ? p.status === 'Pending' : p.status === 'Scheduled'
      );
    }
    
    // Note: Search is handled by backend API via getAllParcels searchText parameter
    // We don't need to filter here again as the data is already filtered from backend
    
    // Apply sorting (frontend sorting)
    filtered = [...filtered].sort((a, b) => {
      let aValue: string | number = '';
      let bValue: string | number = '';
      
      switch (sortBy) {
        case 'id':
          aValue = a.id;
          bValue = b.id;
          break;
        case 'receiver':
          aValue = a.receiver;
          bValue = b.receiver;
          break;
        case 'location':
          aValue = a.location;
          bValue = b.location;
          break;
        case 'warehouse':
          aValue = a.warehouse;
          bValue = b.warehouse;
          break;
      }
      
      if (typeof aValue === 'string') {
        return sortOrder === 'asc' 
          ? aValue.localeCompare(bValue as string)
          : (bValue as string).localeCompare(aValue);
      }
      return sortOrder === 'asc' ? Number(aValue) - Number(bValue) : Number(bValue) - Number(aValue);
    });
    
    return filtered;
  }, [scheduleParcels, filterStatus, sortBy, sortOrder]);

  // Handle optimize route
  const handleOptimizeRoute = async () => {
    if (selectedScheduleParcels.length === 0) {
      setScheduleError('Please select at least one parcel to optimize routes.');
      return;
    }
    
    if (!selectedWarehouseId) {
      setScheduleError('Please select a warehouse first.');
      return;
    }
    
    setIsOptimizing(true);
    setScheduleError('');
    
    try {
      // Get selected parcel IDs (from parcelId field, not id field)
      const selectedParcelIds = selectedScheduleParcels
        .map(id => {
          const parcel = scheduleParcels.find(p => p.id === id);
          return parcel?.parcelId;
        })
        .filter((id): id is number => id !== undefined);
      
      if (selectedParcelIds.length === 0) {
        throw new Error('No valid parcels selected');
      }
      
      // Call optimize route API
      const result = await plannerService.generateRoutes({
        depot_id: selectedWarehouseId,
        parcelIds: selectedParcelIds
      });
      
      // On success, navigate to RouteAssignmentPage
      setSelectedParcelIds(selectedParcelIds.map(id => id.toString()));
      setShowRouteAssignment(true);
      setActiveView('dashboard'); // Switch to dashboard view
      setScheduleError('');
      
    } catch (err: any) {
      console.error('Error optimizing routes:', err);
      setScheduleError(err.message || 'Failed to optimize routes. Please try again.');
    } finally {
      setIsOptimizing(false);
    }
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
    if (availableTrucks.length > 0) {
      setScheduleTruck(availableTrucks[0]);
    }
    setSchedulePriority('High');
    setScheduleError('');
  };

  const handleScheduleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!scheduleDate || selectedScheduleParcels.length === 0) {
      setScheduleError('Select at least one parcel and pick a delivery date.');
      return;
    }

    setLoading(true);
    setScheduleError('');

    try {
      // Extract parcel IDs
      const parcelIds = selectedScheduleParcels
        .map(id => {
          const match = id.match(/P(\d+)/);
          return match ? parseInt(match[1], 10) : 0;
        })
        .filter(id => id > 0);

      if (parcelIds.length === 0) {
        setScheduleError('Invalid parcel IDs selected.');
        return;
      }

      // Convert date to ISO format
      const deliveryDate = scheduleDate ? new Date(scheduleDate).toISOString() : undefined;

      // Call schedule API
      await plannerService.scheduleParcels({
        parcelIds,
        deliveryDate
      });

      // Store request info locally (for display in New Requests)
      const newRequest: DashboardRequest = {
        truckPlateId: scheduleTruck || 'TBD',
        deliveryDate: scheduleDate 
          ? formatDate(new Date(scheduleDate).toISOString())
          : 'TBD',
        parcels: selectedScheduleParcels.length,
        warehouse: (warehouses && warehouses.find(w => w.id === selectedWarehouseId)?.name) || 'Unknown',
        priority: schedulePriority,
        parcelIds,
        warehouseId: selectedWarehouseId || 0
      };

      setNewRequests(prev => [newRequest, ...prev]);
      resetScheduleForm();
      setActiveView('dashboard');
    } catch (error: any) {
      console.error('Error scheduling parcels:', error);
      setScheduleError(error.message || 'Failed to schedule parcels. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'PLANNER') {
      navigate('/');
    }
  }, [navigate]);

  if (showParcelDetail) {
    return (
      <ParcelDetailPage
        parcelId={selectedParcelId}
        onReturn={handleReturnFromParcelDetail}
      />
    );
  }

  if (showTruckDetail) {
    return (
      <TruckDetailPage
        truckPlateNo={selectedTruckPlateNo}
        onReturn={handleReturnFromTruckDetail}
        onParcelClick={handleParcelClick}
      />
    );
  }

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
    { title: "Today's Requests", value: newRequests.length.toString(), delta: '', trend: 'down' as const },
    { title: 'Available Drivers', value: '5', delta: '+2%', trend: 'up' as const },
    { title: 'Process', value: '14', delta: '+12%', trend: 'up' as const },
    { title: 'Exceptions', value: '1', delta: 'Warning', trend: 'warning' as const }
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
                        {newRequests.map((request, index) => (
                          <tr key={`${request.truckPlateId}-${request.deliveryDate}-${index}`}>
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

                {/* Available Drivers Panel */}
                <div className="panel available-drivers">
                  <div className="panel-header">
                    <h2>Available Drivers</h2>
                  </div>
                  <div className="available-drivers-list">
                    {availableDrivers.length === 0 ? (
                      <div style={{ padding: '20px', textAlign: 'center', color: '#61716d' }}>
                        No available drivers
                      </div>
                    ) : (
                      availableDrivers.map(driver => (
                        <div key={driver.id} className="driver-item">
                          <div className="driver-avatar">
                            {driver.userName?.charAt(0).toUpperCase() || driver.Name?.charAt(0).toUpperCase() || 'D'}
                          </div>
                          <div className="driver-details">
                            <div className="driver-name">{driver.Name || driver.userName || 'Unknown'}</div>
                            <div className="driver-window">
                              <span className="work-window">
                                {driver.email || 'No email'}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </section>

              {/* Status Monitoring Panel */}
              <section className="status-monitoring-section">
                <div className="panel status-monitoring">
                  <div className="panel-header">
                    <h2>Status Monitoring</h2>
                  </div>
                  <div className="table-wrapper">
                    <table className="status-monitoring-table">
                      <thead>
                        <tr>
                          <th>Driver</th>
                          <th>Status</th>
                          <th>Route</th>
                        </tr>
                      </thead>
                      <tbody>
                        {statusMonitoring.length === 0 ? (
                          <tr>
                            <td colSpan={3} style={{ padding: '20px', textAlign: 'center', color: '#61716d' }}>
                              No active routes
                            </td>
                          </tr>
                        ) : (
                          statusMonitoring.map((item, index) => (
                            <tr key={index}>
                              <td>{item.driver}</td>
                              <td>
                                <span className={`status-badge status-${item.status.toLowerCase() === 'assigned' ? 'delivery' : item.status.toLowerCase() === 'in_progress' ? 'processing' : 'exceptions'}`}>
                                  {item.status}
                                </span>
                              </td>
                              <td>{item.route}</td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </section>
            </>
          ) : activeView === 'schedule' ? (
            <section className="schedule-page" style={{ minHeight: '400px' }}>
              <form className="schedule-panel" onSubmit={handleScheduleSubmit}>
                <div className="schedule-header">
                  <h1>Parcel Pool</h1>
                  <p>Select Parcels for New Request</p>
                </div>

                {warehouses && warehouses.length === 0 && !loading && (
                  <div style={{ 
                    padding: '20px', 
                    textAlign: 'center',
                    color: '#61716d',
                    backgroundColor: '#fff9e6',
                    border: '1px solid #ffd966',
                    borderRadius: '8px',
                    margin: '0 20px'
                  }}>
                    Loading warehouses... Please wait.
                  </div>
                )}

                {warehouses && warehouses.length > 0 && (
                  <div className="schedule-form-grid">
                    <label className="schedule-field">
                      <span>Select Warehouse:</span>
                      <select
                        value={selectedWarehouseId || ''}
                        onChange={(e) => {
                          setSelectedWarehouseId(parseInt(e.target.value, 10));
                          setScheduleError('');
                        }}
                        required
                      >
                        {warehouses.map(warehouse => (
                          <option key={warehouse.id} value={warehouse.id}>
                            {warehouse.name || `Warehouse ${warehouse.id}`}
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>
                )}

                {scheduleError && (
                  <div className="schedule-error-message" style={{ 
                    padding: '12px 20px', 
                    margin: '16px 20px',
                    backgroundColor: '#fee', 
                    border: '1px solid #fcc',
                    borderRadius: '8px',
                    color: '#c33'
                  }}>
                    {scheduleError}
                  </div>
                )}

                {!selectedWarehouseId && warehouses && warehouses.length > 0 && !loading && (
                  <div className="schedule-message" style={{ 
                    padding: '20px', 
                    textAlign: 'center',
                    color: '#61716d'
                  }}>
                    Please select a warehouse to view parcels.
                  </div>
                )}

                {loading && selectedWarehouseId && (
                  <div className="schedule-loading" style={{ 
                    padding: '40px', 
                    textAlign: 'center',
                    color: '#61716d'
                  }}>
                    Loading parcels...
                  </div>
                )}

                {/* Filter and Sort Controls */}
                <div className="schedule-controls">
                  <div className="schedule-search">
                    <input
                      type="text"
                      placeholder="Search parcels..."
                      value={searchText}
                      onChange={(e) => setSearchText(e.target.value)}
                      className="search-input"
                    />
                  </div>
                  
                  <div className="schedule-filters">
                    <label className="filter-label">
                      <span>Status:</span>
                      <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value as 'All' | 'Pending' | 'Scheduled')}
                      >
                        <option value="All">All</option>
                        <option value="Pending">Pending</option>
                        <option value="Scheduled">Scheduled</option>
                      </select>
                    </label>
                    
                    <label className="filter-label">
                      <span>Sort By:</span>
                      <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value as 'id' | 'receiver' | 'location' | 'warehouse')}
                      >
                        <option value="id">Parcel ID</option>
                        <option value="receiver">Receiver</option>
                        <option value="location">Location</option>
                        <option value="warehouse">Warehouse</option>
                      </select>
                    </label>
                    
                    <button
                      type="button"
                      className="sort-order-btn"
                      onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
                      title={`Sort ${sortOrder === 'asc' ? 'Descending' : 'Ascending'}`}
                    >
                      {sortOrder === 'asc' ? '↑' : '↓'}
                    </button>
                  </div>
                </div>

                {!loading && selectedWarehouseId && (
                  <div className="schedule-table-wrapper">
                    {filteredAndSortedParcels.length === 0 ? (
                      <div className="schedule-message" style={{ 
                        padding: '40px', 
                        textAlign: 'center',
                        color: '#61716d'
                      }}>
                        No parcels found. Please try adjusting your filters or search criteria.
                      </div>
                    ) : (
                      <table className="schedule-table">
                        <thead>
                          <tr>
                            <th aria-label="Select parcel" />
                            <th>Parcel ID</th>
                            <th>Receiver</th>
                            <th>Delivery Location</th>
                            <th>Warehouse</th>
                            <th>Status</th>
                            <th>Weight</th>
                            <th>Volume</th>
                            <th>Phone</th>
                          </tr>
                        </thead>
                        <tbody>
                          {filteredAndSortedParcels.map(parcel => {
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
                                <td className={`schedule-status ${parcel.status.toLowerCase()}`}>
                                  {parcel.status}
                                </td>
                                <td>{parcel.weight ? `${parcel.weight} kg` : 'N/A'}</td>
                                <td>{parcel.volume ? `${parcel.volume} m³` : 'N/A'}</td>
                                <td>{parcel.phone || 'N/A'}</td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    )}
                  </div>
                )}

                {/* Action Buttons */}
                <div className="schedule-actions">
                  <button
                    type="button"
                    className="optimize-route-btn"
                    onClick={handleOptimizeRoute}
                    disabled={isOptimizing || selectedScheduleParcels.length === 0}
                  >
                    {isOptimizing ? 'Optimizing...' : 'Optimize Route'}
                  </button>
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
                      {availableTrucks.map(plate => (
                        <option key={plate} value={plate}>
                          {plate}
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
                  <button type="submit" className="schedule-submit" disabled={loading}>
                    {loading ? 'Submitting...' : 'Submit'}
                  </button>
                </div>
              </form>
            </section>
          ) : null}
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

