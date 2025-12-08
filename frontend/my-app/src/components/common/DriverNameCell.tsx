import React from 'react';
import { DriverResponse } from '../../services/plannerService';

interface DriverNameCellProps {
  driverId: string | null;
  drivers: DriverResponse[];
  fallback?: string;
}

export default function DriverNameCell({ driverId, drivers, fallback = 'Unassigned' }: DriverNameCellProps) {
  if (!driverId) return <>{fallback}</>;
  
  const driver = drivers.find(d => d.id.toString() === driverId);
  const driverName = driver 
    ? (driver.userName || driver.Name || `Driver ${driver.id}`)
    : 'Unknown';
  
  return <>{driverName}</>;
}

