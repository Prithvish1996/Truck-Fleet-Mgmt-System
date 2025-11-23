import React, { useEffect, useState } from "react";
import { plannerDriverService } from "../../../services/plannerDriverService";
import "../PlannerDashboard.css";
import { PlannerDriver } from "../../../services/plannerDriverService";

const DriversSection = () => {
  const [drivers, setDrivers] = useState<PlannerDriver[]>([]);

  const loadDrivers = async () => {
    const result = await plannerDriverService.getAvailableDrivers();
    setDrivers(result);
  };

  useEffect(() => {
    loadDrivers();
  }, []);

  return (
    <div className="eco-card">
      <h2>Available Drivers</h2>
      <p className="subtext">Drivers available for assignment</p>

      <table className="eco-table">
        <thead>
          <tr>
            <th>ID</th><th>Username</th><th>Email</th><th>City</th>
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
        </tbody>
      </table>
    </div>
  );
};

export default DriversSection;
