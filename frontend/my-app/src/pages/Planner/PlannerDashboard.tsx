import React, { useEffect, useState } from 'react';
import './PlannerDashboard.css';
import { plannerParcelService, PlannerParcel, PaginatedResult } from '../../services/plannerParcelService';
import { plannerDriverService, PlannerDriver } from '../../services/plannerDriverService';
import { plannerRouteService } from '../../services/plannerRouteService';
import { RouteData, RouteStop, Parcel as RouteParcel } from '../../types';

const PlannerDashboard: React.FC = () => {
  
  const [warehouseId, setWarehouseId] = useState<number>(1);
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(10);

  
  const [parcels, setParcels] = useState<PlannerParcel[]>([]);
  const [parcelPageInfo, setParcelPageInfo] = useState<Pick<PaginatedResult<PlannerParcel>, 'totalItems' | 'totalPages' | 'currentPage'>>({
    totalItems: 0,
    totalPages: 0,
    currentPage: 0,
  });

  const [drivers, setDrivers] = useState<PlannerDriver[]>([]);
  const [truckId, setTruckId] = useState<number>(1);
  const [routes, setRoutes] = useState<RouteData[]>([]);

  
  const [loadingParcels, setLoadingParcels] = useState(false);
  const [loadingDrivers, setLoadingDrivers] = useState(false);
  const [loadingRoutes, setLoadingRoutes] = useState(false);
  const [error, setError] = useState<string | null>(null);

  
  useEffect(() => {
    const loadParcels = async () => {
      setLoadingParcels(true);
      setError(null);
      try {
        const result = await plannerParcelService.getWarehouseParcels({
          warehouseId,
          page,
          size,
        });
        setParcels(result.data);
        setParcelPageInfo({
          totalItems: result.totalItems,
          totalPages: result.totalPages,
          currentPage: result.currentPage,
        });
      } catch (e: any) {
        console.error(e);
        setError(e.message ?? 'Failed to load parcels');
      } finally {
        setLoadingParcels(false);
      }
    };

    loadParcels();
  }, [warehouseId, page, size]);

  
  useEffect(() => {
    const loadDrivers = async () => {
      setLoadingDrivers(true);
      setError(null);
      try {
        const result = await plannerDriverService.getAvailableDrivers();
        setDrivers(result);
      } catch (e: any) {
        console.error(e);
        setError(e.message ?? 'Failed to load drivers');
      } finally {
        setLoadingDrivers(false);
      }
    };

    loadDrivers();
  }, []);

  // discuss with Johnson + Rishi
  const handleLoadRoutes = async () => {
    setLoadingRoutes(true);
    setError(null);
    try {
      const result = await plannerRouteService.getRoutesByTruckId(truckId);
      setRoutes(result);
    } catch (e: any) {
      console.error(e);
      setError(e.message ?? 'Failed to load routes');
    } finally {
      setLoadingRoutes(false);
    }
  };

  const handleWarehouseChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value);
    setWarehouseId(value);
    setPage(0);
  };

  const formatStopSummary = (stop: RouteStop): string => {
    const parcels = stop.parcelsToDeliver as RouteParcel[];
    if (!parcels || parcels.length === 0) return '-';
    const first = parcels[0];
    return `${first.recipientName} – ${first.deliveryAddress}, ${first.deliveryCity}`;
  };

  return (
    <div className="planner-dashboard">
      <h1>Planner Dashboard</h1>

      {error && <div className="planner-error">{error}</div>}

      <div className="planner-top-row">
        {/* Warehouse & parcels --> to be discussed with Johnson for changes */}
        <section className="planner-card planner-parcels-card">
          <header className="planner-card-header">
            <div>
              <h2>Warehouse Parcels</h2>
              <p>View parcels in a specific warehouse (backend data)</p>
            </div>
            <div className="planner-warehouse-input">
              <label>
                Warehouse ID:
                <input
                  type="number"
                  value={warehouseId}
                  onChange={handleWarehouseChange}
                  min={1}
                />
              </label>
            </div>
          </header>

          {loadingParcels ? (
            <p>Loading parcels…</p>
          ) : (
            <>
              <table className="planner-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Status</th>
                    <th>Weight</th>
                    <th>Volume</th>
                    <th>City</th>
                    <th>Address</th>
                    <th>Recipient</th>
                  </tr>
                </thead>
                <tbody>
                  {parcels.map((p) => (
                    <tr key={p.parcelId}>
                      <td>{p.parcelId}</td>
                      <td>{p.name}</td>
                      <td>{p.status}</td>
                      <td>{p.weight}</td>
                      <td>{p.volume}</td>
                      <td>{p.deliveryCity}</td>
                      <td>{p.deliveryAddress}</td>
                      <td>{p.recipientName}</td>
                    </tr>
                  ))}
                  {parcels.length === 0 && (
                    <tr>
                      <td colSpan={8} className="planner-empty">
                        No parcels found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>

              <div className="planner-pagination">
                <span>
                  Page {parcelPageInfo.currentPage + 1} of{' '}
                  {parcelPageInfo.totalPages || 1} — {parcelPageInfo.totalItems} items
                </span>
                <div>
                  <button
                    onClick={() => setPage((p) => Math.max(p - 1, 0))}
                    disabled={page === 0}
                  >
                    ‹ Prev
                  </button>
                  <button
                    onClick={() =>
                      setPage((p) =>
                        p + 1 < parcelPageInfo.totalPages ? p + 1 : p
                      )
                    }
                    disabled={page + 1 >= parcelPageInfo.totalPages}
                  >
                    Next ›
                  </button>
                </div>
              </div>
            </>
          )}
        </section>

        {/* Available Drivers (Not available with time --> ask Johnson to update) */}
        <section className="planner-card planner-drivers-card">
          <header className="planner-card-header">
            <div>
              <h2>Available Drivers</h2>
              <p>Data from /planner/drivers/available</p>
            </div>
          </header>

          {loadingDrivers ? (
            <p>Loading drivers…</p>
          ) : (
            <table className="planner-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>City</th>
                </tr>
              </thead>
              <tbody>
                {drivers.map((d) => (
                  <tr key={d.id}>
                    <td>{d.id}</td>
                    <td>{d.userName}</td>
                    <td>{d.email}</td>
                    <td>{d.city}</td>
                  </tr>
                ))}
                {drivers.length === 0 && (
                  <tr>
                    <td colSpan={4} className="planner-empty">
                      No drivers available
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </section>
      </div>

      {/* Routes by truck --> implement modification Adi */}
      <section className="planner-card planner-routes-card">
        <header className="planner-card-header">
          <div>
            <h2>Routes by Truck</h2>
            <p>Displays routes from /planner/routes/truck/&lt;id&gt;</p>
          </div>
          <div className="planner-truck-input">
            <label>
              Truck ID:
              <input
                type="number"
                value={truckId}
                onChange={(e) => setTruckId(Number(e.target.value))}
                min={1}
              />
            </label>
            <button onClick={handleLoadRoutes} disabled={loadingRoutes}>
              {loadingRoutes ? 'Loading…' : 'Load Routes'}
            </button>
          </div>
        </header>

        {routes.length === 0 && !loadingRoutes && (
          <p className="planner-empty">No routes loaded yet.</p>
        )}

        {routes.map((route) => (
          <div key={route.routeId} className="planner-route-card">
            <div className="planner-route-header">
              <h3>
                Route #{route.routeId} – Truck {route.truckPlateNumber} – Driver{' '}
                {route.driverUserName}
              </h3>
              <span className={`planner-route-status planner-route-status-${route.status.toLowerCase()}`}>
                {route.status}
              </span>
            </div>
            <p>
              Distance: {route.totalDistance} km · Time: {route.totalTransportTime} min ·
              Fuel: €{route.estimatedFuelCost} · Start: {route.startTime}
            </p>
            <p>{route.note}</p>

            <table className="planner-table planner-stops-table">
              <thead>
                <tr>
                  <th>Priority</th>
                  <th>Type</th>
                  <th># Parcels</th>
                  <th>Example Customer / Address</th>
                </tr>
              </thead>
              <tbody>
                {route.routeStops
                  .slice()
                  .sort((a, b) => a.priority - b.priority)
                  .map((stop) => (
                    <tr key={stop.stopId}>
                      <td>{stop.priority}</td>
                      <td>{stop.stopType}</td>
                      <td>{stop.parcelsToDeliver.length}</td>
                      <td>{formatStopSummary(stop)}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        ))}
      </section>
    </div>
  );
};

export default PlannerDashboard;
