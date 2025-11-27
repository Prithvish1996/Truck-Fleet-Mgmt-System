import React from 'react';
import homeIcon from '../../assets/home icon.png';
import scheduleIcon from '../../assets/schedule icon.png';
import taskIcon from '../../assets/task icon.png';
import routeIcon from '../../assets/route icon.png';

interface DashboardSidebarProps {
  activeView: 'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking' | 'truck-detail' | 'route-map';
  onViewChange: (view: 'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking') => void;
  hasValidRoutes?: boolean;
}

export default function DashboardSidebar({ activeView, onViewChange, hasValidRoutes = false }: DashboardSidebarProps) {
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
        <button
          type="button"
          className={`nav-item ${activeView === 'route-assignment' ? 'active' : ''} ${!hasValidRoutes ? 'disabled' : ''}`}
          onClick={() => onViewChange('route-assignment')}
          disabled={!hasValidRoutes}
          title={!hasValidRoutes ? 'No routes available for assignment. Please generate routes first.' : ''}
        >
          <img src={taskIcon} alt="" aria-hidden className="nav-icon" />
          <span className="nav-label">Assignment</span>
        </button>
        <button
          type="button"
          className={`nav-item ${activeView === 'route-tracking' ? 'active' : ''}`}
          onClick={() => onViewChange('route-tracking')}
        >
          <img src={routeIcon} alt="" aria-hidden className="nav-icon" />
          <span className="nav-label">Tracking</span>
        </button>
      </nav>
    </aside>
  );
}

