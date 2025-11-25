import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { plannerService, ParcelResponse, DriverResponse } from '../../services/plannerService';
import { formatDate, formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
import RouteAssignmentPage from '../../components/RouteAssignmentPage/RouteAssignmentPage';
import RouteTrackingPage from '../../components/RouteTrackingPage/RouteTrackingPage';
import TruckDetailPage from '../../components/TruckDetailPage/TruckDetailPage';
import ParcelDetailPage from '../../components/ParcelDetailPage/ParcelDetailPage';
import RouteMapModal from '../../components/RouteMapModal/RouteMapModal';
import { RouteAssignment } from '../../types';
import DashboardHeader from './components/DashboardHeader';
import DashboardSidebar from './components/DashboardSidebar';
import SummaryCards from './components/SummaryCards';
import NewRequestsPanel from './components/NewRequestsPanel';
import AvailableDriversPanel from './components/AvailableDriversPanel';
import StatusMonitoringPanel from './components/StatusMonitoringPanel';
import SchedulePage from './components/SchedulePage';
import './PlannerDashboard.css';

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
  const [truckDetailPreviousPage, setTruckDetailPreviousPage] = useState<'assignment' | 'tracking' | 'truck-detail' | null>(null);
  const [selectedRouteAssignment, setSelectedRouteAssignment] = useState<RouteAssignment | null>(null);

  const [activeView, setActiveView] = useState<'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking' | 'truck-detail' | 'route-map'>('dashboard');
  const [newRequests, setNewRequests] = useState<DashboardRequest[]>([]);
  const [scheduleParcels, setScheduleParcels] = useState<ScheduleParcel[]>([]);
  const [selectedScheduleParcels, setSelectedScheduleParcels] = useState<string[]>([]);
  const [scheduleError, setScheduleError] = useState('');
  const [loading, setLoading] = useState(false);
  const [warehouses, setWarehouses] = useState<any[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<number | null>(null);
  const [availableTrucks, setAvailableTrucks] = useState<string[]>([]);
  const [availableDrivers, setAvailableDrivers] = useState<DriverResponse[]>([]);
  const [statusMonitoring, setStatusMonitoring] = useState<Array<{ driver: string; status: string; route: string }>>([]);
  const [requestTrucks, setRequestTrucks] = useState<Map<string, string>>(new Map());

  const [filterStatus, setFilterStatus] = useState<'All' | 'Pending' | 'Scheduled'>('All');
  const [sortBy, setSortBy] = useState<'id' | 'receiver' | 'location' | 'warehouse'>('id');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [searchText, setSearchText] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);

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
          
          const allParcelsData = await plannerService.getAllParcels(
            selectedWarehouseId,
            0,
            10000,
            searchText || undefined
          );
          
          console.log('Parcels data structure:', {
            hasData: !!allParcelsData,
            hasDataData: !!(allParcelsData && allParcelsData.data),
            isArray: Array.isArray(allParcelsData?.data),
            dataLength: allParcelsData?.data?.length
          });
          
          if (!allParcelsData || !allParcelsData.data) {
            console.warn('Invalid parcels data structure:', allParcelsData);
            setScheduleError('Invalid data format received from server.');
            setScheduleParcels([]);
            return;
          }
          
          const allParcels = allParcelsData.data;
          
          console.log(`Received total ${allParcels.length} parcels from API`);
          console.log('Parcels status breakdown:', {
            PENDING: allParcels.filter(p => p.status === 'PENDING').length,
            SCHEDULED: allParcels.filter(p => p.status === 'SCHEDULED').length,
            DELIVERED: allParcels.filter(p => p.status === 'DELIVERED').length,
            OTHER: allParcels.filter(p => !['PENDING', 'SCHEDULED', 'DELIVERED'].includes(p.status)).length
          });
          
          const parcels: ScheduleParcel[] = allParcels
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

  const loadScheduledDeliveries = async (retryCount = 0): Promise<void> => {
    if (activeView === 'dashboard') {
      setLoading(true);
      try {
        console.log('Loading scheduled deliveries...');
        
        if (selectedWarehouseId) {
          try {
            console.log('Trying to load scheduled parcels from getAllParcels...');
            
            const size = retryCount > 0 ? 100 : 10000;
            const allParcelsData = await plannerService.getAllParcels(selectedWarehouseId, 0, size);
            
            if (!allParcelsData || !allParcelsData.data) {
              throw new Error('Invalid data structure from getAllParcels');
            }
            
            const allParcels = allParcelsData.data;
            console.log(`Fetched all parcels. Total parcels collected: ${allParcels.length}`);
            
            if (Array.isArray(allParcels)) {
              const scheduledParcels = allParcels.filter(p => p.status === 'SCHEDULED');
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
                  let requestKey = '';
                  if (firstParcel.plannedDeliveryDate) {
                    const dateOnly = firstParcel.plannedDeliveryDate.split('T')[0];
                    requestKey = `${firstParcel.warehouseId}-${dateOnly}`;
                  } else {
                    requestKey = `${firstParcel.warehouseId}-no-date`;
                  }
                  const savedTruck = requestTrucks.get(requestKey);
                  const truckPlateId = savedTruck || 'TBD';
                  const deliveryDate = firstParcel.plannedDeliveryDate 
                    ? formatDate(firstParcel.plannedDeliveryDate)
                    : 'TBD';
                  
                  return {
                    truckPlateId,
                    deliveryDate,
                    parcels: parcels.length,
                    warehouse: firstParcel.warehouseCity || 'Unknown',
                    parcelIds: parcels.map(p => p.parcelId),
                    warehouseId: firstParcel.warehouseId
                  };
                });

                console.log('Final requests:', requests);
                
                // 合并现有的 requests 和新的 requests，去重
                setNewRequests(prevRequests => {
                  const existingKeys = new Set<string>();
                  prevRequests.forEach(req => {
                    const key = `${req.warehouseId}-${req.deliveryDate}`;
                    existingKeys.add(key);
                  });
                  
                  // 添加新的 requests，避免重复
                  const mergedRequests = [...prevRequests];
                  requests.forEach(newReq => {
                    const key = `${newReq.warehouseId}-${newReq.deliveryDate}`;
                    if (!existingKeys.has(key)) {
                      mergedRequests.push(newReq);
                      existingKeys.add(key);
                    } else {
                      // 如果已存在，更新它（使用新的数据）
                      const index = mergedRequests.findIndex(r => 
                        r.warehouseId === newReq.warehouseId && r.deliveryDate === newReq.deliveryDate
                      );
                      if (index >= 0) {
                        mergedRequests[index] = newReq;
                      }
                    }
                  });
                  
                  return mergedRequests;
                });
                
                try {
                  const routeData = await plannerService.getUnassignedRoutes();
                  const trucks = routeData.trucks
                    .filter(t => t.isAvailable)
                    .map(t => t.plateNumber);
                  setAvailableTrucks(trucks);
                } catch (error) {
                  console.error('Error loading trucks:', error);
                }
                
                setLoading(false);
                return;
              }
            }
          } catch (error: any) {
            if (error.message && error.message.includes('Too many requests') && retryCount < 3) {
              if (activeView !== 'dashboard') {
                return;
              }
              const delay = (retryCount + 1) * 2000;
              console.log(`Rate limited. Retrying after ${delay}ms (attempt ${retryCount + 1}/3)...`);
              await new Promise(resolve => setTimeout(resolve, delay));
              if (activeView === 'dashboard') {
                return loadScheduledDeliveries(retryCount + 1);
              }
            }
            console.warn('Failed to load scheduled parcels from getAllParcels, trying getScheduledDeliveries:', error);
          }
        }
        
        let data;
        try {
          data = await plannerService.getScheduledDeliveries(undefined, 1, 100);
        } catch (err: any) {
          if (err.message && err.message.includes('Too many requests') && retryCount < 3) {
            if (activeView !== 'dashboard') {
              return;
            }
            const delay = (retryCount + 1) * 2000;
            console.log(`Rate limited. Retrying after ${delay}ms (attempt ${retryCount + 1}/3)...`);
            await new Promise(resolve => setTimeout(resolve, delay));
            if (activeView === 'dashboard') {
              return loadScheduledDeliveries(retryCount + 1);
            }
          }
          throw err;
        }
        
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
          let requestKey = '';
          if (firstParcel.plannedDeliveryDate) {
            const dateOnly = firstParcel.plannedDeliveryDate.split('T')[0];
            requestKey = `${firstParcel.warehouseId}-${dateOnly}`;
          } else {
            requestKey = `${firstParcel.warehouseId}-no-date`;
          }
          const savedTruck = requestTrucks.get(requestKey);
          const truckPlateId = savedTruck || 'TBD';
          const deliveryDate = firstParcel.plannedDeliveryDate 
            ? formatDate(firstParcel.plannedDeliveryDate)
            : 'TBD';
          
          return {
            truckPlateId,
            deliveryDate,
            parcels: parcels.length,
            warehouse: firstParcel.warehouseCity || 'Unknown',
            parcelIds: parcels.map(p => p.parcelId),
            warehouseId: firstParcel.warehouseId
          };
        });

        console.log('Final requests:', requests);
        
        // 合并现有的 requests 和新的 requests，去重
        setNewRequests(prevRequests => {
          const existingKeys = new Set<string>();
          prevRequests.forEach(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            existingKeys.add(key);
          });
          
          // 添加新的 requests，避免重复
          const mergedRequests = [...prevRequests];
          requests.forEach(newReq => {
            const key = `${newReq.warehouseId}-${newReq.deliveryDate}`;
            if (!existingKeys.has(key)) {
              mergedRequests.push(newReq);
              existingKeys.add(key);
            } else {
              // 如果已存在，更新它（使用新的数据）
              const index = mergedRequests.findIndex(r => 
                r.warehouseId === newReq.warehouseId && r.deliveryDate === newReq.deliveryDate
              );
              if (index >= 0) {
                mergedRequests[index] = newReq;
              }
            }
          });
          
          return mergedRequests;
        });

        try {
          const routeData = await plannerService.getUnassignedRoutes();
          const trucks = routeData.trucks
            .filter(t => t.isAvailable)
            .map(t => t.plateNumber);
          setAvailableTrucks(trucks);
        } catch (error) {
          console.error('Error loading trucks:', error);
        }
      } catch (error: any) {
        console.error('Error loading scheduled deliveries:', error);
        console.error('Error details:', error?.message, error?.stack);
        // 不要清空现有的 requests，只记录错误
      } finally {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadScheduledDeliveries();
  }, [activeView]);

  const handleGenerateRouteClick = async () => {
    if (isOptimizing) {
      return;
    }

    if (newRequests.length === 0) {
      setScheduleError('No requests available to generate routes.');
      return;
    }

    setIsOptimizing(true);
    setScheduleError('');

    try {
      // 按 warehouseId 分组 requests
      const requestsByWarehouse = new Map<number, DashboardRequest[]>();
      newRequests.forEach(request => {
        if (!requestsByWarehouse.has(request.warehouseId)) {
          requestsByWarehouse.set(request.warehouseId, []);
        }
        requestsByWarehouse.get(request.warehouseId)!.push(request);
      });

      const allParcelIds: number[] = [];
      const warehouseEntries = Array.from(requestsByWarehouse.entries());
      const processedRequests: DashboardRequest[] = [];

      // 串行为每个 warehouseId 生成路由，避免 429 错误
      for (let i = 0; i < warehouseEntries.length; i++) {
        const [warehouseId, requests] = warehouseEntries[i];
        const parcelIds: number[] = [];
        requests.forEach((request: DashboardRequest) => {
          parcelIds.push(...request.parcelIds);
        });

        if (parcelIds.length === 0) {
          continue;
        }

        allParcelIds.push(...parcelIds);

        // 为每个 warehouse 生成路由，添加重试逻辑
        let retryCount = 0;
        const maxRetries = 3;
        let success = false;

        while (retryCount < maxRetries && !success) {
          try {
            console.log(`Generating routes for warehouse ${warehouseId} (attempt ${retryCount + 1}/${maxRetries})...`);
            console.log(`Parcel IDs for warehouse ${warehouseId}:`, parcelIds);
            
            await plannerService.generateRoutes({
              depot_id: warehouseId,
              warehouse_id: warehouseId,
              parcelIds: parcelIds
            });

            success = true;
            console.log(`Successfully generated routes for warehouse ${warehouseId}`);
            
            // 记录成功处理的 requests
            processedRequests.push(...requests);

            // 如果不是最后一个 warehouse，添加延迟避免 429 错误
            if (i < warehouseEntries.length - 1) {
              await new Promise(resolve => setTimeout(resolve, 1500)); // 1.5秒延迟
            }

          } catch (err: any) {
            console.error(`Error generating routes for warehouse ${warehouseId}:`, err);
            
            if (err.message && err.message.includes('Too many requests') && retryCount < maxRetries - 1) {
              retryCount++;
              const delay = retryCount * 2000; // 2s, 4s, 6s
              console.log(`Rate limited for warehouse ${warehouseId}. Retrying after ${delay}ms...`);
              await new Promise(resolve => setTimeout(resolve, delay));
              continue;
            }
            
            // 如果是 500 错误且还有重试次数，也重试
            if (err.message && (err.message.includes('Server error') || err.message.includes('500') || err.message.includes('Please try again later')) && retryCount < maxRetries - 1) {
              retryCount++;
              const delay = retryCount * 2000;
              console.log(`Server error for warehouse ${warehouseId}. Retrying after ${delay}ms...`);
              await new Promise(resolve => setTimeout(resolve, delay));
              continue;
            }
            
            // 重试次数用完或不可重试的错误，抛出异常
            throw err;
          }
        }

        if (!success) {
          throw new Error(`Failed to generate routes for warehouse ${warehouseId} after ${maxRetries} attempts`);
        }
      }

      if (allParcelIds.length === 0) {
        throw new Error('No parcels found in requests');
      }

      // 从 newRequests 中移除已成功处理的 requests
      if (processedRequests.length > 0) {
        setNewRequests(prevRequests => {
          const processedKeys = new Set<string>();
          processedRequests.forEach(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            processedKeys.add(key);
          });
          
          return prevRequests.filter(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            return !processedKeys.has(key);
          });
        });
      }

      setSelectedParcelIds(allParcelIds.map(id => id.toString()));
      setActiveView('route-assignment');
      setScheduleError('');
      setIsOptimizing(false);

    } catch (err: any) {
      console.error('Error generating routes:', err);
      console.error('Error details:', {
        message: err.message,
        stack: err.stack,
        name: err.name
      });
      
      let errorMessage = 'Failed to generate routes. ';
      if (err.message && err.message.includes('Too many requests')) {
        errorMessage += 'Please wait a moment and try again.';
      } else if (err.message && (err.message.includes('Server error') || err.message.includes('500'))) {
        errorMessage += err.message || 'Server error occurred. Please check if all parcels are scheduled and have valid delivery locations.';
      } else {
        errorMessage += err.message || 'Please try again.';
      }
      setScheduleError(errorMessage);
      setIsOptimizing(false);
    }
  };

  const handleGenerateRoute = async (selectedParcelIds: string[]) => {
    setSelectedParcelIds(selectedParcelIds);
    setActiveView('route-assignment');
  };

  const handleReturnFromAssignment = () => {
    setActiveView('dashboard');
  };

  const handleSubmitAssignments = (assignments: RouteAssignment[]) => {
    setSubmittedAssignments(assignments);
    setActiveView('route-tracking');
  };

  const handleReturnFromTracking = () => {
    setActiveView('route-assignment');
  };

  const handleTrackRoute = (assignment: RouteAssignment) => {
    setSelectedRouteAssignment(assignment);
    setActiveView('route-map');
  };

  const handleTruckClick = (truckPlateNo: string) => {
    setSelectedTruckPlateNo(truckPlateNo);
    if (activeView === 'route-assignment') {
      setTruckDetailPreviousPage('assignment');
    } else if (activeView === 'route-tracking') {
      setTruckDetailPreviousPage('tracking');
    }
    setActiveView('truck-detail');
  };

  const handleReturnFromTruckDetail = () => {
    if (truckDetailPreviousPage === 'assignment') {
      setActiveView('route-assignment');
    } else if (truckDetailPreviousPage === 'tracking') {
      setActiveView('route-tracking');
    } else {
      setActiveView('dashboard');
    }
    setTruckDetailPreviousPage(null);
    setSelectedTruckPlateNo('');
  };

  const handleParcelClick = (parcelId: string) => {
    setSelectedParcelId(parcelId);
    if (activeView === 'truck-detail') {
      setTruckDetailPreviousPage('truck-detail');
    }
    setShowParcelDetail(true);
  };

  const handleReturnFromParcelDetail = () => {
    setShowParcelDetail(false);
    if (truckDetailPreviousPage === 'truck-detail') {
      setActiveView('truck-detail');
    }
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
        warehouse_id: selectedWarehouseId,
        parcelIds: selectedParcelIds
      });
      
      setSelectedParcelIds(selectedParcelIds.map(id => id.toString()));
      setActiveView('route-assignment');
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

  const handleSelectAll = (selected: boolean) => {
    if (selected) {
      const selectableParcelIds = filteredAndSortedParcels
        .filter(p => p.selectable)
        .map(p => p.id);
      setSelectedScheduleParcels(selectableParcelIds);
    } else {
      setSelectedScheduleParcels([]);
    }
    setScheduleError('');
  };

  const resetScheduleForm = () => {
    setSelectedScheduleParcels([]);
    setScheduleError('');
  };


  const handleScheduleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (selectedScheduleParcels.length === 0) {
      setScheduleError('Select at least one parcel.');
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

      console.log('Submitting parcels:', { parcelIds });
      
      const scheduledParcels = await plannerService.scheduleParcels({
        parcelIds
      });
      console.log('Scheduled parcels response:', scheduledParcels);
      console.log('Number of parcels scheduled:', scheduledParcels?.length || 0);

      resetScheduleForm();
      setActiveView('dashboard');
      
      // 立即刷新 New Requests
      await loadScheduledDeliveries(0);
      
      // 如果第一次加载失败，使用重试机制
      let retryCount = 0;
      const maxRetries = 3;
      const retryDelay = 2000;
      
      const retryLoad = async () => {
        if (activeView !== 'dashboard') {
          return;
        }
        await new Promise(resolve => setTimeout(resolve, retryDelay));
        if (activeView !== 'dashboard') {
          return;
        }
        console.log(`Retrying load scheduled deliveries (attempt ${retryCount + 1}/${maxRetries})...`);
        await loadScheduledDeliveries(retryCount);
        retryCount++;
        
        if (retryCount < maxRetries && activeView === 'dashboard') {
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

  const handleViewChange = (view: 'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking') => {
    setActiveView(view);
    setScheduleError('');
    setShowTruckDetail(false);
  };

  const handleReturnFromRouteMap = () => {
    setActiveView('route-tracking');
    setSelectedRouteAssignment(null);
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
                  isGenerating={isOptimizing}
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
              searchText={searchText}
              filterStatus={filterStatus}
              sortBy={sortBy}
              sortOrder={sortOrder}
              loading={loading}
              scheduleError={scheduleError}
              onScheduleSubmit={handleScheduleSubmit}
              onParcelToggle={handleScheduleParcelToggle}
              onSelectAll={handleSelectAll}
              onSearchChange={setSearchText}
              onFilterStatusChange={setFilterStatus}
              onSortByChange={setSortBy}
              onSortOrderToggle={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
              onWarehouseChange={(id) => {
                setSelectedWarehouseId(id);
                setScheduleError('');
              }}
            />
          ) : activeView === 'route-assignment' ? (
            <RouteAssignmentPage
              selectedParcelIds={selectedParcelIds}
              onReturn={() => setActiveView('dashboard')}
              onSubmit={handleSubmitAssignments}
              onTruckClick={handleTruckClick}
            />
          ) : activeView === 'route-tracking' ? (
            <RouteTrackingPage
              assignments={submittedAssignments}
              onReturn={() => setActiveView('dashboard')}
              onTrack={handleTrackRoute}
              onTruckClick={handleTruckClick}
            />
          ) : activeView === 'truck-detail' ? (
            <TruckDetailPage
              truckPlateNo={selectedTruckPlateNo}
              onReturn={handleReturnFromTruckDetail}
              onParcelClick={handleParcelClick}
            />
          ) : activeView === 'route-map' ? (
            <RouteMapModal
              assignment={selectedRouteAssignment}
              onReturn={handleReturnFromRouteMap}
            />
          ) : null}
        </main>
      </div>

    </div>
  );
}

