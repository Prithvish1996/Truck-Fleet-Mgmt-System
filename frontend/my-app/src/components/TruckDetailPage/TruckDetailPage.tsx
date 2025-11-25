import React, { useState, useEffect } from 'react';
import { TruckParcel } from '../../types';
import { plannerService, RouteResponse, ParcelResponse, DriverResponse } from '../../services/plannerService';
import { formatParcelId, getFullDeliveryAddress } from '../../utils/dataTransformers';
import TruckParcelsTable from './TruckParcelsTable';
import Pagination from '../common/Pagination';
import '../TruckDetailPage.css';

interface TruckDetailPageProps {
  truckPlateNo: string;
  onReturn: () => void;
  onParcelClick?: (parcelId: string) => void;
}

export default function TruckDetailPage({ truckPlateNo, onReturn, onParcelClick }: TruckDetailPageProps) {
  const [parcels, setParcels] = useState<TruckParcel[]>([]);
  const [drivers, setDrivers] = useState<DriverResponse[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const itemsPerPage = 12;

  useEffect(() => {
    loadTruckParcels();
    loadDrivers();
  }, [truckPlateNo]);

  const loadTruckParcels = async () => {
    setLoading(true);
    setError('');
    try {
      const routeData = await plannerService.getUnassignedRoutes();
      const truck = routeData.trucks.find(t => t.plateNumber === truckPlateNo);
      
      if (!truck) {
        setError('Truck not found. It may not exist or may have been removed.');
        setLoading(false);
        return;
      }

      try {
        const assignedRoutes = await plannerService.getRouteByTruckId(truck.truckId);
        if (assignedRoutes.routes && assignedRoutes.routes.length > 0) {
          extractParcelsFromRoutes(assignedRoutes.routes);
          return;
        }
      } catch (err: any) {
        console.warn('No assigned routes found, checking unassigned routes:', err);
      }

      const unassignedRoutes = routeData.unAssignedRoute?.filter(r => r.truckId === truck.truckId) || [];
      if (unassignedRoutes.length > 0) {
        extractParcelsFromRoutes(unassignedRoutes);
      } else {
        setError('No routes available for this truck.');
      }
    } catch (err: any) {
      console.error('Error loading truck parcels:', err);
      setError(err.message || 'Failed to load truck parcels.');
    } finally {
      setLoading(false);
    }
  };

  const extractParcelsFromRoutes = (routes: RouteResponse[]) => {
    const allParcels: TruckParcel[] = [];
    let parcelIndex = 0;

    routes.forEach((route, routeIndex) => {
      route.routeStops?.forEach((stop, stopIndex) => {
        stop.parcelsToDeliver?.forEach((parcel: ParcelResponse, parcelIdx: number) => {
          allParcels.push({
            id: `parcel-${parcelIndex + 1}`,
            parcelId: formatParcelId(parcel.parcelId, parcelIndex),
            customer: parcel.recipientName || 'Unknown',
            deliveryLocation: getFullDeliveryAddress(parcel),
            driverId: route.driverId?.toString() || null
          });
          parcelIndex++;
        });
      });
    });

    setParcels(allParcels);
  };

  const loadDrivers = async () => {
    try {
      const driverList = await plannerService.getAvailableDrivers();
      setDrivers(driverList);
    } catch (error) {
      console.error('Error loading drivers:', error);
    }
  };

  const totalPages = Math.ceil(parcels.length / itemsPerPage);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const getCurrentPageParcels = () => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return parcels.slice(startIndex, endIndex);
  };

  return (
    <div className="truck-detail-page">
      <div className="truck-detail-container">
        <h2 className="truck-detail-title">{truckPlateNo} Truck</h2>
        
        {loading && (
          <div style={{ padding: '20px', textAlign: 'center' }}>Loading parcels...</div>
        )}

        {error && (
          <div style={{ padding: '20px', color: 'red', textAlign: 'center' }}>{error}</div>
        )}

        {!loading && !error && parcels.length > 0 && (
          <>
            <TruckParcelsTable
              parcels={getCurrentPageParcels()}
              currentPage={currentPage}
              itemsPerPage={itemsPerPage}
              drivers={drivers}
              onParcelClick={onParcelClick}
            />

            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </>
        )}

        {!loading && !error && parcels.length === 0 && (
          <div style={{ padding: '20px', textAlign: 'center' }}>No parcels found for this truck.</div>
        )}

        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}

