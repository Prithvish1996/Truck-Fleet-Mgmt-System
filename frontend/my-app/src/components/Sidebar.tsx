import React from 'react';
import logo from '../assets/logo.png';
import './Sidebar.css';

export type PlannerSection = 'overview' | 'parcels' | 'drivers' | 'routes';

interface SidebarProps {
  activeSection: PlannerSection;
  onSectionChange: (section: PlannerSection) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ activeSection, onSectionChange }) => {
  const itemClass = (section: PlannerSection) =>
    `sidebar-item ${activeSection === section ? 'active' : ''}`;

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <img src={logo} alt="EcoFlow Logo" className="sidebar-logo" />
        <div className="sidebar-brand">
          <span className="sidebar-brand-title">EcoFlow</span>
          <span className="sidebar-brand-subtitle">Planner</span>
        </div>
      </div>

      <nav className="sidebar-menu">
        <div className="sidebar-section-label">Dashboard</div>
        <button
          type="button"
          className={itemClass('overview')}
          onClick={() => onSectionChange('overview')}
        >
          Overview
        </button>

        <div className="sidebar-section-label">Planning</div>
        <button
          type="button"
          className={itemClass('parcels')}
          onClick={() => onSectionChange('parcels')}
        >
          Warehouse Parcels
        </button>
        <button
          type="button"
          className={itemClass('drivers')}
          onClick={() => onSectionChange('drivers')}
        >
          Available Drivers
        </button>
        <button
          type="button"
          className={itemClass('routes')}
          onClick={() => onSectionChange('routes')}
        >
          Routes by Truck
        </button>
      </nav>
    </div>
  );
};

export default Sidebar;
