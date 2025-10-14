import React from 'react';
import './BottomTabBar.css';

interface BottomTabBarProps {
  activeTab: 'home' | 'agenda';
  onTabChange: (tab: 'home' | 'agenda') => void;
}

export default function BottomTabBar({ activeTab, onTabChange }: BottomTabBarProps) {
  return (
    <div className="bottom-tab-bar">
      <button 
        className={`tab-button ${activeTab === 'home' ? 'active' : ''}`}
        onClick={() => onTabChange('home')}
      >
        <div className="tab-icon">⌂</div>
        <span className="tab-label">Home</span>
      </button>
      
      <button 
        className={`tab-button ${activeTab === 'agenda' ? 'active' : ''}`}
        onClick={() => onTabChange('agenda')}
      >
        <div className="tab-icon">⚏</div>
        <span className="tab-label">Agenda</span>
      </button>
    </div>
  );
}
