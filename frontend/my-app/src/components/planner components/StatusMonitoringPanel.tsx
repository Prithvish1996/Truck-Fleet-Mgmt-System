import React, { useState, useMemo, useEffect, useRef } from 'react';
import './StatusMonitoringPanel.css';

interface StatusMonitoringItem {
  driver: string;
  status: string;
  route: string;
  routeId?: number;
  driverId?: number;
  truckPlateNumber?: string;
  totalDistance?: number;
  totalTransportTime?: number;
  numberOfStops?: number;
  startTime?: string;
}

interface StatusMonitoringPanelProps {
  statusData: StatusMonitoringItem[];
  onRouteClick?: (routeId: number, item: StatusMonitoringItem) => void;
  onDriverClick?: (driverId: number, item: StatusMonitoringItem) => void;
  onStatusFilter?: (status: string | null) => void;
}

type SortField = 'driver' | 'status' | 'route';
type SortOrder = 'asc' | 'desc';

export default function StatusMonitoringPanel({ 
  statusData, 
  onRouteClick,
  onDriverClick,
  onStatusFilter
}: StatusMonitoringPanelProps) {
  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());
  const [sortField, setSortField] = useState<SortField | null>(null);
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const [searchText, setSearchText] = useState('');
  const [hoveredRow, setHoveredRow] = useState<number | null>(null);
  const [showActionsMenu, setShowActionsMenu] = useState<number | null>(null);
  const [lastUpdateTime, setLastUpdateTime] = useState<Date>(new Date());
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    setLastUpdateTime(new Date());
    
    intervalRef.current = setInterval(() => {
      setLastUpdateTime(new Date());
    }, 30000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [statusData]);

  const handleRowClick = (item: StatusMonitoringItem, index: number) => {
    if (item.routeId && onRouteClick) {
      onRouteClick(item.routeId, item);
    }
  };

  const handleDriverClick = (e: React.MouseEvent, item: StatusMonitoringItem) => {
    e.stopPropagation();
    if (item.driverId && onDriverClick) {
      onDriverClick(item.driverId, item);
    }
  };

  const handleRouteClick = (e: React.MouseEvent, item: StatusMonitoringItem) => {
    e.stopPropagation();
    if (item.routeId && onRouteClick) {
      onRouteClick(item.routeId, item);
    }
  };

  const handleStatusClick = (e: React.MouseEvent, status: string) => {
    e.stopPropagation();
    const newFilter = statusFilter === status ? null : status;
    setStatusFilter(newFilter);
    if (onStatusFilter) {
      onStatusFilter(newFilter);
    }
  };

  const toggleRowExpansion = (e: React.MouseEvent, index: number) => {
    e.stopPropagation();
    setExpandedRows(prev => {
      const newSet = new Set(prev);
      if (newSet.has(index)) {
        newSet.delete(index);
      } else {
        newSet.add(index);
      }
      return newSet;
    });
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  const handleActionsMenuToggle = (e: React.MouseEvent, index: number) => {
    e.stopPropagation();
    setShowActionsMenu(showActionsMenu === index ? null : index);
  };

  const filteredAndSortedData = useMemo(() => {
    let filtered = [...statusData];

    if (searchText) {
      filtered = filtered.filter(item =>
        item.driver.toLowerCase().includes(searchText.toLowerCase()) ||
        item.route.toLowerCase().includes(searchText.toLowerCase())
      );
    }

    if (statusFilter) {
      filtered = filtered.filter(item => item.status === statusFilter);
    }

    if (sortField) {
      filtered.sort((a, b) => {
        let aVal: string;
        let bVal: string;

        switch (sortField) {
          case 'driver':
            aVal = a.driver;
            bVal = b.driver;
            break;
          case 'status':
            aVal = a.status;
            bVal = b.status;
            break;
          case 'route':
            aVal = a.route;
            bVal = b.route;
            break;
          default:
            return 0;
        }

        const comparison = aVal.localeCompare(bVal);
        return sortOrder === 'asc' ? comparison : -comparison;
      });
    }

    return filtered;
  }, [statusData, searchText, statusFilter, sortField, sortOrder]);

  const uniqueStatuses = useMemo(() => {
    return Array.from(new Set(statusData.map(item => item.status)));
  }, [statusData]);

  const getSortIcon = (field: SortField) => {
    if (sortField !== field) return '↕️';
    return sortOrder === 'asc' ? '↑' : '↓';
  };

  useEffect(() => {
    const handleClickOutside = () => {
      setShowActionsMenu(null);
    };
    if (showActionsMenu !== null) {
      document.addEventListener('click', handleClickOutside);
      return () => document.removeEventListener('click', handleClickOutside);
    }
  }, [showActionsMenu]);

  return (
    <div className="panel status-monitoring">
      <div className="panel-header">
        <h2>Status Monitoring</h2>
        <div className="status-monitoring-controls">
          <div className="last-update-time">
            Updated: {lastUpdateTime.toLocaleTimeString()}
          </div>
        </div>
      </div>

      <div className="status-monitoring-filters">
        <input
          type="text"
          placeholder="Search driver or route..."
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          className="status-search-input"
          onClick={(e) => e.stopPropagation()}
        />
        <div className="status-filter-buttons">
          <button
            className={`status-filter-btn ${statusFilter === null ? 'active' : ''}`}
            onClick={(e) => {
              e.stopPropagation();
              setStatusFilter(null);
              if (onStatusFilter) onStatusFilter(null);
            }}
          >
            All
          </button>
          {uniqueStatuses.map(status => (
            <button
              key={status}
              className={`status-filter-btn ${statusFilter === status ? 'active' : ''}`}
              onClick={(e) => handleStatusClick(e, status)}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      <div className="table-wrapper">
        <table className="status-monitoring-table">
          <thead>
            <tr>
              <th 
                className="sortable-header"
                onClick={() => handleSort('driver')}
              >
                Driver {getSortIcon('driver')}
              </th>
              <th 
                className="sortable-header"
                onClick={() => handleSort('status')}
              >
                Status {getSortIcon('status')}
              </th>
              <th 
                className="sortable-header"
                onClick={() => handleSort('route')}
              >
                Route {getSortIcon('route')}
              </th>
              <th className="actions-header">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredAndSortedData.length === 0 ? (
              <tr>
                <td colSpan={4} style={{ padding: '20px', textAlign: 'center', color: '#61716d' }}>
                  {searchText || statusFilter ? 'No routes match your filters' : 'No active routes'}
                </td>
              </tr>
            ) : (
              filteredAndSortedData.map((item, index) => {
                const isExpanded = expandedRows.has(index);
                const isHovered = hoveredRow === index;
                const showMenu = showActionsMenu === index;

                return (
                  <React.Fragment key={index}>
                    <tr
                      className={`status-row ${item.routeId ? 'clickable' : ''} ${isHovered ? 'hovered' : ''}`}
                      onClick={() => handleRowClick(item, index)}
                      onMouseEnter={() => setHoveredRow(index)}
                      onMouseLeave={() => setHoveredRow(null)}
                    >
                      <td 
                        className={`driver-cell ${item.driverId ? 'clickable' : ''}`}
                        onClick={(e) => handleDriverClick(e, item)}
                        title={item.driverId ? 'Click to view driver details' : ''}
                      >
                        {item.driver}
                        {item.driverId && <span className="link-indicator">👤</span>}
                      </td>
                      <td>
                        <span 
                          className={`status-badge status-${item.status.toLowerCase() === 'assigned' ? 'delivery' : item.status.toLowerCase() === 'in_progress' ? 'processing' : 'exceptions'} clickable-badge`}
                          onClick={(e) => handleStatusClick(e, item.status)}
                          title={`Click to filter by ${item.status}`}
                        >
                          {item.status}
                        </span>
                      </td>
                      <td 
                        className={`route-cell ${item.routeId ? 'clickable' : ''}`}
                        onClick={(e) => handleRouteClick(e, item)}
                        title={item.routeId ? 'Click to view route details' : ''}
                      >
                        {item.route}
                        {item.routeId && <span className="link-indicator">📍</span>}
                      </td>
                      <td className="actions-cell" onClick={(e) => e.stopPropagation()}>
                        <div className="row-actions">
                          <button
                            className="expand-btn"
                            onClick={(e) => toggleRowExpansion(e, index)}
                            title={isExpanded ? 'Collapse details' : 'Expand details'}
                          >
                            {isExpanded ? '▼' : '▶'}
                          </button>
                          <div className="actions-menu-wrapper">
                            <button
                              className="actions-menu-btn"
                              onClick={(e) => handleActionsMenuToggle(e, index)}
                              title="More actions"
                            >
                              ⋯
                            </button>
                            {showMenu && (
                              <div className="actions-menu">
                                {item.routeId && (
                                  <button
                                    className="action-item"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleRouteClick(e, item);
                                      setShowActionsMenu(null);
                                    }}
                                  >
                                    📍 View Route Details
                                  </button>
                                )}
                                {item.driverId && (
                                  <button
                                    className="action-item"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleDriverClick(e, item);
                                      setShowActionsMenu(null);
                                    }}
                                  >
                                    👤 View Driver Profile
                                  </button>
                                )}
                                {item.driverId && (
                                  <button
                                    className="action-item"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setShowActionsMenu(null);
                                    }}
                                  >
                                    📞 Contact Driver
                                  </button>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                    </tr>
                    {isExpanded && (
                      <tr className="expanded-row-details">
                        <td colSpan={4}>
                          <div className="row-details-content">
                            <div className="detail-item">
                              <span className="detail-label">Route ID:</span>
                              <span className="detail-value">{item.routeId || 'N/A'}</span>
                            </div>
                            {item.truckPlateNumber && (
                              <div className="detail-item">
                                <span className="detail-label">Truck:</span>
                                <span className="detail-value">{item.truckPlateNumber}</span>
                              </div>
                            )}
                            {item.totalDistance !== undefined && (
                              <div className="detail-item">
                                <span className="detail-label">Distance:</span>
                                <span className="detail-value">{item.totalDistance.toFixed(2)} km</span>
                              </div>
                            )}
                            {item.totalTransportTime !== undefined && (
                              <div className="detail-item">
                                <span className="detail-label">Transport Time:</span>
                                <span className="detail-value">{item.totalTransportTime} min</span>
                              </div>
                            )}
                            {item.numberOfStops !== undefined && (
                              <div className="detail-item">
                                <span className="detail-label">Stops:</span>
                                <span className="detail-value">{item.numberOfStops}</span>
                              </div>
                            )}
                            {item.startTime && (
                              <div className="detail-item">
                                <span className="detail-label">Start Time:</span>
                                <span className="detail-value">{new Date(item.startTime).toLocaleString()}</span>
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
