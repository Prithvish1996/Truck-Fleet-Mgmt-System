import React from 'react';
import './WorkflowIndicator.css';

interface WorkflowStep {
  label: string;
  icon: string;
  completed: boolean;
  active: boolean;
}

interface WorkflowIndicatorProps {
  steps: WorkflowStep[];
}

export default function WorkflowIndicator({ steps }: WorkflowIndicatorProps) {
  return (
    <div className="workflow-indicator">
      <div className="workflow-steps">
        {steps.map((step, index) => (
          <React.Fragment key={index}>
            <div className={`workflow-step ${step.active ? 'active' : ''} ${step.completed ? 'completed' : ''}`}>
              <div className="step-icon">
                {step.completed ? '✓' : step.icon}
              </div>
              <div className="step-label">{step.label}</div>
            </div>
            {index < steps.length - 1 && (
              <div className={`workflow-connector ${step.completed ? 'completed' : ''}`} />
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}

