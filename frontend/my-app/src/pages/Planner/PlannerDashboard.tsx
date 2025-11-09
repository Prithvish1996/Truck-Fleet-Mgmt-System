import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import './PlannerDashboard.css';

export default function PlannerDashboard() {
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
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'PLANNER') {
      navigate('/');
    }
  }, [navigate]);

  return (
    <div className="planner-dashboard">
      <header className="dashboard-header">
        <h1 className="dashboard-title">📋 Planner Dashboard</h1>
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
          Welcome to the Planner Dashboard. Optimize routes, schedule deliveries, and coordinate the fleet.
        </p>
        
        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('routes')}
          >
            🗺️ Route Planning
          </button>
          {expandedSections.routes && (
            <div className="collapsible-content">
              <p>Plan and optimize delivery routes for maximum efficiency.</p>
              <p>Consider traffic patterns, delivery windows, and vehicle capacity.</p>
              <p>Assign routes to drivers and track progress.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('schedule')}
          >
            📅 Delivery Scheduling
          </button>
          {expandedSections.schedule && (
            <div className="collapsible-content">
              <p>Schedule deliveries and manage time slots.</p>
              <p>Coordinate with customers for delivery windows.</p>
              <p>Handle rescheduling and priority deliveries.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('fleet')}
          >
            🚛 Fleet Coordination
          </button>
          {expandedSections.fleet && (
            <div className="collapsible-content">
              <p>Monitor fleet status and vehicle availability.</p>
              <p>Assign vehicles to drivers and routes.</p>
              <p>Track maintenance schedules and vehicle capacity.</p>
            </div>
          )}
        </div>

        <div className="collapsible-section">
          <button 
            className="collapsible-header"
            onClick={() => toggleSection('analytics')}
          >
            📊 Planning Analytics
          </button>
          {expandedSections.analytics && (
            <div className="collapsible-content">
              <p>Analyze delivery patterns and optimization opportunities.</p>
              <p>Generate reports on route efficiency and performance.</p>
              <p>Identify areas for improvement in planning processes.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}