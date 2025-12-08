import React from 'react';
import { DriverResponse } from '../../services/plannerService';

interface AvailableDriversPanelProps {
  drivers: DriverResponse[];
}

export default function AvailableDriversPanel({ drivers }: AvailableDriversPanelProps) {
  return (
    <div className="panel available-drivers">
      <div className="panel-header">
        <h2>Available Drivers</h2>
      </div>
      <div className="available-drivers-list">
        {drivers.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#61716d' }}>
            No available drivers
          </div>
        ) : (
          drivers.map(driver => (
            <div key={driver.id} className="driver-item">
              <div className="driver-avatar">
                {driver.userName?.charAt(0).toUpperCase() || driver.Name?.charAt(0).toUpperCase() || 'D'}
              </div>
              <div className="driver-details">
                <div className="driver-name">{driver.Name || driver.userName || 'Unknown'}</div>
                <div className="driver-window">
                  <span className="work-window">
                    {driver.email || 'No email'}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

