import React from 'react';
import { DriverResponse } from '../../../services/plannerService';

interface DriverSelectProps {
  value: string | null;
  drivers: DriverResponse[];
  disabled: boolean;
  onChange: (driverId: string | null) => void;
}

export default function DriverSelect({ value, drivers, disabled, onChange }: DriverSelectProps) {
  return (
    <select
      className="driver-select"
      value={value || ''}
      onChange={(e) => onChange(e.target.value === '' ? null : e.target.value)}
      disabled={disabled}
    >
      <option value="">Select Driver</option>
      {drivers.map(driver => (
        <option key={driver.id} value={driver.id.toString()}>
          {driver.userName || driver.Name || `Driver ${driver.id}`}
        </option>
      ))}
    </select>
  );
}

