function FeedbackBox({ feedback, setFeedback, handleFeedbackSubmit }: { feedback: string, setFeedback: (feedback: string) => void, handleFeedbackSubmit: () => void }) {
  return (
    <div className="feedback-card">
        <div className="feedback-header">
        <div className="feedback-title">
            <span className="feedback-icon">📦</span>
            <span>Feedback box</span>
            <span className="lightbulb-icon">💡</span>
        </div>
        </div>
        <div className="feedback-content">
        <p className="feedback-text">
            We'd love to hear your ideas! Use the box below to share your suggestions on how we can make our workplace even better for everyone ✨
        </p>
        <div className="feedback-input-container">
            <input
            type="text"
            className="feedback-input"
            placeholder="Give your suggestions......"
            value={feedback}
            onChange={(e) => setFeedback(e.target.value)}
            />
            <button 
            className="feedback-submit"
            onClick={handleFeedbackSubmit}
            >
            →
            </button>
        </div>
        </div>
    </div>  
    );
}

export default FeedbackBox;