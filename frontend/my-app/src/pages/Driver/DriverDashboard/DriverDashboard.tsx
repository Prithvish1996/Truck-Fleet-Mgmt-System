import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../../services/authService';
import { routeService } from '../../../services/routeService';
import { Route } from '../../../types';
import './DriverDashboard.css';
import DriverHeader from '../components/driverHeader';
import RouteCard from '../components/RouteCard';
import FeedbackBox from '../components/feedbackBox';
import BottomTabBar from '../components/BottomTabBar/BottomTabBar';
import AgendaPlanner from '../AgendaPlanner/AgendaPlanner';

export default function DriverDashboard() {
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState('');
  const [routes, setRoutes] = useState<Route[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'home' | 'agenda'>('home');

  const handleFeedbackSubmit = () => {
    if (feedback.trim()) {
      console.log('Feedback submitted:', feedback);
      setFeedback('');
    }
  };

  const startRoute = async (routeId: string) => {
    try {
      await routeService.startRoute(routeId);
      sessionStorage.removeItem('currentRouteId');
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

    } catch (error) {
      console.error('Error loading routes:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authService.isAuthenticated() || authService.getUserRole() !== 'DRIVER') {
      navigate('/');
    } else {
      loadRoutes();
    }
  }, [navigate]);

  return (
    <div className="driver-dashboard">
      <DriverHeader navigate={navigate} />

      <div className="dashboard-content">
        {activeTab === 'home' ? (
          <>
            <FeedbackBox feedback={feedback} setFeedback={setFeedback} handleFeedbackSubmit={handleFeedbackSubmit} />

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
          </>
        ) : (
          <AgendaPlanner />
        )}
      </div>

      <BottomTabBar activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
}
