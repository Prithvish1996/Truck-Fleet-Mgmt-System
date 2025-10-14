import { useNavigate } from "react-router-dom";
import DriverHeader from "../components/driverHeader";
import { useState, useEffect } from "react";
import { routeService } from "../../../services/routeService";
import { Route, RouteBreak } from "../../../types";
import "./RouteOverview.css";

interface RouteStop {
    id: string;
    type: 'shipping' | 'break';
    name: string;
    address: string;
    city: string;
    postalCode: string;
    duration?: string; // for breaks
    packagesBetween?: {
        beforePackage: string;
        afterPackage: string;
    };
}

function RouteOverview() {
    const navigate = useNavigate();
    const [currentRoute, setCurrentRoute] = useState<Route | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadCurrentRoute();
    }, []);

    const loadCurrentRoute = async () => {
        try {
            setLoading(true);
            const routes = await routeService.getDriverRoutes();
            // Find the route that's currently in progress
            const inProgressRoute = routes.find(route => route.status === 'in_progress');
            if (inProgressRoute) {
                setCurrentRoute(inProgressRoute);
            }
        } catch (error) {
            console.error('Error loading current route:', error);
        } finally {
            setLoading(false);
        }
    };

    // Generate route stops from packages and backend-provided breaks
    const generateRouteStops = (route: Route): RouteStop[] => {
        const stops: RouteStop[] = [];
        
        // Create a map of package IDs to their index for quick lookup
        const packageIndexMap = new Map<string, number>();
        route.packages.forEach((pkg, index) => {
            packageIndexMap.set(pkg.id, index);
        });
        
        // Create all stops (packages and breaks) with their positions
        const allStops: (RouteStop & { position: number })[] = [];
        
        // Add package stops
        route.packages.forEach((pkg) => {
            allStops.push({
                id: pkg.id,
                type: 'shipping',
                name: pkg.recipientName,
                address: pkg.address,
                city: pkg.city,
                postalCode: pkg.postalCode,
                position: packageIndexMap.get(pkg.id) || 0
            });
        });
        
        // Add breaks with their calculated positions
        route.breaks.forEach((breakItem) => {
            if (breakItem.packagesBetween) {
                const beforeIndex = packageIndexMap.get(breakItem.packagesBetween.beforePackage);
                const afterIndex = packageIndexMap.get(breakItem.packagesBetween.afterPackage);
                
                if (beforeIndex !== undefined && afterIndex !== undefined) {
                    // Position the break between the packages
                    const breakPosition = beforeIndex + 0.5; // Place break between packages
                    
                    allStops.push({
                        id: breakItem.id,
                        type: 'break',
                        name: breakItem.name,
                        address: breakItem.location?.address || '',
                        city: breakItem.location?.city || '',
                        postalCode: breakItem.location?.postalCode || '',
                        duration: breakItem.duration,
                        packagesBetween: breakItem.packagesBetween,
                        position: breakPosition
                    });
                }
            }
        });
        
        // Sort stops by position
        allStops.sort((a, b) => a.position - b.position);
        
        // Remove position property and return the ordered stops
        return allStops.map(({ position, ...stop }) => stop);
    };

    const handleStartRoute = () => {
        // Navigate to route execution or start the route
        console.log('Starting route...');
        // You can implement route starting logic here
    };

    return (
        <div className="route-overview">
            <DriverHeader navigate={() => navigate('/driver/dashboard')} />
            
            <div className="route-overview-content">
                {loading ? (
                    <div className="loading-message">Loading route...</div>
                ) : currentRoute ? (
                    <div className="route-stops-container">
                        {generateRouteStops(currentRoute).map((stop) => (
                            <div key={stop.id} className={`route-stop-card ${stop.type}`}>
                                <div className="stop-icon">
                                    {stop.type === 'shipping' ? (
                                        <div className="house-icon">🏠</div>
                                    ) : (
                                        <div className="break-icon">⏸</div>
                                    )}
                                </div>
                                <div className="stop-content">
                                    {stop.type === 'shipping' ? (
                                        <>
                                            <div className="stop-type">Shipping</div>
                                            <div className="stop-name">{stop.name}</div>
                                            <div className="stop-address">{stop.address}</div>
                                            <div className="stop-location">{stop.city} {stop.postalCode}</div>
                                        </>
                                    ) : (
                                        <>
                                            <div className="stop-type">Break</div>
                                            <div className="stop-name">{stop.name}</div>
                                            {stop.duration && (
                                                <div className="break-duration">Duration: {stop.duration}</div>
                                            )}
                                        </>
                                    )}
                                </div>
                                {stop.type === 'shipping' && (
                                    <div className="stop-arrow">›</div>
                                )}
                            </div>
                        ))}
                        
                        <button className="start-route-button" onClick={handleStartRoute}>
                            Start
                        </button>
                    </div>
                ) : (
                    <div className="no-route-message">
                        <h2>No active route found</h2>
                        <p>Start a route from the dashboard to see route details here.</p>
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