interface RouteCardProps {
  startRoute: (routeId: string) => void;
  routeId: string;
  truckId: string;
  packages: number;
  startTime: string;
  duration: string;
  date: string;
  status?: 'scheduled' | 'in_progress' | 'completed' | 'cancelled';
}

export default function RouteCard({ startRoute, routeId, truckId, packages, startTime, duration, date, status = 'scheduled' }: RouteCardProps) {
    const today = new Date().toISOString().split('T')[0]; // Get today's date in YYYY-MM-DD format
    const isToday = date === today;
    const isFuture = new Date(date) > new Date(today);
    const displayDate = isToday ? "Today" : isFuture ? "Upcoming" : date;
    const isDisabled = status === 'completed' || status === 'cancelled' || !isToday;
    
    // Debug logging
    console.log(`Route ${routeId}: date=${date}, today=${today}, isToday=${isToday}, isFuture=${isFuture}`);
    
    return (
        <div className={`route-card ${!isToday ? 'disabled' : ''}`}>
            <div className="route-header">
                <div className="route-id">Route {routeId}</div>
                <div className="route-date">{displayDate}</div>
            </div>
            <div className="route-truck">
                <span className="truck-icon">🚛</span>
                <span>Truck {truckId}</span>
            </div>
            <div className="route-stats">
                <div className="stat">
                    <div className="stat-label">Packages</div>
                    <div className="stat-value">{packages}</div>
                </div>
                <div className="stat">
                    <div className="stat-label">Start time</div>
                    <div className="stat-value">{startTime}</div>
                </div>
                <div className="stat">
                    <div className="stat-label">Duration</div>
                    <div className="stat-value">{duration}</div>
                </div>
            </div>
            <button 
            className={`start-route-btn ${status === 'in_progress' ? 'in-progress' : status === 'completed' ? 'completed' : ''} ${!isToday ? 'disabled' : ''}`}
            onClick={() => !isDisabled && startRoute(routeId)}
            disabled={isDisabled}
            >
                {!isToday ? '⏳' : status === 'in_progress' ? '⏸' : status === 'completed' ? '✓' : '▶'}
            </button>
        </div>
    );
}