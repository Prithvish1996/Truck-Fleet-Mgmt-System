import React, { useState, useEffect, useRef } from 'react';
import './OnboardingTour.css';

interface TourStep {
  target: string;
  title: string;
  content: string;
  position: 'top' | 'bottom' | 'left' | 'right';
  action?: {
    label: string;
    onClick: () => void;
  };
}

interface OnboardingTourProps {
  steps: TourStep[];
  onComplete: () => void;
  onSkip: () => void;
}

export default function OnboardingTour({ steps, onComplete, onSkip }: OnboardingTourProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [overlayStyle, setOverlayStyle] = useState<React.CSSProperties>({});
  const overlayRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (currentStep >= steps.length) {
      onComplete();
      return;
    }

    const step = steps[currentStep];
    const element = document.querySelector(step.target);
    
    if (element) {
      const rect = element.getBoundingClientRect();
      const scrollY = window.scrollY;
      const scrollX = window.scrollX;

      setOverlayStyle({
        top: `${rect.top + scrollY}px`,
        left: `${rect.left + scrollX}px`,
        width: `${rect.width}px`,
        height: `${rect.height}px`,
      });

      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [currentStep, steps, onComplete]);

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      onComplete();
    }
  };

  const handlePrevious = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  if (currentStep >= steps.length) return null;

  const step = steps[currentStep];

  return (
    <>
      <div className="onboarding-overlay" />
      
      <div 
        ref={overlayRef}
        className="onboarding-highlight"
        style={overlayStyle}
      />

      <div className="onboarding-tooltip">
        <div className="tooltip-header">
          <div className="tooltip-step-indicator">
            Step {currentStep + 1} of {steps.length}
          </div>
          <button className="tooltip-close" onClick={onSkip}>×</button>
        </div>
        
        <h3 className="tooltip-title">{step.title}</h3>
        <p className="tooltip-content">{step.content}</p>
        
        {step.action && (
          <button 
            className="tooltip-action-btn"
            onClick={step.action.onClick}
          >
            {step.action.label}
          </button>
        )}

        <div className="tooltip-footer">
          <button 
            className="tooltip-btn-secondary"
            onClick={onSkip}
          >
            Skip Tour
          </button>
          <div className="tooltip-nav">
            <button 
              className="tooltip-btn-secondary"
              onClick={handlePrevious}
              disabled={currentStep === 0}
            >
              Previous
            </button>
            <button 
              className="tooltip-btn-primary"
              onClick={handleNext}
            >
              {currentStep === steps.length - 1 ? 'Get Started' : 'Next'}
            </button>
          </div>
        </div>
      </div>
    </>
  );
}

