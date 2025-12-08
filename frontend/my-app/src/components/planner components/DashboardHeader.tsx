import React from 'react';
import smallLogo from '../../assets/small logo.png';

interface DashboardHeaderProps {
  isLoggingOut: boolean;
  onLogout: () => void;
}

export default function DashboardHeader({ isLoggingOut, onLogout }: DashboardHeaderProps) {
  return (
    <header className="top-bar">
      <div className="top-bar-brand">
        <img src={smallLogo} alt="Driver GO" className="small-logo" />
      </div>
      <div className="top-bar-controls">
        <div className="search-input">
          <span className="search-icon" aria-hidden />
          <input type="search" placeholder="Search..." aria-label="Search dashboard" />
        </div>
        <button className="language-switch" type="button" aria-label="Change language">
          <span className="globe-icon" aria-hidden />
          EN
          <span className="chevron" aria-hidden />
        </button>
        <div className="user-pill">
          <div className="user-avatar" aria-hidden />
          <span>Planner 1</span>
        </div>
        <button
          className="logout-button"
          type="button"
          onClick={onLogout}
          disabled={isLoggingOut}
        >
          {isLoggingOut ? 'Logging out...' : 'Logout'}
        </button>
      </div>
    </header>
  );
}

