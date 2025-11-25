import React from 'react';

type PriorityLevel = 'High' | 'Medium' | 'Low';

interface ScheduleFormProps {
  warehouses: any[];
  selectedWarehouseId: number | null;
  scheduleDate: string;
  scheduleTruck: string;
  schedulePriority: PriorityLevel;
  availableTrucks: string[];
  priorityOptions: PriorityLevel[];
  selectedParcelsCount: number;
  loading: boolean;
  onWarehouseChange: (warehouseId: number) => void;
  onDateChange: (date: string) => void;
  onTruckChange: (truck: string) => void;
  onPriorityChange: (priority: PriorityLevel) => void;
}

export default function ScheduleForm({
  warehouses,
  selectedWarehouseId,
  scheduleDate,
  scheduleTruck,
  schedulePriority,
  availableTrucks,
  priorityOptions,
  selectedParcelsCount,
  loading,
  onWarehouseChange,
  onDateChange,
  onTruckChange,
  onPriorityChange
}: ScheduleFormProps) {
  return (
    <>
      {warehouses && warehouses.length > 0 && (
        <div className="schedule-form-grid">
          <label className="schedule-field">
            <span>Select Warehouse:</span>
            <select
              value={selectedWarehouseId || ''}
              onChange={(e) => onWarehouseChange(parseInt(e.target.value, 10))}
              required
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

      <div className="schedule-form-grid">
        <label className="schedule-field">
          <span>Schedule Delivery Date:</span>
          <input
            type="date"
            lang="en-US"
            value={scheduleDate}
            onChange={event => onDateChange(event.target.value)}
            required
          />
        </label>
        <label className="schedule-field">
          <span>Assign Truck:</span>
          <select
            value={scheduleTruck}
            onChange={event => onTruckChange(event.target.value)}
          >
            {availableTrucks.map(plate => (
              <option key={plate} value={plate}>
                {plate}
              </option>
            ))}
          </select>
        </label>
        <label className="schedule-field">
          <span>Priority:</span>
          <select
            value={schedulePriority}
            onChange={event => onPriorityChange(event.target.value as PriorityLevel)}
          >
            {priorityOptions.map(option => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="schedule-footer">
        <div className="schedule-count">
          <span>Number of Parcel:</span>
          <strong>{selectedParcelsCount}</strong>
        </div>
        <button type="submit" className="schedule-submit" disabled={loading}>
          {loading ? 'Submitting...' : 'Submit'}
        </button>
      </div>
    </>
  );
}

