import React, { useState } from "react";
import { plannerRouteService } from "../../../services/plannerRouteService";
import "../PlannerDashboard.css";
import { RouteData } from "../../../types";

const RoutesSection = () => {
  const [truckId, setTruckId] = useState(1);
const [routes, setRoutes] = useState<RouteData[]>([]);

  const loadRoutes = async () => {
    const res = await plannerRouteService.getRoutesByTruckId(truckId);
    setRoutes(res);
  };

  return (
    <div className="eco-card">
      <h2>Routes by Truck</h2>
      <p className="subtext">Fetch & display truck route details</p>

      <label>
        Truck ID:
        <input
          type="number"
          value={truckId}
          onChange={(e) => setTruckId(Number(e.target.value))}
          style={{ marginLeft: "10px" }}
        />
      </label>

      <button className="eco-button" onClick={loadRoutes} style={{ marginLeft: "10px" }}>
        Load Routes
      </button>

      {routes.map((route) => (
        <div key={route.routeId} className="eco-card" style={{ marginTop: "20px" }}>
          <h3>
            Route #{route.routeId} • Truck {route.truckPlateNumber} • Driver {route.driverUserName}
          </h3>

          <p className="subtext">
            Distance: {route.totalDistance} km — Time: {route.totalTransportTime} min  
          </p>
        </div>
      ))}
    </div>
  );
};

export default RoutesSection;
