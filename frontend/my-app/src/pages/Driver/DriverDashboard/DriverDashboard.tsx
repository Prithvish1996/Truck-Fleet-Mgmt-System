import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './DriverDashboard.css';
import DriverHeader from '../components/driverHeader';
import RoutesList from '../components/RoutesList';
import BottomTabBar from '../components/BottomTabBar/BottomTabBar';
import AgendaPlanner from '../AgendaPlanner/AgendaPlanner';
import Suggestions from '../Suggestions/Suggestions';
import { useDriverRoutes } from '../hooks';

export default function DriverDashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'home' | 'agenda' | 'suggestions'>('home');
  const [showOtherRoutes, setShowOtherRoutes] = useState(false);
  const { routes, loading, startRoute } = useDriverRoutes();

  return (
    <div className="driver-dashboard">
      <DriverHeader navigate={navigate} />

      <div className="dashboard-content">
        {activeTab === 'home' ? (
          <>
            {loading ? (
              <div className="loading-message">Loading routes...</div>
            ) : routes.length === 0 ? (
              <div className="no-routes-message">No routes assigned to you.</div>
            ) : (
              <RoutesList
                routes={routes}
                startRoute={startRoute}
                showOtherRoutes={showOtherRoutes}
                onToggleOtherRoutes={() => setShowOtherRoutes(!showOtherRoutes)}
              />
            )}
          </>
        ) : activeTab === 'agenda' ? (
          <AgendaPlanner />
        ) : (
          <Suggestions />
        )}
      </div>

      <BottomTabBar activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
}
