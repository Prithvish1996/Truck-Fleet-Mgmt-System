import React from 'react';
import './CompletedState.css';

interface CompletedStateProps {
  onComplete: () => void;
}

const CompletedState: React.FC<CompletedStateProps> = ({ onComplete }) => {
  return (
    <div className="completed-state">
      <div className="completed-state__icon">✓</div>
      <h2>All packages delivered!</h2>
      <p>All packages have been successfully delivered.</p>
      <button 
        onClick={onComplete}
        className="completed-state__button"
      >
        OK
      </button>
    </div>
  );
};

export default CompletedState;

