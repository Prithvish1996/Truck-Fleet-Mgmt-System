import React, { useState, useEffect } from 'react';
import { TruckParcel } from '../types';
import { plannerService, RouteResponse, ParcelResponse, DriverResponse } from '../services/plannerService';
import { formatParcelId, getFullDeliveryAddress } from '../utils/dataTransformers';
import './TruckDetailPage.css';

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
      // Try to find truck by plate number from available trucks
      // First, get unassigned routes to find truck
      const routeData = await plannerService.getUnassignedRoutes();
      let truck = routeData.trucks.find(t => t.plateNumber === truckPlateNo);
      
      // If found in unassigned routes, get routes by truck ID (status=PLANNED)
      if (truck) {
        // For unassigned routes, we can get them from routeData.unAssignedRoute
        const unassignedRoutes = routeData.unAssignedRoute?.filter(r => r.truckPlateNumber === truckPlateNo) || [];
        if (unassignedRoutes.length > 0) {
          extractParcelsFromRoutes(unassignedRoutes);
          return;
        }
      }

      // If not found, try to get assigned routes (status=ASSIGNED)
      // We need to find truck ID first - try all trucks
      if (!truck) {
        // Search through all trucks from unassigned routes response
        // Note: This might not work if truck is already assigned
        // In that case, we'd need truckId passed as prop
        setError('Truck not found in unassigned routes. The truck may already be assigned.');
        return;
      }

      // If truck found but no unassigned routes, try assigned routes
      // Store truck reference to help TypeScript understand it's not undefined
      const currentTruck = truck;
      try {
        const assignedRoutes = await plannerService.getRouteByTruckId(currentTruck.truckId);
        extractParcelsFromRoutes(assignedRoutes.routes);
      } catch (err: any) {
        // If no assigned routes, try unassigned again
        // Check truck exists before using it
        if (currentTruck && routeData.unAssignedRoute) {
          const unassignedRoutes = routeData.unAssignedRoute.filter(r => r.truckId === currentTruck.truckId);
          if (unassignedRoutes.length > 0) {
            extractParcelsFromRoutes(unassignedRoutes);
          } else {
            setError('No routes available for this truck.');
          }
        } else {
          setError('No routes available for this truck.');
        }
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

  const getDriverName = (driverId: string | null): string => {
    if (!driverId) return 'Unassigned';
    const driver = drivers.find(d => d.id.toString() === driverId);
    return driver ? (driver.userName || driver.Name || `Driver ${driver.id}`) : 'Unknown';
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
            <div className="truck-detail-table-container">
              <table className="truck-detail-table">
                <thead>
                  <tr>
                    <th>No.</th>
                    <th>Parcel ID</th>
                    <th>Customer</th>
                    <th>Delivery Location</th>
                    <th>Driver</th>
                  </tr>
                </thead>
                <tbody>
                  {getCurrentPageParcels().map((parcel, index) => {
                    const rowNumber = (currentPage - 1) * itemsPerPage + index + 1;
                    return (
                      <tr key={parcel.id}>
                        <td>{rowNumber}</td>
                        <td>
                          {onParcelClick ? (
                            <button
                              className="parcel-id-link"
                              onClick={() => onParcelClick(parcel.parcelId)}
                            >
                              {parcel.parcelId}
                            </button>
                          ) : (
                            parcel.parcelId
                          )}
                        </td>
                        <td>{parcel.customer}</td>
                        <td>{parcel.deliveryLocation}</td>
                        <td>{getDriverName(parcel.driverId)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="pagination">
              <button
                className="pagination-button"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
              >
                Previous page
              </button>
              <div className="pagination-numbers">
                {Array.from({ length: Math.min(7, totalPages) }, (_, i) => {     
                  let pageNum: number;
                  if (totalPages <= 7) {
                    pageNum = i + 1;
                  } else if (currentPage <= 4) {
                    pageNum = i + 1;
                  } else if (currentPage >= totalPages - 3) {
                    pageNum = totalPages - 6 + i;
                  } else {
                    pageNum = currentPage - 3 + i;
                  }
                  
                  return (
                    <button
                      key={pageNum}
                      className={`pagination-number ${currentPage === pageNum ? 'active' : ''}`}
                      onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum}
                    </button>
                  );
                })}
                {totalPages > 7 && <span className="pagination-ellipsis">...</span>}
              </div>
              <button
                className="pagination-button"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
              >
                Next page
              </button>
            </div>
          </>
        )}

        {!loading && !error && parcels.length === 0 && (
          <div style={{ padding: '20px', textAlign: 'center' }}>No parcels found for this truck.</div>
        )}

        {/* Return Button */}
        <div className="return-button-container">
          <button className="return-button" onClick={onReturn}>
            Return
          </button>
        </div>
      </div>
    </div>
  );
}
