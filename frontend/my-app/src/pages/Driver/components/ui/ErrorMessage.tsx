import React from 'react';
import './ErrorMessage.css';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  retryText?: string;
  className?: string;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ 
  message, 
  onRetry, 
  retryText = 'Retry',
  className = ''
}) => {
  return (
    <div className={`error-message ${className}`}>
      <div className="error-message__icon">⚠️</div>
      <p className="error-message__text">{message}</p>
      {onRetry && (
        <button 
          className="error-message__retry-button"
          onClick={onRetry}
        >
          {retryText}
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;
