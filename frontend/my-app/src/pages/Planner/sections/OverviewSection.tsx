import React from "react";
import "../PlannerDashboard.css";


const OverviewSection: React.FC = () => {
  return (
    <div>
      <div className="eco-card">
        <h2>Welcome Planner</h2>
        <p className="subtext">Use the sidebar to manage parcels, drivers, routes and warehouses.</p>
      </div>
    </div>
  );
};

export default OverviewSection;
