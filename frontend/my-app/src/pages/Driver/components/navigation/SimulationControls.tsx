import React, { useState, useEffect, useCallback } from 'react';
import { GraphhopperRoute } from '../../../../services/graphhopperService';
import { graphhopperService } from '../../../../services/graphhopperService';
import { routeSimulator, SimulationState } from '../../../../services/routeSimulator';
import './SimulationControls.css';

interface SimulationControlsProps {
  routeData: GraphhopperRoute | null;
  onLocationUpdate: (location: [number, number]) => void;
  onHeadingUpdate: (heading: number) => void;
}

const SimulationControls: React.FC<SimulationControlsProps> = ({
  routeData,
  onLocationUpdate,
  onHeadingUpdate
}) => {
  const [isSimulating, setIsSimulating] = useState(false);
  const [speedMultiplier, setSpeedMultiplier] = useState(1);
  const [progress, setProgress] = useState(0);
  const [simulationState, setSimulationState] = useState<SimulationState | null>(null);
  const [isCollapsed, setIsCollapsed] = useState(false);

  // Create stable callback functions
  const handleLocationUpdate = useCallback((location: [number, number]) => {
    onLocationUpdate(location);
  }, [onLocationUpdate]);

  const handleHeadingUpdate = useCallback((heading: number) => {
    onHeadingUpdate(heading);
  }, [onHeadingUpdate]);

  const handleSimulationComplete = useCallback(() => {
    setIsSimulating(false);
    console.log('Simulation completed!');
  }, []);

  const handleSimulationError = useCallback((error: string) => {
    console.error('Simulation error:', error);
    setIsSimulating(false);
  }, []);

  useEffect(() => {
    // Set up simulator callbacks
    routeSimulator.setCallbacks({
      onLocationUpdate: handleLocationUpdate,
      onHeadingUpdate: handleHeadingUpdate,
      onSimulationComplete: handleSimulationComplete,
      onError: handleSimulationError
    });

          // Update progress periodically
          const progressInterval = setInterval(() => {
            if (isSimulating) {
              const state = routeSimulator.getState();
              setSimulationState(state);
              setProgress(state.progress);
            }
          }, 200); // Update every 200ms to match simulation frequency

    return () => {
      clearInterval(progressInterval);
      // Don't cleanup the simulator here as it's shared
    };
  }, [isSimulating, handleLocationUpdate, handleHeadingUpdate, handleSimulationComplete, handleSimulationError]);

  const handleStartSimulation = () => {
    console.log('Start simulation clicked!');
    console.log('Route data:', routeData);
    
    if (!routeData) {
      console.error('No route data available for simulation');
      return;
    }

    console.log('Loading route into simulator...');
    routeSimulator.loadRoute(routeData);
    routeSimulator.setSpeedMultiplier(speedMultiplier);
    
    console.log('Starting simulation...');
    routeSimulator.startSimulation();
    setIsSimulating(true);
    
    console.log('Simulation state after start:', routeSimulator.getState());
  };

  const handleStopSimulation = () => {
    routeSimulator.stopSimulation();
    setIsSimulating(false);
  };

  const handlePauseSimulation = () => {
    routeSimulator.pauseSimulation();
  };

  const handleResumeSimulation = () => {
    routeSimulator.resumeSimulation();
  };

  const handleSpeedChange = (newSpeed: number) => {
    setSpeedMultiplier(newSpeed);
    routeSimulator.setSpeedMultiplier(newSpeed);
  };

  const handleProgressChange = (newProgress: number) => {
    if (!routeData) return;
    
    // Calculate target index based on progress
    const targetIndex = Math.floor(newProgress * (routeData.paths[0]?.points ? 
      graphhopperService.decodePolyline(routeData.paths[0].points).length - 1 : 0));
    
    console.log('Jumping to progress:', newProgress, 'index:', targetIndex);
    routeSimulator.jumpToPoint(targetIndex);
    setProgress(newProgress);
  };

  return (
    <div className="simulation-controls">
      <div className="simulation-controls__header">
        <h3>Route Simulation</h3>
        <div className="simulation-controls__header-right">
          <div className="simulation-controls__status">
            {isSimulating ? (
              <span className="status-indicator status-indicator--running">Running</span>
            ) : (
              <span className="status-indicator status-indicator--stopped">Stopped</span>
            )}
          </div>
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="collapse-btn"
            title={isCollapsed ? 'Expand controls' : 'Collapse controls'}
          >
            {isCollapsed ? '▼' : '▲'}
          </button>
        </div>
      </div>

      {!isCollapsed && (
        <>
          {!routeData ? (
            <div className="simulation-controls__no-route">
              <p>No route data available. Please wait for a route to be loaded.</p>
              <p>Make sure you have:</p>
              <ul>
                <li>Granted location permission</li>
                <li>A destination set</li>
                <li>An active internet connection</li>
              </ul>
            </div>
          ) : (
            <div className="simulation-controls__content">
        {/* Speed Control */}
        <div className="control-group">
          <label htmlFor="speed-slider">Speed Multiplier: {speedMultiplier}x</label>
          <input
            id="speed-slider"
            type="range"
            min="0.5"
            max="10"
            step="0.5"
            value={speedMultiplier}
            onChange={(e) => handleSpeedChange(Number(e.target.value))}
            className="speed-slider"
            disabled={isSimulating}
          />
          <div className="speed-labels">
            <span>0.5x</span>
            <span>5x</span>
            <span>10x</span>
          </div>
        </div>

        {/* Progress Control */}
        <div className="control-group">
          <label htmlFor="progress-slider">Progress: {Math.round(progress * 100)}%</label>
          <input
            id="progress-slider"
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={progress}
            onChange={(e) => handleProgressChange(Number(e.target.value))}
            className="progress-slider"
          />
        </div>

        {/* Control Buttons */}
        <div className="control-buttons">
          {!isSimulating ? (
            <button
              onClick={handleStartSimulation}
              className="btn btn--primary btn--start"
              disabled={!routeData}
            >
              Start Simulation
            </button>
          ) : (
            <>
              <button
                onClick={handlePauseSimulation}
                className="btn btn--secondary btn--pause"
              >
                Pause
              </button>
              <button
                onClick={handleResumeSimulation}
                className="btn btn--secondary btn--resume"
              >
                Resume
              </button>
              <button
                onClick={handleStopSimulation}
                className="btn btn--danger btn--stop"
              >
                Stop
              </button>
            </>
          )}
        </div>

        {/* Simulation Info */}
        {simulationState && (
          <div className="simulation-info">
            <div className="info-item">
              <span className="info-label">Current Position:</span>
              <span className="info-value">
                {simulationState.currentLocation 
                  ? `${simulationState.currentLocation[0].toFixed(6)}, ${simulationState.currentLocation[1].toFixed(6)}`
                  : 'N/A'
                }
              </span>
            </div>
            <div className="info-item">
              <span className="info-label">Heading:</span>
              <span className="info-value">{Math.round(simulationState.currentHeading)}°</span>
            </div>
            <div className="info-item">
              <span className="info-label">Point:</span>
              <span className="info-value">{simulationState.currentIndex} / {simulationState.totalPoints}</span>
            </div>
          </div>
        )}
      </div>
          )}
        </>
      )}
    </div>
  );
};

export default SimulationControls;
