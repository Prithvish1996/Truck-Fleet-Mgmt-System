import React from 'react';
import { authService } from '../services/authService';
import './TopBar.css';

const TopBar: React.FC = () => {
  const email = authService.getUserEmail();

  const handleLogout = () => {
    authService.removeToken();
    window.location.href = '/';
  };

  return (
    <div className="topbar">
      <div className="topbar-left">
        <h1>EcoFlow Planner Dashboard</h1>
        <span className="topbar-subtitle">
          Plan sustainable routes, assign drivers, and monitor warehouse parcels.
        </span>
      </div>

      <div className="topbar-user">
        <span>{email ?? 'Planner'}</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
};

export default TopBar;
