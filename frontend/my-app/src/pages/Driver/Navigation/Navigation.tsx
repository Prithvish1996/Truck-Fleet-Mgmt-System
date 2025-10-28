import React, { useState, useEffect } from 'react';
import GraphhopperNavigation from './GraphhopperNavigation';
import NavigationFallback from './NavigationFallback';
import { navigationConfig, debugLog } from './navigationConfig';

interface NavigationProps {
  navigate: (path: string) => void;
}

const Navigation: React.FC<NavigationProps> = ({ navigate }) => {
  const [useFallback, setUseFallback] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    debugLog('Navigation component initialized', {
      useGraphhopper: navigationConfig.useGraphhopper,
      fallbackOnError: navigationConfig.fallbackOnError,
      debugMode: navigationConfig.debugMode
    });
  }, []);

  const handleGraphhopperError = (errorMessage: string) => {
    debugLog('Graphhopper error occurred:', errorMessage);
    setError(errorMessage);
    
    if (navigationConfig.fallbackOnError) {
      debugLog('Switching to fallback navigation due to error');
      setUseFallback(true);
    }
  };

  const handleRetry = () => {
    debugLog('Retrying Graphhopper navigation');
    setError(null);
    setUseFallback(false);
  };

  // If fallback is forced or Graphhopper is disabled, use fallback
  if (useFallback || !navigationConfig.useGraphhopper) {
    debugLog('Using fallback navigation');
    return <NavigationFallback navigate={navigate} />;
  }

  // Use Graphhopper navigation with error handling
  debugLog('Using Graphhopper navigation');
  return (
    <div>
      <GraphhopperNavigation navigate={navigate} />
      {error && navigationConfig.fallbackOnError && (
        <div style={{
          position: 'fixed',
          top: '10px',
          right: '10px',
          background: '#ff6b6b',
          color: 'white',
          padding: '10px',
          borderRadius: '5px',
          zIndex: 10000,
          fontSize: '12px'
        }}>
          <div>Graphhopper Error: {error}</div>
          <button 
            onClick={handleRetry}
            style={{
              background: 'white',
              color: '#ff6b6b',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '3px',
              marginTop: '5px',
              cursor: 'pointer'
            }}
          >
            Retry
          </button>
        </div>
      )}
    </div>
  );
};

export default Navigation;