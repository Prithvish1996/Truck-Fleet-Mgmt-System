import React from 'react';
import RouteAssignmentPage from '../../pages/Planner/RouteAssignmentPage/RouteAssignmentPage';
import RouteTrackingPage from '../../pages/Planner/RouteTrackingPage/RouteTrackingPage';
import TruckDetailPage from '../../pages/Planner/TruckDetailPage/TruckDetailPage';
import RouteMapModal from '../../pages/Planner/RouteMapModal/RouteMapModal';
import SchedulePage from './SchedulePage';
import DashboardView from './DashboardView';
import { RouteAssignment } from '../../types';
import { DriverResponse } from '../../services/plannerService';

type ScheduleParcel = {
  id: string;
  parcelId: number;
  receiver: string;
  location: string;
  warehouse: string;
  status: 'Pending' | 'Scheduled';
  selectable: boolean;
  weight?: number;
  volume?: number;
  phone?: string;
  deliveryInstructions?: string;
  createdAt?: string;
};

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

interface ViewRouterProps {
  activeView: 'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking' | 'truck-detail' | 'route-map';
  selectedParcelIds: string[];
  newRequests: DashboardRequest[];
  availableDrivers: DriverResponse[];
  statusMonitoring: StatusMonitoringItem[];
  summaryCards: SummaryCard[];
  warehouses: any[];
  selectedWarehouseId: number | null;
  scheduleParcels: ScheduleParcel[];
  filteredAndSortedParcels: ScheduleParcel[];
  selectedScheduleParcels: string[];
  searchText: string;
  filterStatus: 'All' | 'Pending' | 'Scheduled';
  sortBy: 'id' | 'receiver' | 'location' | 'warehouse';
  sortOrder: 'asc' | 'desc';
  loading: boolean;
  scheduleError: string;
  submittedAssignments: RouteAssignment[];
  selectedTruckPlateNo: string;
  selectedRouteAssignment: RouteAssignment | null;
  isOptimizing: boolean;
  onGenerateRouteClick: () => void;
  onScheduleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  onParcelToggle: (parcelId: string) => void;
  onSelectAll: (selected: boolean) => void;
  onSearchChange: (value: string) => void;
  onFilterStatusChange: (value: 'All' | 'Pending' | 'Scheduled') => void;
  onSortByChange: (value: 'id' | 'receiver' | 'location' | 'warehouse') => void;
  onSortOrderToggle: () => void;
  onWarehouseChange: (id: number) => void;
  onSetActiveView: (view: 'dashboard' | 'schedule' | 'route-assignment' | 'route-tracking' | 'truck-detail' | 'route-map') => void;
  onSubmitAssignments: (assignments: RouteAssignment[]) => void;
  onTruckClick: (truckPlateNo: string) => void;
  onTrackRoute: (assignment: RouteAssignment) => void;
  onReturnFromTruckDetail: () => void;
  onParcelClick: (parcelId: string) => void;
  onReturnFromRouteMap: () => void;
}

export default function ViewRouter({
  activeView,
  selectedParcelIds,
  newRequests,
  availableDrivers,
  statusMonitoring,
  summaryCards,
  warehouses,
  selectedWarehouseId,
  scheduleParcels,
  filteredAndSortedParcels,
  selectedScheduleParcels,
  searchText,
  filterStatus,
  sortBy,
  sortOrder,
  loading,
  scheduleError,
  submittedAssignments,
  selectedTruckPlateNo,
  selectedRouteAssignment,
  isOptimizing,
  onGenerateRouteClick,
  onScheduleSubmit,
  onParcelToggle,
  onSelectAll,
  onSearchChange,
  onFilterStatusChange,
  onSortByChange,
  onSortOrderToggle,
  onWarehouseChange,
  onSetActiveView,
  onSubmitAssignments,
  onTruckClick,
  onTrackRoute,
  onReturnFromTruckDetail,
  onParcelClick,
  onReturnFromRouteMap
}: ViewRouterProps) {
  return (
    <>
      {activeView === 'dashboard' ? (
        <DashboardView
          summaryCards={summaryCards}
          newRequests={newRequests}
          availableDrivers={availableDrivers}
          statusMonitoring={statusMonitoring}
          onGenerateRouteClick={onGenerateRouteClick}
          isGenerating={isOptimizing}
        />
      ) : activeView === 'schedule' ? (
        <SchedulePage
          warehouses={warehouses}
          selectedWarehouseId={selectedWarehouseId}
          scheduleParcels={scheduleParcels}
          filteredAndSortedParcels={filteredAndSortedParcels}
          selectedScheduleParcels={selectedScheduleParcels}
          searchText={searchText}
          filterStatus={filterStatus}
          sortBy={sortBy}
          sortOrder={sortOrder}
          loading={loading}
          scheduleError={scheduleError}
          onScheduleSubmit={onScheduleSubmit}
          onParcelToggle={onParcelToggle}
          onSelectAll={onSelectAll}
          onSearchChange={onSearchChange}
          onFilterStatusChange={onFilterStatusChange}
          onSortByChange={onSortByChange}
          onSortOrderToggle={onSortOrderToggle}
          onWarehouseChange={onWarehouseChange}
        />
      ) : activeView === 'route-assignment' && selectedParcelIds.length > 0 ? (
        <RouteAssignmentPage
          selectedParcelIds={selectedParcelIds}
          onReturn={() => onSetActiveView('dashboard')}
          onSubmit={onSubmitAssignments}
          onTruckClick={onTruckClick}
          submittedAssignments={submittedAssignments}
        />
      ) : activeView === 'route-tracking' ? (
        <RouteTrackingPage
          assignments={submittedAssignments}
          onReturn={() => onSetActiveView('dashboard')}
          onTrack={onTrackRoute}
          onTruckClick={onTruckClick}
        />
      ) : activeView === 'truck-detail' ? (
        <TruckDetailPage
          truckPlateNo={selectedTruckPlateNo}
          onReturn={onReturnFromTruckDetail}
          onParcelClick={onParcelClick}
        />
      ) : activeView === 'route-map' ? (
        <RouteMapModal
          assignment={selectedRouteAssignment}
          onReturn={onReturnFromRouteMap}
        />
      ) : null}
    </>
  );
}

