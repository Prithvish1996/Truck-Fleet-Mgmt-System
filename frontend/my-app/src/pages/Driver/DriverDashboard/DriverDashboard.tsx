import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../../services/authService';
import { routeService } from '../../../services/routeService';
import { Route } from '../../../types';
import './DriverDashboard.css';
import DriverHeader from '../components/driverHeader';
import RouteCard from '../components/RouteCard';
import FeedbackBox from '../components/feedbackBox';

export default function DriverDashboard() {
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState('');
  const [routes, setRoutes] = useState<Route[]>([]);
  const [loading, setLoading] = useState(true);

  const handleFeedbackSubmit = () => {
    if (feedback.trim()) {
      console.log('Feedback submitted:', feedback);
      setFeedback('');
      // Here you would typically send the feedback to the backend
    }
  };

  const startRoute = async (routeId: string) => {
    try {
      await routeService.startRoute(routeId);
      // Navigate to route overview page
      navigate('/driver/route-overview');
    } catch (error) {
      console.error('Error starting route:', error);
    }
  };

  const loadRoutes = async () => {
    try {
      setLoading(true);
      const driverRoutes = await routeService.getDriverRoutes();
      setRoutes(driverRoutes);
      console.log('Loaded routes:', driverRoutes);
    } catch (error) {
      console.error('Error loading routes:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Check if user is authenticated and is driver
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'DRIVER') {
      navigate('/');
    } else {
      loadRoutes();
    }
  }, [navigate]);

  return (
    <div className="driver-dashboard">
      {/* Header */}
     <DriverHeader navigate={navigate} />

      {/* Main Content */}
      <div className="dashboard-content">

        {/* Feedback Box */}
        <FeedbackBox feedback={feedback} setFeedback={setFeedback} handleFeedbackSubmit={handleFeedbackSubmit} />

        {/* Route Cards */}
        {loading ? (
          <div className="loading-message">Loading routes...</div>
        ) : routes.length === 0 ? (
          <div className="no-routes-message">No routes assigned to you.</div>
        ) : (
          routes.map((route) => (
            <RouteCard
              key={route.id}
              startRoute={startRoute}
              routeId={route.id}
              truckId={route.truckId}
              packages={route.packages.length}
              startTime={route.startTime}
              duration={route.duration}
              date={route.date}
              status={route.status}
            />
          ))
        )}

      </div>
    </div>
  );
}
