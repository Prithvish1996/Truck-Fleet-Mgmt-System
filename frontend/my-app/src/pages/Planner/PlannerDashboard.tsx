import React, { useState } from "react";
import "./PlannerDashboard.css";

import Sidebar from "../../components/Sidebar";
import TopBar from "../../components/TopBar";

import ParcelsSection from "./sections/ParcelsSection";
import DriversSection from "./sections/DriversSection";
import RoutesSection from "./sections/RoutesSection";
import WarehousesSection from "./sections/WarehousesSection";
import OverviewSection from "./sections/OverviewSection";

const PlannerDashboard: React.FC = () => {
  const [activeSection, setActiveSection] = useState("dashboard");

  const renderSection = () => {
    switch (activeSection) {
      case "parcels":
        return <ParcelsSection />;
      case "drivers":
        return <DriversSection />;
      case "routes":
        return <RoutesSection />;
      case "warehouses":
        return <WarehousesSection />;
      default:
        return <OverviewSection />;
    }
  };

  return (
    <div className="planner-layout">
      <Sidebar active={activeSection} onChange={setActiveSection} />

      <div className="planner-main">
        <TopBar title="EcoFlow Planner Dashboard" />
        <div className="planner-content">{renderSection()}</div>
      </div>
    </div>
  );
};

export default PlannerDashboard;
