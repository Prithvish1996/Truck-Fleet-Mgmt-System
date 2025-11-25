import React from 'react';

interface StatusMonitoringItem {
  driver: string;
  status: string;
  route: string;
}

interface StatusMonitoringPanelProps {
  statusData: StatusMonitoringItem[];
}

export default function StatusMonitoringPanel({ statusData }: StatusMonitoringPanelProps) {
  return (
    <div className="panel status-monitoring">
      <div className="panel-header">
        <h2>Status Monitoring</h2>
      </div>
      <div className="table-wrapper">
        <table className="status-monitoring-table">
          <thead>
            <tr>
              <th>Driver</th>
              <th>Status</th>
              <th>Route</th>
            </tr>
          </thead>
          <tbody>
            {statusData.length === 0 ? (
              <tr>
                <td colSpan={3} style={{ padding: '20px', textAlign: 'center', color: '#61716d' }}>
                  No active routes
                </td>
              </tr>
            ) : (
              statusData.map((item, index) => (
                <tr key={index}>
                  <td>{item.driver}</td>
                  <td>
                    <span className={`status-badge status-${item.status.toLowerCase() === 'assigned' ? 'delivery' : item.status.toLowerCase() === 'in_progress' ? 'processing' : 'exceptions'}`}>
                      {item.status}
                    </span>
                  </td>
                  <td>{item.route}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

