import React from 'react';

interface ScheduleFormProps {
  warehouses: any[];
  selectedWarehouseId: number | null;
  selectedParcelsCount: number;
  loading: boolean;
  onWarehouseChange: (warehouseId: number) => void;
}

export default function ScheduleForm({
  warehouses,
  selectedWarehouseId,
  selectedParcelsCount,
  loading,
  onWarehouseChange
}: ScheduleFormProps) {
  return (
    <>
      <div className="schedule-footer">
        <div className="schedule-count">
          <span>Number of Parcel:</span>
          <strong>{selectedParcelsCount}</strong>
        </div>
        <button type="submit" className="schedule-submit" disabled={loading}>
          {loading ? 'Scheduling Parcels...' : 'Schedule Parcels'}
        </button>
      </div>
    </>
  );
}

