import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { plannerService, ParcelResponse, DriverResponse } from '../../services/plannerService';
import { formatDate, formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
import { requestCache } from '../../utils/requestCache';
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
  const [depots, setDepots] = useState<Array<{ id: number; name: string; capacity: number; location: any }>>([]);
  const [defaultDepotId, setDefaultDepotId] = useState<number | null>(null);
  const [availableTrucks, setAvailableTrucks] = useState<string[]>([]);
  const [availableDrivers, setAvailableDrivers] = useState<DriverResponse[]>([]);
  const [statusMonitoring, setStatusMonitoring] = useState<Array<{ driver: string; status: string; route: string }>>([]);
  const [requestTrucks, setRequestTrucks] = useState<Map<string, string>>(new Map());

  const [filterStatus, setFilterStatus] = useState<'All' | 'Pending' | 'Scheduled'>('All');
  const [sortBy, setSortBy] = useState<'id' | 'receiver' | 'location' | 'warehouse'>('id');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [searchText, setSearchText] = useState('');
  const [isOptimizing, setIsOptimizing] = useState(false);
  
  const availableDriversCacheRef = useRef<{ data: DriverResponse[]; timestamp: number } | null>(null);
  const CACHE_DURATION = 3000;

  useEffect(() => {
    const loadDepots = async () => {
      try {
        const depotList = await plannerService.getDepots();
        setDepots(depotList);
        if (depotList.length > 0) {
          setDefaultDepotId(depotList[0].id);
        }
      } catch (error) {
        console.error('Error loading depots:', error);
        setDefaultDepotId(1);
      }
    };

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
          
          const cacheKey = `getAllParcels-${selectedWarehouseId}-${searchText || 'all'}`;
          const allParcelsData = await requestCache.get(
            cacheKey,
            () => plannerService.getAllParcels(
              selectedWarehouseId,
              0,
              10000,
              searchText || undefined
            )
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
          const now = Date.now();
          if (availableDriversCacheRef.current && 
              (now - availableDriversCacheRef.current.timestamp) < CACHE_DURATION) {
            setAvailableDrivers(availableDriversCacheRef.current.data.filter(d => d.isAvailable));
            return;
          }

          const drivers = await requestCache.get(
            'availableDrivers',
            () => plannerService.getAvailableDrivers()
          );
          
          const filtered = drivers.filter(d => d.isAvailable);
          availableDriversCacheRef.current = {
            data: drivers,
            timestamp: now
          };
          
          setAvailableDrivers(filtered);
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
          const now = Date.now();
          let drivers: DriverResponse[];
          
          if (availableDriversCacheRef.current && 
              (now - availableDriversCacheRef.current.timestamp) < CACHE_DURATION) {
            drivers = availableDriversCacheRef.current.data;
          } else {
            drivers = await requestCache.get(
              'availableDrivers',
              () => plannerService.getAvailableDrivers()
            );
            availableDriversCacheRef.current = {
              data: drivers,
              timestamp: now
            };
          }

          const statusData: Array<{ driver: string; status: string; route: string }> = [];
          
          for (const driver of drivers) {
            try {
              const routeData = await plannerService.getRouteByDriverId(driver.id);
              
              if (routeData && routeData.routes && Array.isArray(routeData.routes) && routeData.routes.length > 0) {
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
              console.warn(`Error loading routes for driver ${driver.id}:`, error);
            }
            
            await new Promise(resolve => setTimeout(resolve, 100));
          }
          
          setStatusMonitoring(statusData);
        } catch (error) {
          console.error('Error loading status monitoring:', error);
          setStatusMonitoring([]);
        }
      }
    };
    loadStatusMonitoring();
  }, [activeView]);

  const loadScheduledDeliveries = async (): Promise<void> => {
    if (activeView === 'dashboard') {
      setLoading(true);
      try {
        console.log('Loading scheduled deliveries...');
        
        if (selectedWarehouseId) {
          try {
            console.log('Trying to load scheduled parcels from getAllParcels...');
            
            const size = 10000;
            const cacheKey = `getAllParcels-${selectedWarehouseId}-${size}`;
            const allParcelsData = await requestCache.get(
              cacheKey,
              () => plannerService.getAllParcels(selectedWarehouseId, 0, size)
            );
            
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
                  const routeData = await requestCache.get(
                    'getUnassignedRoutes',
                    () => plannerService.getUnassignedRoutes()
                  );
                  if (routeData && routeData.trucks) {
                    const trucks = routeData.trucks
                      .filter(t => t.isAvailable)
                      .map(t => t.plateNumber);
                    setAvailableTrucks(trucks);
                  } else {
                    setAvailableTrucks([]);
                  }
                } catch (error) {
                  console.error('Error loading trucks:', error);
                  setAvailableTrucks([]);
                }
                
                setLoading(false);
                return;
              }
            }
          } catch (error: any) {
            console.warn('Failed to load scheduled parcels from getAllParcels, trying getScheduledDeliveries:', error);
          }
        }
        
        let data;
        try {
          data = await requestCache.get(
            'getScheduledDeliveries',
            () => plannerService.getScheduledDeliveries(undefined, 1, 100)
          );
        } catch (err: any) {
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
          const routeData = await requestCache.get(
            'getUnassignedRoutes',
            () => plannerService.getUnassignedRoutes()
          );
          if (routeData && routeData.trucks) {
            const trucks = routeData.trucks
              .filter(t => t.isAvailable)
              .map(t => t.plateNumber);
            setAvailableTrucks(trucks);
          } else {
            setAvailableTrucks([]);
          }
        } catch (error) {
          console.error('Error loading trucks:', error);
          setAvailableTrucks([]);
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
      const failedRequests: DashboardRequest[] = [];
      const errors: string[] = [];
      let has429Error = false;

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

        try {
          console.log(`Generating routes for warehouse ${warehouseId}...`);
          console.log(`Parcel IDs for warehouse ${warehouseId}:`, parcelIds);
          
          await plannerService.generateRoutes({
            depot_id: defaultDepotId || 1,
            warehouse_id: warehouseId,
            parcelIds: parcelIds
          });

          console.log(`Successfully generated routes for warehouse ${warehouseId}`);
          
          allParcelIds.push(...parcelIds);
          processedRequests.push(...requests);

          if (i < warehouseEntries.length - 1) {
            await new Promise(resolve => setTimeout(resolve, 500));
          }
        } catch (err: any) {
          console.error(`Error generating routes for warehouse ${warehouseId}:`, err);
          const errorMsg = err.message || `Failed to generate routes for warehouse ${warehouseId}`;
          errors.push(`Warehouse ${warehouseId}: ${errorMsg}`);
          failedRequests.push(...requests);
          
          // 如果是429错误，停止处理后续warehouse
          if (err.message && err.message.includes('Too many requests')) {
            has429Error = true;
            console.log('Rate limit reached. Stopping further route generation.');
            break;
          }
        }
      }

      // 从 newRequests 中移除已处理的 requests（包括成功和失败的）
      const allProcessedRequests = [...processedRequests, ...failedRequests];
      if (allProcessedRequests.length > 0) {
        setNewRequests(prevRequests => {
          const processedKeys = new Set<string>();
          allProcessedRequests.forEach(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            processedKeys.add(key);
          });
          
          return prevRequests.filter(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            return !processedKeys.has(key);
          });
        });
      }

      // 如果有成功的请求，跳转到 route-assignment 页面
      console.log('Route generation result:', {
        processedRequests: processedRequests.length,
        allParcelIds: allParcelIds.length,
        errors: errors.length,
        failedRequests: failedRequests.length
      });
      
      if (processedRequests.length > 0 && allParcelIds.length > 0) {
        console.log('Jumping to route-assignment page');
        setSelectedParcelIds(allParcelIds.map(id => id.toString()));
        setActiveView('route-assignment');
        
        // 如果有错误，显示警告信息
        if (errors.length > 0) {
          if (has429Error) {
            setScheduleError(`Some routes generated successfully, but rate limit reached. Please wait before trying again. Failed: ${errors.join('; ')}`);
          } else {
            setScheduleError(`Some routes generated successfully, but some failed: ${errors.join('; ')}`);
          }
        } else {
          setScheduleError('');
        }
      } else {
        // 全部失败
        console.log('All requests failed - staying on dashboard');
        console.log('Current activeView before setting:', activeView);
        if (errors.length > 0) {
          if (has429Error) {
            setScheduleError(`Rate limit reached. Please wait a moment before trying again. Errors: ${errors.join('; ')}`);
          } else {
            setScheduleError(`Failed to generate routes for all warehouses: ${errors.join('; ')}`);
          }
        } else {
          setScheduleError('No parcels found in requests');
        }
        // 强制设置为 dashboard，不使用条件判断
        console.log('Forcing view back to dashboard from:', activeView);
        setActiveView('dashboard');
        // 清除 selectedParcelIds，防止 RouteAssignmentPage 被渲染
        setSelectedParcelIds([]);
      }

      // 如果有429错误，延迟重置状态，防止立即重试
      if (has429Error) {
        await new Promise(resolve => setTimeout(resolve, 3000));
      }
      
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
        await new Promise(resolve => setTimeout(resolve, 3000));
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

  const handleSubmitAssignments = async (assignments: RouteAssignment[]) => {
    setSubmittedAssignments(assignments);
    
    requestCache.invalidate('availableDrivers');
    availableDriversCacheRef.current = null;
    
    setTimeout(async () => {
      try {
        const drivers = await requestCache.get(
          'availableDrivers',
          () => plannerService.getAvailableDrivers()
        );
        setAvailableDrivers(drivers.filter(d => d.isAvailable));
      } catch (error) {
        console.error('Error loading available drivers:', error);
      }
    }, 500);
    
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
        depot_id: defaultDepotId || 1,
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
      
      await loadScheduledDeliveries();
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
    if (view === 'route-assignment' && selectedParcelIds.length === 0) {
      console.log('Cannot switch to route-assignment: no selected parcels');
      setScheduleError('No routes available for assignment. Please generate routes first.');
      return;
    }
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
        <DashboardSidebar 
          activeView={activeView} 
          onViewChange={handleViewChange}
          hasValidRoutes={selectedParcelIds.length > 0}
        />

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
          ) : activeView === 'route-assignment' && selectedParcelIds.length > 0 ? (
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

