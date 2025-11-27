import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../../services/authService';
import { plannerService, ParcelResponse, DriverResponse } from '../../../services/plannerService';
import { formatDate, formatParcelId, getFullDeliveryAddress } from '../../../utils/dataTransformers';
import { requestCache } from '../../../utils/requestCache';
import ParcelDetailPage from '../ParcelDetailPage/ParcelDetailPage';
import { RouteAssignment } from '../../../types';
import DashboardHeader from '../../../components/planner components/DashboardHeader';
import DashboardSidebar from '../../../components/planner components/DashboardSidebar';
import ViewRouter from '../../../components/planner components/ViewRouter';
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
          if (errorMessage.includes('Too many requests')) {
            setScheduleError('Rate limit reached. Please wait a moment and try again.');
          } else {
          setScheduleError(errorMessage);
          }
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
    
    const timer = setTimeout(() => {
    loadScheduleParcels();
    }, searchText ? 500 : 0);
    
    return () => clearTimeout(timer);
  }, [activeView, selectedWarehouseId, searchText]);

  useEffect(() => {
    const loadAvailableDrivers = async () => {
      if (activeView === 'dashboard') {
        try {
          const now = Date.now();
          
          const assignedDriverIds = submittedAssignments
            .map(a => parseInt(a.driverId || '0', 10))
            .filter(id => id > 0);
          
          if (!availableDriversCacheRef.current || 
              (now - availableDriversCacheRef.current.timestamp) >= CACHE_DURATION) {
            try {
              const drivers = await requestCache.get(
                'availableDrivers',
                () => plannerService.getAvailableDrivers()
              );
              
              const filtered = drivers.filter(d => d.isAvailable);
              availableDriversCacheRef.current = {
                data: drivers,
                timestamp: now
              };
              
              const finalFiltered = filtered.filter(d => !assignedDriverIds.includes(d.id));
              setAvailableDrivers(finalFiltered);
              console.log('Loaded available drivers for dashboard:', finalFiltered.length, 'Assigned drivers filtered:', assignedDriverIds);
            } catch (error: any) {
              if (error.message && error.message.includes('Too many requests')) {
                console.warn('Rate limit reached when loading available drivers. Using cached data if available.');
                if (availableDriversCacheRef.current) {
                  const filtered = availableDriversCacheRef.current.data
                    .filter(d => d.isAvailable && !assignedDriverIds.includes(d.id));
                  setAvailableDrivers(filtered);
                  console.log('Using cached available drivers due to rate limit:', filtered.length);
                } else {
                  setAvailableDrivers([]);
                }
              } else {
                throw error;
              }
            }
          } else {
            const filtered = availableDriversCacheRef.current.data
              .filter(d => d.isAvailable && !assignedDriverIds.includes(d.id));
            setAvailableDrivers(filtered);
            console.log('Using cached available drivers:', filtered.length, 'Assigned drivers filtered:', assignedDriverIds);
          }
        } catch (error: any) {
          console.error('Error loading available drivers:', error);
          if (error.message && error.message.includes('Too many requests')) {
            if (availableDriversCacheRef.current) {
              const filtered = availableDriversCacheRef.current.data
                .filter(d => d.isAvailable);
              setAvailableDrivers(filtered);
            } else {
              setAvailableDrivers([]);
            }
          } else {
            setAvailableDrivers([]);
          }
        }
      }
    };
    loadAvailableDrivers();
  }, [activeView, submittedAssignments]);

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
            try {
              drivers = await requestCache.get(
                'availableDrivers',
                () => plannerService.getAvailableDrivers()
              );
              availableDriversCacheRef.current = {
                data: drivers,
                timestamp: now
              };
            } catch (error: any) {
              if (error.message && error.message.includes('Too many requests')) {
                console.warn('Rate limit reached when loading drivers for status monitoring. Using cached data if available.');
                if (availableDriversCacheRef.current) {
                  drivers = availableDriversCacheRef.current.data;
                } else {
                  console.warn('No cached drivers available. Skipping status monitoring.');
                  setStatusMonitoring([]);
                  return;
                }
              } else {
                throw error;
              }
            }
          }

          const statusData: Array<{ driver: string; status: string; route: string }> = [];
          let has429Error = false;
          
          for (const driver of drivers) {
            if (has429Error) {
              console.warn('Rate limit reached. Stopping status monitoring requests.');
              break;
            }

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
              if (error.message && error.message.includes('Too many requests')) {
                has429Error = true;
                console.warn('Rate limit reached. Stopping further driver route requests.');
                break;
              }
            }
            
            await new Promise(resolve => setTimeout(resolve, 500));
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
              
              if (scheduledParcels.length === 0) {
                const otherStatusParcels = allParcels.filter(p => 
                  p.status !== 'SCHEDULED' && 
                  p.status !== 'DELIVERED' && 
                  p.plannedDeliveryDate
                );
                if (otherStatusParcels.length > 0) {
                  console.warn(`Found ${otherStatusParcels.length} parcels with OTHER status but have plannedDeliveryDate. Status: ${otherStatusParcels[0].status}`);
                  console.warn('These parcels may not be in SCHEDULED status. Route generation may fail.');
                }
              }
              
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
                
                setNewRequests(prevRequests => {
                  const existingKeys = new Set<string>();
                  prevRequests.forEach(req => {
                    const key = `${req.warehouseId}-${req.deliveryDate}`;
                    existingKeys.add(key);
                  });
                  
                  const mergedRequests = [...prevRequests];
                  requests.forEach(newReq => {
                    const key = `${newReq.warehouseId}-${newReq.deliveryDate}`;
                    if (!existingKeys.has(key)) {
                      mergedRequests.push(newReq);
                      existingKeys.add(key);
                    } else {
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
                } catch (error: any) {
                  console.error('Error loading trucks:', error);
                  if (error.message && error.message.includes('Too many requests')) {
                    console.warn('Rate limit reached. Skipping truck loading.');
                  }
                  setAvailableTrucks([]);
                }
                
                setLoading(false);
                return;
              }
            }
          } catch (error: any) {
            console.warn('Failed to load scheduled parcels from getAllParcels, trying getScheduledDeliveries:', error);
            if (error.message && error.message.includes('Too many requests')) {
              console.warn('Rate limit reached. Will retry later.');
              setLoading(false);
              return;
            }
          }
        }
        
        let data;
        try {
          data = await requestCache.get(
            'getScheduledDeliveries',
            () => plannerService.getScheduledDeliveries(undefined, 1, 100)
          );
        } catch (err: any) {
          if (err.message && err.message.includes('Too many requests')) {
            console.warn('Rate limit reached for getScheduledDeliveries. Will retry later.');
            setLoading(false);
            return;
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
        
        setNewRequests(prevRequests => {
          const existingKeys = new Set<string>();
          prevRequests.forEach(req => {
            const key = `${req.warehouseId}-${req.deliveryDate}`;
            existingKeys.add(key);
          });
          
          const mergedRequests = [...prevRequests];
          requests.forEach(newReq => {
            const key = `${newReq.warehouseId}-${newReq.deliveryDate}`;
            if (!existingKeys.has(key)) {
              mergedRequests.push(newReq);
              existingKeys.add(key);
            } else {
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
        } catch (error: any) {
          console.error('Error loading trucks:', error);
          if (error.message && error.message.includes('Too many requests')) {
            console.warn('Rate limit reached. Skipping truck loading.');
          }
          setAvailableTrucks([]);
        }
      } catch (error: any) {
        console.error('Error loading scheduled deliveries:', error);
        console.error('Error details:', error?.message, error?.stack);
        if (error.message && error.message.includes('Too many requests')) {
          console.warn('Rate limit reached. Will retry later.');
        }
      } finally {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
    loadScheduledDeliveries();
    }, 300);
    
    return () => clearTimeout(timer);
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
          
          try {
            await plannerService.generateRoutes({
              depot_id: defaultDepotId || 1,
              warehouse_id: warehouseId,
              parcelIds: parcelIds
            });
          } catch (routeError: any) {
            if (routeError.message && routeError.message.includes('Only parcels with status')) {
              const invalidIdsMatch = routeError.message.match(/Invalid IDs: \[(.*?)\]/);
              const invalidIds = invalidIdsMatch ? invalidIdsMatch[1].split(', ').map((id: string) => id.trim()) : [];
              throw new Error(`Some parcels are not in SCHEDULED status and cannot be planned. Invalid parcel IDs: ${invalidIds.join(', ')}. Please ensure all parcels are scheduled before generating routes.`);
            }
            throw routeError;
          }

          console.log(`Successfully generated routes for warehouse ${warehouseId}`);
          
          allParcelIds.push(...parcelIds);
          processedRequests.push(...requests);

          if (i < warehouseEntries.length - 1) {
            await new Promise(resolve => setTimeout(resolve, 500));
          }
        } catch (err: any) {
          console.error(`Error generating routes for warehouse ${warehouseId}:`, err);
          let errorMsg = err.message || `Failed to generate routes for warehouse ${warehouseId}`;
          
          if (err.message && err.message.includes('Only parcels with status')) {
            errorMsg = `Parcels are not in SCHEDULED status. ${errorMsg}`;
          } else if (err.message && err.message.includes('Invalid parcel IDs')) {
            errorMsg = err.message;
          }
          
          errors.push(`Warehouse ${warehouseId}: ${errorMsg}`);
          failedRequests.push(...requests);
          
          if (err.message && err.message.includes('Too many requests')) {
            has429Error = true;
            console.log('Rate limit reached. Stopping further route generation.');
            break;
          }
        }
      }

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

      console.log('Route generation result:', {
        processedRequests: processedRequests.length,
        allParcelIds: allParcelIds.length,
        errors: errors.length,
        failedRequests: failedRequests.length
      });
      
      if (processedRequests.length > 0 && allParcelIds.length > 0) {
        console.log('Jumping to route-assignment page');
        
        requestCache.invalidate('getUnassignedRoutes');
        
        setSelectedParcelIds(allParcelIds.map(id => id.toString()));
        setActiveView('route-assignment');
        
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
        console.log('Forcing view back to dashboard from:', activeView);
        setActiveView('dashboard');
        setSelectedParcelIds([]);
      }

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

  const handleSubmitAssignments = async (assignments: RouteAssignment[]) => {
    console.log('handleSubmitAssignments called with assignments:', assignments);
    
    setSubmittedAssignments(prev => {
      const newAssignmentKeys = new Set(assignments.map(a => a.routeId?.toString()).filter(Boolean));
      
      const filteredPrev = prev.filter(a => !newAssignmentKeys.has(a.routeId?.toString()));
      
      const merged = [...filteredPrev, ...assignments];
      console.log('Merged submitted assignments:', merged.length, 'Previous:', prev.length, 'New:', assignments.length);
      return merged;
    });
    
    const assignedDriverIds = assignments.map(a => parseInt(a.driverId || '0', 10)).filter(id => id > 0);
    console.log('Assigned driver IDs to remove:', assignedDriverIds);
    
    if (assignedDriverIds.length > 0) {
      setAvailableDrivers(prev => {
        const updated = prev.filter(driver => !assignedDriverIds.includes(driver.id));
        console.log('Updated available drivers count:', updated.length, 'Removed:', prev.length - updated.length);
        return updated;
      });
      
      if (availableDriversCacheRef.current) {
        availableDriversCacheRef.current = {
          data: availableDriversCacheRef.current.data.filter(d => !assignedDriverIds.includes(d.id)),
          timestamp: Date.now()
        };
        console.log('Updated cache, remaining drivers:', availableDriversCacheRef.current.data.length);
      }
    }
    
    requestCache.invalidate('availableDrivers');
    
    setActiveView('route-tracking');
  };

  const handleReturnFromTracking = () => {
    requestCache.invalidate('getUnassignedRoutes');
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
      requestCache.invalidate('getUnassignedRoutes');
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

      requestCache.invalidatePattern('getAllParcels-');
      requestCache.invalidate('getScheduledDeliveries');

      resetScheduleForm();
      setActiveView('dashboard');
      
      await new Promise(resolve => setTimeout(resolve, 500));
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

  // Debug: Monitor availableDrivers changes
  useEffect(() => {
    console.log('Available drivers state updated:', availableDrivers.length, 'drivers:', availableDrivers.map(d => d.id));
  }, [availableDrivers]);

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
          <ViewRouter
            activeView={activeView}
            selectedParcelIds={selectedParcelIds}
            newRequests={newRequests}
            availableDrivers={availableDrivers}
            statusMonitoring={statusMonitoring}
            summaryCards={summaryCards}
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
            submittedAssignments={submittedAssignments}
            selectedTruckPlateNo={selectedTruckPlateNo}
            selectedRouteAssignment={selectedRouteAssignment}
            isOptimizing={isOptimizing}
            onGenerateRouteClick={handleGenerateRouteClick}
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
            onSetActiveView={setActiveView}
            onSubmitAssignments={handleSubmitAssignments}
            onTruckClick={handleTruckClick}
            onTrackRoute={handleTrackRoute}
            onReturnFromTruckDetail={handleReturnFromTruckDetail}
            onParcelClick={handleParcelClick}
            onReturnFromRouteMap={handleReturnFromRouteMap}
          />
        </main>
      </div>

    </div>
  );
}

