import React, { useState } from 'react';
import './Suggestions.css';
import { suggestionService } from '../../../services/suggestionService';

export default function Suggestions() {
  const [feedback, setFeedback] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitMessage, setSubmitMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handleFeedbackSubmit = async () => {
    if (!feedback.trim()) {
      return;
    }

    setIsSubmitting(true);
    setSubmitMessage(null);

    try {
      const message = await suggestionService.submitSuggestion(feedback);
      setSubmitMessage({ type: 'success', text: message });
      setFeedback('');
      
      setTimeout(() => {
        setSubmitMessage(null);
      }, 3000);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to submit suggestion. Please try again.';
      setSubmitMessage({ type: 'error', text: errorMessage });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="suggestions-page">
      <div className="suggestions-header">
        <h1 className="suggestions-title">💡 Suggestions</h1>
        <p className="suggestions-subtitle">
          We'd love to hear your ideas! Share your suggestions on how we can make our workplace even better for everyone.
        </p>
      </div>

      <div className="suggestions-content">
        <div className="feedback-section">
          <div className="feedback-card">
            <div className="feedback-header">
              <div className="feedback-title">
                <span className="feedback-icon">📦</span>
                <span>Share Your Feedback</span>
              </div>
            </div>
            <div className="feedback-content">
              <p className="feedback-text">
                Your voice matters! Help us improve by sharing your thoughts, ideas, and suggestions. Every piece of feedback helps us create a better work environment.
              </p>
              <div className="feedback-input-container">
                <input
                  type="text"
                  className="feedback-input"
                  placeholder="Type your suggestion here..."
                  value={feedback}
                  onChange={(e) => setFeedback(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter' && !isSubmitting && feedback.trim()) {
                      handleFeedbackSubmit();
                    }
                  }}
                  disabled={isSubmitting}
                />
                <button 
                  className="feedback-submit"
                  onClick={handleFeedbackSubmit}
                  disabled={!feedback.trim() || isSubmitting}
                >
                  {isSubmitting ? '...' : '→'}
                </button>
              </div>
              {submitMessage && (
                <div className={`submit-message ${submitMessage.type}`}>
                  {submitMessage.text}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="suggestions-info">
          <div className="info-card">
            <div className="info-icon">✨</div>
            <h3>Why Your Feedback Matters</h3>
            <p>Your suggestions help us identify areas for improvement and create a more positive workplace experience for everyone.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

