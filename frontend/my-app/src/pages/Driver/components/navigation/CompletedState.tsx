import React from 'react';
import './CompletedState.css';

interface CompletedStateProps {
  onReturnToDashboard: () => void;
}

const CompletedState: React.FC<CompletedStateProps> = ({ onReturnToDashboard }) => {
  return (
    <div className="completed-state">
      <div className="completed-state__icon">✓</div>
      <h2>All packages delivered!</h2>
      <p>All packages have been successfully delivered.</p>
      <button 
        onClick={onReturnToDashboard}
        className="completed-state__button"
      >
        Return to Dashboard
      </button>
    </div>
  );
};

export default CompletedState;

