import React from 'react';

interface PriorityBadgeProps {
  priority: 'High' | 'Medium' | 'Low';
  className?: string;
}

export default function PriorityBadge({ priority, className = 'priority-badge' }: PriorityBadgeProps) {
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'High':
        return '#ff4444';
      case 'Medium':
        return '#2f8b56';
      case 'Low':
        return '#2196F3';
      default:
        return '#666';
    }
  };

  return (
    <span 
      className={className}
      style={{ color: getPriorityColor(priority) }}
    >
      {priority}
    </span>
  );
}

