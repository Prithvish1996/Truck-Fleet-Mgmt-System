import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from './services/authService';
import './Explore.css';

export default function Explore() {
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
    if (!authService.isAuthenticated()) {
      navigate('/');
    }
  }, [navigate]);

  return (
    <div className="explore-container">
      <header className="explore-header">
        <h1 className="explore-title">Driver GO Dashboard</h1>
        <button 
          className="logout-button" 
          onClick={handleLogout}
          disabled={isLoggingOut}
        >
          {isLoggingOut ? 'Logging out...' : 'Logout'}
        </button>
      </header>
      
      <div className="explore-content">
        <p className="explore-description">
          Welcome to your driver dashboard. Here you can manage your deliveries and track your progress.
        </p>
        
        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('deliveries')}
          >
            📦 My Deliveries
          </button>
          {expandedSections.deliveries && (
            <div className="collapsible-content">
              <p>View and manage your current delivery assignments.</p>
              <p>Track delivery status and update progress.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('routes')}
          >
            🗺️ Route Optimization
          </button>
          {expandedSections.routes && (
            <div className="collapsible-content">
              <p>Get optimized routes for your deliveries.</p>
              <p>Save time and fuel with smart routing.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('analytics')}
          >
            📊 Analytics
          </button>
          {expandedSections.analytics && (
            <div className="collapsible-content">
              <p>View your delivery performance metrics.</p>
              <p>Track earnings and efficiency over time.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('settings')}
          >
            ⚙️ Settings
          </button>
          {expandedSections.settings && (
            <div className="collapsible-content">
              <p>Manage your account preferences.</p>
              <p>Update notification settings and profile information.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

