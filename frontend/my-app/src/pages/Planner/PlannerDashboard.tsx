import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { plannerService, ParcelResponse, DriverResponse } from '../../services/plannerService';
import { formatDate, formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
import RoutePlanningModal from '../../components/RoutePlanningModal/RoutePlanningModal';
import RouteAssignmentPage from '../../components/RouteAssignmentPage/RouteAssignmentPage';
import RouteTrackingPage from '../../components/RouteTrackingPage/RouteTrackingPage';
import TruckDetailPage from '../../components/TruckDetailPage/TruckDetailPage';
import ParcelDetailPage from '../../components/ParcelDetailPage/ParcelDetailPage';
import { RouteAssignment } from '../../types';
import DashboardHeader from './components/DashboardHeader';
import DashboardSidebar from './components/DashboardSidebar';
import SummaryCards from './components/SummaryCards';
import NewRequestsPanel from './components/NewRequestsPanel';
import AvailableDriversPanel from './components/AvailableDriversPanel';
import StatusMonitoringPanel from './components/StatusMonitoringPanel';
import SchedulePage from './components/SchedulePage';
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

  const [filterStatus, setFilterStatus] = useState<'All' | 'Pending' | 'Scheduled'>('All');
  const [sortBy, setSortBy] = useState<'id' | 'receiver' | 'location' | 'warehouse'>('id');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [searchText, setSearchText] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);

  const priorityOptions: PriorityLevel[] = ['High', 'Medium', 'Low'];

  useEffect(() => {
    const loadWarehouses = async () => {
      console.log('=== Starting to load warehouses ===');
      try {
        console.log('Calling plannerService.getWarehouses...');
        const data = await plannerService.getWarehouses(0, 100);
        console.log('Warehouses API response:', data);
        console.log('Data structure:', {
          hasData: !!data,
          hasDataData: !!(data && data.data),
          isArray: Array.isArray(data?.data),
          dataLength: data?.data?.length
        });
        
        if (data && data.data && Array.isArray(data.data)) {
          console.log(`Setting ${data.data.length} warehouses`);
          setWarehouses(data.data);
          if (data.data.length > 0) {
            console.log('Setting selected warehouse:', data.data[0].id, data.data[0].name);
            setSelectedWarehouseId(data.data[0].id);
          } else {
            console.warn('No warehouses found in response');
            setSelectedWarehouseId(null);
          }
        } else {
          console.warn('Invalid warehouses data structure:', data);
          console.warn('Data type:', typeof data);
          console.warn('Data keys:', data ? Object.keys(data) : 'null');
          setWarehouses([]);
          setSelectedWarehouseId(null);
        }
      } catch (error: any) {
        console.error('=== ERROR loading warehouses ===');
        console.error('Error object:', error);
        console.error('Error message:', error?.message);
        console.error('Error stack:', error?.stack);
        setWarehouses([]);
        setSelectedWarehouseId(null);
      }
      console.log('=== Finished loading warehouses ===');
    };
    loadWarehouses();
  }, []);

  useEffect(() => {
    const loadScheduleParcels = async () => {
      console.log('=== loadScheduleParcels called ===');
      console.log('activeView:', activeView);
      console.log('selectedWarehouseId:', selectedWarehouseId);
      
      if (activeView === 'schedule' && selectedWarehouseId) {
        console.log('Loading parcels for warehouse:', selectedWarehouseId);
        setLoading(true);
        setScheduleError('');
        try {
          console.log('Calling plannerService.getAllParcels...');
          const data = await plannerService.getAllParcels(
            selectedWarehouseId, 
            0, 
            100, 
            searchText || undefined
          );
          console.log('Parcels API response:', data);
          console.log('Parcels data structure:', {
            hasData: !!data,
            hasDataData: !!(data && data.data),
            isArray: Array.isArray(data?.data),
            dataLength: data?.data?.length
          });
          
          if (!data || !data.data) {
            console.warn('Invalid parcels data structure:', data);
            setScheduleError('Invalid data format received from server.');
            setScheduleParcels([]);
            return;
          }
          
          console.log(`Received ${data.data.length} parcels from API`);
          console.log('Parcels status breakdown:', {
            PENDING: data.data.filter(p => p.status === 'PENDING').length,
            SCHEDULED: data.data.filter(p => p.status === 'SCHEDULED').length,
            DELIVERED: data.data.filter(p => p.status === 'DELIVERED').length,
            OTHER: data.data.filter(p => !['PENDING', 'SCHEDULED', 'DELIVERED'].includes(p.status)).length
          });
          
          const parcels: ScheduleParcel[] = (data.data || [])
            .map((p, index) => ({
              id: formatParcelId(p.parcelId, index),
              parcelId: p.parcelId,
              receiver: p.recipientName || 'Unknown',
              location: getFullDeliveryAddress(p),
              warehouse: p.warehouseCity || 'Unknown',
              status: p.status === 'PENDING' ? 'Pending' as const : 'Scheduled' as const,
              selectable: p.status === 'PENDING',
              weight: p.weight,
              volume: p.volume,
              phone: p.recipientPhone,
              deliveryInstructions: p.deliveryInstructions,
              createdAt: p.createdAt
            }));
          
          console.log(`Processed ${parcels.length} parcels (all statuses)`);
          setScheduleParcels(parcels);
        } catch (error: any) {
          console.error('=== ERROR loading schedule parcels ===');
          console.error('Error object:', error);
          console.error('Error message:', error?.message);
          console.error('Error stack:', error?.stack);
          const errorMessage = error?.message || 'Failed to load parcels. Please try again.';
          setScheduleError(errorMessage);
          setScheduleParcels([]);
        } finally {
          console.log('Setting loading to false');
          setLoading(false);
        }
      } else if (activeView === 'schedule' && !selectedWarehouseId) {
        console.log('No warehouse selected, cannot load parcels');
        setScheduleParcels([]);
        setScheduleError('');
        setLoading(false);
      } else {
        console.log('Not loading parcels - activeView:', activeView, 'selectedWarehouseId:', selectedWarehouseId);
      }
    };
    loadScheduleParcels();
  }, [activeView, selectedWarehouseId, searchText]);

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

  useEffect(() => {
    const loadStatusMonitoring = async () => {
      if (activeView === 'dashboard') {
        try {
          const drivers = await plannerService.getAvailableDrivers();
          const statusData: Array<{ driver: string; status: string; route: string }> = [];
          
          await Promise.all(
            drivers.map(async (driver) => {
              try {
                const routeData = await plannerService.getRouteByDriverId(driver.id);
                
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

  const loadScheduledDeliveries = async () => {
    if (activeView === 'dashboard') {
      setLoading(true);
      try {
        console.log('Loading scheduled deliveries...');
        
        // 使用 getAllParcels 获取所有 parcels，然后过滤 SCHEDULED 状态
        // 这样可以获取所有日期的 scheduled parcels，而不只是"明天"的
        if (selectedWarehouseId) {
          try {
            console.log('Trying to load scheduled parcels from getAllParcels...');
            const allParcelsData = await plannerService.getAllParcels(selectedWarehouseId, 0, 1000);
            console.log('All parcels data:', allParcelsData);
            
            if (allParcelsData && allParcelsData.data && Array.isArray(allParcelsData.data)) {
              // 过滤出 SCHEDULED 状态的 parcels
              const scheduledParcels = allParcelsData.data.filter(p => p.status === 'SCHEDULED');
              console.log(`Found ${scheduledParcels.length} scheduled parcels from getAllParcels`);
              
              if (scheduledParcels.length > 0) {
                const grouped = new Map<string, ParcelResponse[]>();
                scheduledParcels.forEach(parcel => {
                  const key = `${parcel.warehouseId}-${parcel.plannedDeliveryDate || 'no-date'}`;
                  if (!grouped.has(key)) {
                    grouped.set(key, []);
                  }
                  grouped.get(key)!.push(parcel);
                });

                console.log(`Grouped into ${grouped.size} requests`);

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

                console.log('Final requests:', requests);
                setNewRequests(requests);
                
                // 加载 trucks
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
                }
                
                setLoading(false);
                return;
              }
            }
          } catch (error) {
            console.warn('Failed to load scheduled parcels from getAllParcels, trying getScheduledDeliveries:', error);
          }
        }
        
        // 回退到原来的方法
        const data = await plannerService.getScheduledDeliveries(undefined, 1, 100);
        
        console.log('Scheduled deliveries data:', data);
        console.log('Data structure check:', {
          hasData: !!data,
          hasDataData: !!(data && data.data),
          isArray: Array.isArray(data?.data),
          dataLength: data?.data?.length,
          totalItems: data?.totalItems
        });
        
        if (!data || !data.data || !Array.isArray(data.data)) {
          console.warn('Invalid scheduled deliveries data format:', data);
          setNewRequests([]);
          return;
        }
        
        console.log(`Found ${data.data.length} scheduled parcels`);
        
        const grouped = new Map<string, ParcelResponse[]>();
        data.data.forEach(parcel => {
          const key = `${parcel.warehouseId}-${parcel.plannedDeliveryDate || 'no-date'}`;
          if (!grouped.has(key)) {
            grouped.set(key, []);
          }
          grouped.get(key)!.push(parcel);
        });

        console.log(`Grouped into ${grouped.size} requests`);

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

        console.log('Final requests:', requests);
        setNewRequests(requests);

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
        }
      } catch (error: any) {
        console.error('Error loading scheduled deliveries:', error);
        console.error('Error details:', error?.message, error?.stack);
        setNewRequests([]);
      } finally {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
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

  const filteredAndSortedParcels = useMemo(() => {
    let filtered = scheduleParcels;
    
    if (filterStatus !== 'All') {
      filtered = filtered.filter(p => 
        filterStatus === 'Pending' ? p.status === 'Pending' : p.status === 'Scheduled'
      );
    }
    
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
      const selectedParcelIds = selectedScheduleParcels
        .map(id => {
          const parcel = scheduleParcels.find(p => p.id === id);
          return parcel?.parcelId;
        })
        .filter((id): id is number => id !== undefined);
      
      if (selectedParcelIds.length === 0) {
        throw new Error('No valid parcels selected');
      }
      
      const result = await plannerService.generateRoutes({
        depot_id: selectedWarehouseId,
        parcelIds: selectedParcelIds
      });
      
      setSelectedParcelIds(selectedParcelIds.map(id => id.toString()));
      setShowRouteAssignment(true);
      setActiveView('dashboard');
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
      const parcelIds = selectedScheduleParcels
        .map(id => {
          const match = id.match(/P(\d+)/);
          return match ? parseInt(match[1], 10) : 0;
        })
        .filter(id => id > 0);

      if (parcelIds.length === 0) {
        setScheduleError('Invalid parcel IDs selected.');
        setLoading(false);
        return;
      }

      const deliveryDate = scheduleDate ? new Date(scheduleDate).toISOString() : undefined;

      console.log('Submitting parcels:', { parcelIds, deliveryDate });
      const scheduledParcels = await plannerService.scheduleParcels({
        parcelIds,
        deliveryDate
      });
      console.log('Scheduled parcels response:', scheduledParcels);
      console.log('Number of parcels scheduled:', scheduledParcels?.length || 0);

      resetScheduleForm();
      setActiveView('dashboard');
      
      // 增加延迟并重试，确保后端数据已保存
      let retryCount = 0;
      const maxRetries = 3;
      const retryDelay = 1500;
      
      const retryLoad = async () => {
        await new Promise(resolve => setTimeout(resolve, retryDelay));
        console.log(`Retrying load scheduled deliveries (attempt ${retryCount + 1}/${maxRetries})...`);
        await loadScheduledDeliveries();
        retryCount++;
        
        if (retryCount < maxRetries) {
          setTimeout(retryLoad, retryDelay);
        }
      };
      
      setTimeout(retryLoad, retryDelay);
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

  const today = new Date();
  const todayMonthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const todayFormatted = `${todayMonthNames[today.getMonth()]} ${today.getDate()}`;

  const todayRequestsCount = newRequests.filter(request => {
    if (!request.deliveryDate || request.deliveryDate === 'TBD') return false;
    return request.deliveryDate.startsWith(todayFormatted);
  }).length;

  const processCount = statusMonitoring.filter(item => {
    const status = item.status?.toUpperCase() || '';
    return status === 'ASSIGNED' || status === 'IN_PROGRESS';
  }).length;

  const exceptionsCount = statusMonitoring.filter(item => {
    const status = item.status?.toUpperCase() || '';
    return status === 'CANCELLED' || status === 'EXCEPTION';
  }).length;

  const summaryCards = [
    { 
      title: "Today's Requests", 
      value: todayRequestsCount.toString(), 
      delta: '', 
      trend: 'down' as const 
    },
    { 
      title: 'Available Drivers', 
      value: availableDrivers.length.toString(), 
      delta: '', 
      trend: 'up' as const 
    },
    { 
      title: 'Process', 
      value: processCount.toString(), 
      delta: '', 
      trend: 'up' as const 
    },
    { 
      title: 'Exceptions', 
      value: exceptionsCount.toString(), 
      delta: exceptionsCount > 0 ? 'Warning' : '', 
      trend: exceptionsCount > 0 ? 'warning' as const : 'down' as const 
    }
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

  const handleViewChange = (view: 'dashboard' | 'schedule') => {
    setActiveView(view);
    setScheduleError('');
  };

  return (
    <div className="planner-dashboard">
      <DashboardHeader isLoggingOut={isLoggingOut} onLogout={handleLogout} />

      <div className="content-shell">
        <DashboardSidebar activeView={activeView} onViewChange={handleViewChange} />

        <main className={`dashboard-main ${activeView === 'schedule' ? 'schedule-view' : ''}`}>
          {activeView === 'dashboard' ? (
            <>
              <SummaryCards cards={summaryCards} />

              <section className="dashboard-grid">
                <NewRequestsPanel 
                  requests={newRequests} 
                  onGenerateRouteClick={handleGenerateRouteClick} 
                />
                <AvailableDriversPanel drivers={availableDrivers} />
              </section>

              <section className="status-monitoring-section">
                <StatusMonitoringPanel statusData={statusMonitoring} />
              </section>
            </>
          ) : activeView === 'schedule' ? (
            <SchedulePage
              warehouses={warehouses}
              selectedWarehouseId={selectedWarehouseId}
              scheduleParcels={scheduleParcels}
              filteredAndSortedParcels={filteredAndSortedParcels}
              selectedScheduleParcels={selectedScheduleParcels}
              scheduleDate={scheduleDate}
              scheduleTruck={scheduleTruck}
              schedulePriority={schedulePriority}
              availableTrucks={availableTrucks}
              priorityOptions={priorityOptions}
              searchText={searchText}
              filterStatus={filterStatus}
              sortBy={sortBy}
              sortOrder={sortOrder}
              loading={loading}
              isOptimizing={isOptimizing}
              scheduleError={scheduleError}
              onScheduleSubmit={handleScheduleSubmit}
              onParcelToggle={handleScheduleParcelToggle}
              onOptimizeRoute={handleOptimizeRoute}
              onSearchChange={setSearchText}
              onFilterStatusChange={setFilterStatus}
              onSortByChange={setSortBy}
              onSortOrderToggle={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
              onWarehouseChange={(id) => {
                setSelectedWarehouseId(id);
                setScheduleError('');
              }}
              onDateChange={(date) => {
                setScheduleDate(date);
                setScheduleError('');
              }}
              onTruckChange={(truck) => {
                setScheduleTruck(truck);
                setScheduleError('');
              }}
              onPriorityChange={(priority) => {
                setSchedulePriority(priority);
                setScheduleError('');
              }}
            />
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

