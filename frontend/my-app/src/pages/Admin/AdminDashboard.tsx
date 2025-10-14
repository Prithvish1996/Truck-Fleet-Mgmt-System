import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [expandedSections, setExpandedSections] = useState<{ [key: string]: boolean }>({});
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const toggleSection = (section: string) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      const token = authService.getToken();
      if (token) {
        await authService.logout(token);
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      authService.removeToken();
      navigate('/');
    }
  };

  useEffect(() => {
    // Check if user is authenticated and is admin
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'ADMIN') {
      navigate('/');
    }
  }, [navigate]);

  return (
    <div className="admin-dashboard">
      <header className="dashboard-header">
        <h1 className="dashboard-title">🔧 Admin Dashboard</h1>
        <button 
          className="logout-button" 
          onClick={handleLogout}
          disabled={isLoggingOut}
        >
          {isLoggingOut ? 'Logging out...' : 'Logout'}
        </button>
      </header>
      
      <div className="dashboard-content">
        <p className="dashboard-description">
          Welcome to the Admin Dashboard. Manage users, system settings, and monitor the fleet.
        </p>
        
        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('users')}
          >
            👥 User Management
          </button>
          {expandedSections.users && (
            <div className="collapsible-content">
              <p>Create, update, and delete user accounts.</p>
              <p>Manage user roles and permissions.</p>
              <p>View user activity and login history.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('fleet')}
          >
            🚛 Fleet Management
          </button>
          {expandedSections.fleet && (
            <div className="collapsible-content">
              <p>Manage truck fleet and vehicle assignments.</p>
              <p>Monitor vehicle status and maintenance schedules.</p>
              <p>Track fuel consumption and costs.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('analytics')}
          >
            📊 System Analytics
          </button>
          {expandedSections.analytics && (
            <div className="collapsible-content">
              <p>View comprehensive system reports and metrics.</p>
              <p>Monitor delivery performance and efficiency.</p>
              <p>Generate custom reports and exports.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('settings')}
          >
            ⚙️ System Settings
          </button>
          {expandedSections.settings && (
            <div className="collapsible-content">
              <p>Configure system-wide settings and preferences.</p>
              <p>Manage API keys and integrations.</p>
              <p>Set up notifications and alerts.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
