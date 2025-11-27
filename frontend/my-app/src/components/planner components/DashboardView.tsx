import React from 'react';
import SummaryCards from './SummaryCards';
import NewRequestsPanel from './NewRequestsPanel';
import AvailableDriversPanel from './AvailableDriversPanel';
import StatusMonitoringPanel from './StatusMonitoringPanel';
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
  return (
    <>
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

