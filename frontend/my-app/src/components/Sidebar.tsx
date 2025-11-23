import React from "react";
import "./Sidebar.css";
import logo from "../assets/logo.png";

interface SidebarProps {
  active: string;
  onChange: (section: string) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ active, onChange }) => {
  return (
    <aside className="sidebar">

      <div className="sidebar-logo-wrapper">
        <div className="sidebar-logo-bg">
          <img src={logo} className="sidebar-logo" alt="EcoFlow Logo" />
        </div>
      </div>

      <nav className="sidebar-menu">
        <div
          className={`sidebar-item ${active === "dashboard" ? "active" : ""}`}
          onClick={() => onChange("dashboard")}
        >
          Dashboard Overview
        </div>

        <div
          className={`sidebar-item ${active === "parcels" ? "active" : ""}`}
          onClick={() => onChange("parcels")}
        >
          Warehouse Parcels
        </div>

        <div
          className={`sidebar-item ${active === "drivers" ? "active" : ""}`}
          onClick={() => onChange("drivers")}
        >
          Available Drivers
        </div>

        <div
          className={`sidebar-item ${active === "routes" ? "active" : ""}`}
          onClick={() => onChange("routes")}
        >
          Routes by Truck
        </div>

        <div
          className={`sidebar-item ${active === "warehouses" ? "active" : ""}`}
          onClick={() => onChange("warehouses")}
        >
          Warehouses
        </div>
      </nav>
    </aside>
  );
};

export default Sidebar;
