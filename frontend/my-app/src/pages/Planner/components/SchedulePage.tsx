import React from 'react';
import ScheduleControls from './ScheduleControls';
import ScheduleTable from './ScheduleTable';
import ScheduleForm from './ScheduleForm';

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

interface SchedulePageProps {
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
  onScheduleSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  onParcelToggle: (parcelId: string) => void;
  onSelectAll?: (selected: boolean) => void;
  onSearchChange: (value: string) => void;
  onFilterStatusChange: (value: 'All' | 'Pending' | 'Scheduled') => void;
  onSortByChange: (value: 'id' | 'receiver' | 'location' | 'warehouse') => void;
  onSortOrderToggle: () => void;
  onWarehouseChange: (warehouseId: number) => void;
}

export default function SchedulePage({
  warehouses,
  selectedWarehouseId,
  filteredAndSortedParcels,
  selectedScheduleParcels,
  searchText,
  filterStatus,
  sortBy,
  sortOrder,
  loading,
  scheduleError,
  onScheduleSubmit,
  onParcelToggle,
  onSelectAll,
  onSearchChange,
  onFilterStatusChange,
  onSortByChange,
  onSortOrderToggle,
  onWarehouseChange
}: SchedulePageProps) {
  return (
    <section className="schedule-page" lang="en-US" style={{ minHeight: '400px' }}>
      <form className="schedule-panel" lang="en-US" onSubmit={onScheduleSubmit}>
        <div className="schedule-header">
          <h1>Parcel Pool</h1>
          <p>Select Parcels for New Request</p>
        </div>

        {warehouses && warehouses.length > 0 && (
          <div className="schedule-warehouse-selector" style={{ 
            padding: '16px 20px 8px 20px',
            display: 'flex',
            alignItems: 'center'
          }}>
            <label className="schedule-field" style={{ 
              display: 'flex', 
              flexDirection: 'row',
              alignItems: 'center', 
              gap: '12px',
              margin: 0
            }}>
              <span style={{ fontWeight: '500', whiteSpace: 'nowrap' }}>Select Warehouse:</span>
              <select
                value={selectedWarehouseId || ''}
                onChange={(e) => onWarehouseChange(parseInt(e.target.value, 10))}
                required
                style={{
                  padding: '8px 12px',
                  border: '1px solid #ccc',
                  borderRadius: '4px',
                  fontSize: '14px',
                  minWidth: '200px'
                }}
              >
                {warehouses.map(warehouse => (
                  <option key={warehouse.id} value={warehouse.id}>
                    {warehouse.name || `Warehouse ${warehouse.id}`}
                  </option>
                ))}
              </select>
            </label>
          </div>
        )}

        {warehouses.length === 0 && loading && (
          <div style={{ 
            padding: '20px', 
            textAlign: 'center',
            color: '#61716d',
            backgroundColor: '#fff9e6',
            border: '1px solid #ffd966',
            borderRadius: '8px',
            margin: '0 20px'
          }}>
            Loading warehouses... Please wait.
          </div>
        )}

        {warehouses.length === 0 && !loading && (
          <div style={{ 
            padding: '20px', 
            textAlign: 'center',
            color: '#c33',
            backgroundColor: '#fee',
            border: '1px solid #fcc',
            borderRadius: '8px',
            margin: '0 20px'
          }}>
            No warehouses available. Please contact administrator.
          </div>
        )}

        {scheduleError && (
          <div className="schedule-error-message" style={{ 
            padding: '12px 20px', 
            margin: '16px 20px',
            backgroundColor: '#fee', 
            border: '1px solid #fcc',
            borderRadius: '8px',
            color: '#c33'
          }}>
            {scheduleError}
          </div>
        )}

        {!selectedWarehouseId && warehouses && warehouses.length > 0 && !loading && (
          <div className="schedule-message" style={{ 
            padding: '20px', 
            textAlign: 'center',
            color: '#61716d'
          }}>
            Please select a warehouse to view parcels.
          </div>
        )}

        {loading && selectedWarehouseId && (
          <div className="schedule-loading" style={{ 
            padding: '40px', 
            textAlign: 'center',
            color: '#61716d'
          }}>
            Loading parcels...
          </div>
        )}

        <ScheduleControls
          searchText={searchText}
          filterStatus={filterStatus}
          sortBy={sortBy}
          sortOrder={sortOrder}
          onSearchChange={onSearchChange}
          onFilterStatusChange={onFilterStatusChange}
          onSortByChange={onSortByChange}
          onSortOrderToggle={onSortOrderToggle}
        />

        {!loading && selectedWarehouseId && (
          <ScheduleTable
            parcels={filteredAndSortedParcels}
            selectedParcels={selectedScheduleParcels}
            onParcelToggle={onParcelToggle}
            onSelectAll={onSelectAll}
          />
        )}

        <ScheduleForm
          warehouses={warehouses}
          selectedWarehouseId={selectedWarehouseId}
          selectedParcelsCount={selectedScheduleParcels.length}
          loading={loading}
          onWarehouseChange={onWarehouseChange}
        />
      </form>
    </section>
  );
}

