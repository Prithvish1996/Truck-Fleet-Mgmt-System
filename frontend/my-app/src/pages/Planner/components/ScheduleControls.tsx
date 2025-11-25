import React from 'react';

interface ScheduleControlsProps {
  searchText: string;
  filterStatus: 'All' | 'Pending' | 'Scheduled';
  sortBy: 'id' | 'receiver' | 'location' | 'warehouse';
  sortOrder: 'asc' | 'desc';
  onSearchChange: (value: string) => void;
  onFilterStatusChange: (value: 'All' | 'Pending' | 'Scheduled') => void;
  onSortByChange: (value: 'id' | 'receiver' | 'location' | 'warehouse') => void;
  onSortOrderToggle: () => void;
}

export default function ScheduleControls({
  searchText,
  filterStatus,
  sortBy,
  sortOrder,
  onSearchChange,
  onFilterStatusChange,
  onSortByChange,
  onSortOrderToggle
}: ScheduleControlsProps) {
  return (
    <div className="schedule-controls">
      <div className="schedule-search">
        <input
          type="text"
          placeholder="Search parcels..."
          value={searchText}
          onChange={(e) => onSearchChange(e.target.value)}
          className="search-input"
        />
      </div>
      
      <div className="schedule-filters">
        <label className="filter-label">
          <span>Status:</span>
          <select
            value={filterStatus}
            onChange={(e) => onFilterStatusChange(e.target.value as 'All' | 'Pending' | 'Scheduled')}
          >
            <option value="All">All</option>
            <option value="Pending">Pending</option>
            <option value="Scheduled">Scheduled</option>
          </select>
        </label>
        
        <label className="filter-label">
          <span>Sort By:</span>
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value as 'id' | 'receiver' | 'location' | 'warehouse')}
          >
            <option value="id">Parcel ID</option>
            <option value="receiver">Receiver</option>
            <option value="location">Location</option>
            <option value="warehouse">Warehouse</option>
          </select>
        </label>
        
        <button
          type="button"
          className="sort-order-btn"
          onClick={onSortOrderToggle}
          title={`Sort ${sortOrder === 'asc' ? 'Descending' : 'Ascending'}`}
        >
          {sortOrder === 'asc' ? '↑' : '↓'}
        </button>
      </div>
    </div>
  );
}

