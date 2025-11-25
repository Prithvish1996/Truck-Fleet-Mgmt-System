import React from 'react';
import homeIcon from '../../../assets/home icon.png';
import scheduleIcon from '../../../assets/schedule icon.png';

interface DashboardSidebarProps {
  activeView: 'dashboard' | 'schedule';
  onViewChange: (view: 'dashboard' | 'schedule') => void;
}

export default function DashboardSidebar({ activeView, onViewChange }: DashboardSidebarProps) {
  return (
    <aside className="dashboard-sidebar">
      <nav className="sidebar-nav">
        <button
          type="button"
          className={`nav-item ${activeView === 'dashboard' ? 'active' : ''}`}
          onClick={() => onViewChange('dashboard')}
        >
          <img src={homeIcon} alt="" aria-hidden className="nav-icon" />
          <span className="nav-label">Planner Dashboard</span>
        </button>
        <button
          type="button"
          className={`nav-item schedule ${activeView === 'schedule' ? 'active' : ''}`}
          onClick={() => onViewChange('schedule')}
        >
          <img src={scheduleIcon} alt="" aria-hidden className="nav-icon" />
          <span className="nav-label">Schedule</span>
        </button>
      </nav>
    </aside>
  );
}

