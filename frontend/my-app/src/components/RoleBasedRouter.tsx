import React from 'react';
import { Navigate, Routes, Route, useLocation } from 'react-router-dom';
import { authService } from '../services/authService';
import AdminDashboard from '../pages/Admin/AdminDashboard';
import DriverDashboard from '../pages/Driver/DriverDashboard/DriverDashboard';
import RouteOverview from '../pages/Driver/routeOverview/RouteOverview';
import Navigation from '../pages/Driver/Navigation/Navigation';
import PlannerDashboard from '../pages/Planner/PlannerDashboard';

export const RoleBasedRouter: React.FC = () => {
  const userRole = authService.getUserRole();
  const location = useLocation();

  console.log('RoleBasedRouter - userRole:', userRole, 'location:', location.pathname);

  switch (userRole) {
    case 'ADMIN':
      return <AdminDashboard />;
    case 'DRIVER':
      if (location.pathname === '/driver/route-overview') {
        return <RouteOverview />;
      }
      if (location.pathname === '/driver/navigation') {
        return <Navigation navigate={(path: string) => window.location.href = path} />;
      }
      return <DriverDashboard />;
    case 'PLANNER':
      return <PlannerDashboard />;
    default:
      return <Navigate to="/" replace />;
  }
};
