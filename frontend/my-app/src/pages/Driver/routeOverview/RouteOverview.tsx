import { useNavigate, useLocation } from "react-router-dom";
import DriverHeader from "../components/driverHeader";
import { useState, useEffect } from "react";
import { routeService } from "../../../services/routeService";
import { Route, Package } from "../../../types";
import "./RouteOverview.css";

interface RouteOverviewProps {
    routeId?: string;
}

function RouteOverview({ routeId: propRouteId }: RouteOverviewProps = {} as RouteOverviewProps) {
    const navigate = useNavigate();
    const location = useLocation();
    const [currentRoute, setCurrentRoute] = useState<Route | null>(null);
    const [packages, setPackages] = useState<Package[]>([]);
    const [loading, setLoading] = useState(true);
    
    const storedRouteId = sessionStorage.getItem('currentRouteId');
    const routeId = propRouteId || 
                    (location.state as any)?.routeId || 
                    storedRouteId ||
                    null;

    useEffect(() => {
        loadRoute();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [routeId]);

    const loadRoute = async () => {
        try {
            setLoading(true);
            
            if (routeId) {
                const route = await routeService.getRouteById(routeId);
                if (route) {
                    setCurrentRoute(route);
                    setPackages(route.packages);
                }
            } else {
                const routes = await routeService.getDriverRoutes();
                const inProgressRoute = routes.find(route => route.status === 'in_progress');
                if (inProgressRoute) {
                    setCurrentRoute(inProgressRoute);
                    setPackages(inProgressRoute.packages);
                }
            }
        } catch (error) {
            console.error('Error loading route:', error);
        } finally {
            setLoading(false);
        }
    };

    const isFromNavigation = !!storedRouteId;
    
    const handleStartRoute = () => {
        sessionStorage.removeItem('currentRouteId');
        navigate('/driver/navigation');
    };

    const handleBackToNavigation = () => {
        navigate('/driver/navigation');
    };

    return (
        <div className="route-overview">
            <DriverHeader navigate={() => navigate('/driver/dashboard')} />
            
            <div className="route-overview-content">
                {loading ? (
                    <div className="loading-message">Loading route...</div>
                ) : packages.length > 0 ? (
                    <div className="route-stops-container">
                        {packages.map((pkg, index) => (
                            <div key={pkg.id} className="route-stop-card shipping">
                                <div className="stop-status stop-status-top-right">
                                    <span className={`status-badge status-${pkg.status}`}>
                                        {pkg.status === 'delivered' ? '✓ Delivered' : 
                                         pkg.status === 'picked_up' ? '📦 Picked Up' : 
                                         '⏳ Pending'}
                                    </span>
                                </div>
                                <div className="stop-icon">
                                    <div className="house-icon">🏠</div>
                                </div>
                                <div className="stop-content">
                                    <div className="stop-type">Shipping</div>
                                    <div className="stop-name">{pkg.recipientName}</div>
                                    <div className="stop-address">{pkg.address}</div>
                                    <div className="stop-location">{pkg.city} {pkg.postalCode}</div>
                                    {pkg.deliveryInstructions && (
                                        <div className="stop-instructions">
                                            <small>Instructions: {pkg.deliveryInstructions}</small>
                                        </div>
                                    )}
                                </div>
                                <div className="stop-arrow">›</div>
                            </div>
                        ))}
                        
                        {isFromNavigation ? (
                            <button className="back-to-navigation-button" onClick={handleBackToNavigation}>
                                ← Back to Navigation
                            </button>
                        ) : (
                            <button className="start-route-button" onClick={handleStartRoute}>
                                Start
                            </button>
                        )}
                    </div>
                ) : (
                    <div className="no-route-message">
                        <h2>No packages found</h2>
                        <p>No packages are available for this route.</p>
                        <button 
                            className="back-to-dashboard-btn"
                            onClick={() => navigate('/driver/dashboard')}
                        >
                            Back to Dashboard
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default RouteOverview;