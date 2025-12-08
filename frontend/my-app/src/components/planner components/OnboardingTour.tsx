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
  const [tooltipStyle, setTooltipStyle] = useState<React.CSSProperties>({});
  const [arrowPath, setArrowPath] = useState<string>('');
  const overlayRef = useRef<HTMLDivElement>(null);
  const tooltipRef = useRef<HTMLDivElement>(null);

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

      const tooltipWidth = 400;
      const tooltipHeight = 300;
      const spacing = 30;
      let tooltipTop = 0;
      let tooltipLeft = 0;
      let arrowStartX = 0;
      let arrowStartY = 0;

      const elementCenterX = rect.left + rect.width / 2;
      const elementCenterY = rect.top + rect.height / 2;

      switch (step.position) {
        case 'top':
          tooltipTop = rect.top - tooltipHeight - spacing;
          tooltipLeft = elementCenterX - tooltipWidth / 2;
          arrowStartX = tooltipWidth / 2;
          arrowStartY = tooltipHeight;
          break;
        case 'bottom':
          tooltipTop = rect.bottom + spacing;
          tooltipLeft = elementCenterX - tooltipWidth / 2;
          arrowStartX = tooltipWidth / 2;
          arrowStartY = 0;
          break;
        case 'left':
          tooltipTop = elementCenterY - tooltipHeight / 2;
          tooltipLeft = rect.left - tooltipWidth - spacing;
          arrowStartX = tooltipWidth;
          arrowStartY = tooltipHeight / 2;
          break;
        case 'right':
          tooltipTop = elementCenterY - tooltipHeight / 2;
          tooltipLeft = rect.right + spacing;
          arrowStartX = 0;
          arrowStartY = tooltipHeight / 2;
          break;
      }

      tooltipLeft = Math.max(20, Math.min(tooltipLeft, window.innerWidth - tooltipWidth - 20));
      tooltipTop = Math.max(20, Math.min(tooltipTop, window.innerHeight - tooltipHeight - 20));

      setTooltipStyle({
        top: `${tooltipTop + scrollY}px`,
        left: `${tooltipLeft + scrollX}px`,
      });

      const startX = tooltipLeft + arrowStartX;
      const startY = tooltipTop + arrowStartY + scrollY;
      const endX = rect.left + rect.width / 2;
      const endY = rect.top + rect.height / 2 + scrollY;

      const midX = (startX + endX) / 2;
      const midY = (startY + endY) / 2;
      const controlX1 = startX + (endX - startX) * 0.3;
      const controlY1 = startY;
      const controlX2 = endX - (endX - startX) * 0.3;
      const controlY2 = endY;

      const path = `M ${startX} ${startY} C ${controlX1} ${controlY1}, ${controlX2} ${controlY2}, ${endX} ${endY}`;
      setArrowPath(path);
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

      {arrowPath && (
        <svg 
          className="onboarding-arrow"
          style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', pointerEvents: 'none', zIndex: 9999 }}
        >
          <defs>
            <marker
              id="arrowhead"
              markerWidth="10"
              markerHeight="10"
              refX="9"
              refY="3"
              orient="auto"
            >
              <polygon points="0 0, 10 3, 0 6" fill="#82c885" />
            </marker>
            <linearGradient id="arrowGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#82c885" stopOpacity="0.8" />
              <stop offset="100%" stopColor="#2f8b56" stopOpacity="0.8" />
            </linearGradient>
          </defs>
          <path
            d={arrowPath}
            stroke="url(#arrowGradient)"
            strokeWidth="3"
            fill="none"
            markerEnd="url(#arrowhead)"
            className="arrow-path"
          />
        </svg>
      )}

      <div 
        ref={tooltipRef}
        className="onboarding-tooltip"
        style={tooltipStyle}
      >
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
