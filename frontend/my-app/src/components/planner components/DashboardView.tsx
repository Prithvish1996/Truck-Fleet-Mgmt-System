import React, { useState, useEffect } from 'react';
import SummaryCards from './SummaryCards';
import NewRequestsPanel from './NewRequestsPanel';
import AvailableDriversPanel from './AvailableDriversPanel';
import StatusMonitoringPanel from './StatusMonitoringPanel';
import WorkflowIndicator from './WorkflowIndicator';
import OnboardingTour from './OnboardingTour';
import { DriverResponse } from '../../services/plannerService';

type DashboardRequest = {
  truckPlateId: string;
  deliveryDate: string;
  parcels: number;
  warehouse: string;
  parcelIds: number[];
  warehouseId: number;
};

type SummaryCard = {
  title: string;
  value: string;
  delta: string;
  trend: 'up' | 'down' | 'warning';
};

type StatusMonitoringItem = {
  driver: string;
  status: string;
  route: string;
};

interface DashboardViewProps {
  summaryCards: SummaryCard[];
  newRequests: DashboardRequest[];
  availableDrivers: DriverResponse[];
  statusMonitoring: StatusMonitoringItem[];
  onGenerateRouteClick: () => void;
  isGenerating: boolean;
}

export default function DashboardView({
  summaryCards,
  newRequests,
  availableDrivers,
  statusMonitoring,
  onGenerateRouteClick,
  isGenerating
}: DashboardViewProps) {
  const [showOnboarding, setShowOnboarding] = useState(false);

  useEffect(() => {
    const hasSeenOnboarding = localStorage.getItem('planner-onboarding-completed');
    if (!hasSeenOnboarding) {
      const timer = setTimeout(() => {
        const summaryCardsExist = document.querySelector('.summary-cards');
        const newRequestsExist = document.querySelector('.new-requests');
        
        if (summaryCardsExist && newRequestsExist) {
          setShowOnboarding(true);
        }
      }, 1000);
      
      return () => clearTimeout(timer);
    }
  }, []);

  const handleOnboardingComplete = () => {
    localStorage.setItem('planner-onboarding-completed', 'true');
    setShowOnboarding(false);
  };

  const handleOnboardingSkip = () => {
    localStorage.setItem('planner-onboarding-completed', 'true');
    setShowOnboarding(false);
  };

  const onboardingSteps = [
    {
      target: '.summary-cards',
      title: 'Welcome to Planner Dashboard',
      content: 'Here you can see key metrics: Today\'s Requests, Available Drivers, Active Processes, and Exceptions. This gives you a quick overview of your delivery operations.',
      position: 'bottom' as const,
    },
    {
      target: '.new-requests',
      title: 'New Requests',
      content: 'Scheduled parcels appear here, grouped by warehouse and delivery date. These are ready for route generation. Click "Generate Route" to create optimized delivery routes.',
      position: 'left' as const,
      action: newRequests.length > 0 ? {
        label: 'View Requests',
        onClick: () => {
          document.querySelector('.new-requests')?.scrollIntoView({ behavior: 'smooth' });
        }
      } : undefined,
    },
    {
      target: '.dashboard-sidebar',
      title: 'Navigation',
      content: 'Use the sidebar to navigate: Schedule parcels, generate routes, assign drivers, and track deliveries. The workflow flows from left to right.',
      position: 'right' as const,
    },
    {
      target: '.status-monitoring',
      title: 'Status Monitoring',
      content: 'Monitor active routes and driver statuses in real-time. Track which drivers are on the road and the status of their deliveries.',
      position: 'top' as const,
    },
  ];

  const workflowSteps = [
    { label: 'Schedule', icon: '1', completed: false, active: newRequests.length === 0 },
    { label: 'Generate Routes', icon: '2', completed: false, active: newRequests.length > 0 },
    { label: 'Edit Stops', icon: '3', completed: false, active: false },
    { label: 'Assign Drivers', icon: '4', completed: false, active: false },
    { label: 'Track', icon: '5', completed: false, active: false },
  ];

  return (
    <>
      {showOnboarding && (
        <OnboardingTour
          steps={onboardingSteps}
          onComplete={handleOnboardingComplete}
          onSkip={handleOnboardingSkip}
        />
      )}

      <WorkflowIndicator steps={workflowSteps} />

      <SummaryCards cards={summaryCards} />

      <section className="dashboard-grid">
        <NewRequestsPanel 
          requests={newRequests} 
          onGenerateRouteClick={onGenerateRouteClick}
          isGenerating={isGenerating}
        />
        <AvailableDriversPanel drivers={availableDrivers} />
      </section>

      <section className="status-monitoring-section">
        <StatusMonitoringPanel statusData={statusMonitoring} />
      </section>
    </>
  );
}

