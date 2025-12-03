import { useNavigate, useLocation } from "react-router-dom";
import DriverHeader from "../components/driverHeader";
import { useState, useEffect, useMemo } from "react";
import { routeService } from "../../../services/routeService";
import { Route, Package, RouteBreak } from "../../../types";
import { formatTravelTime } from "../../../utils/timeFormatter";
import "./RouteOverview.css";

type RouteItem = { type: 'package'; data: Package } | { type: 'break'; data: RouteBreak } | { type: 'warehouse'; data: Route['warehouse'] } | { type: 'depot'; data: Route['depot'] };

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
        if (routeId) {
            sessionStorage.setItem('currentRouteId', routeId);
        }
        navigate('/driver/navigation', { state: { routeId } });
    };

    const handleBackToNavigation = () => {
        navigate('/driver/navigation');
    };

    const routeItems = useMemo(() => {
        if (!currentRoute) return [];
        
        const items: RouteItem[] = [];
        const breaks = currentRoute.breaks || [];
        const usedBreaks = new Set<string>();
        
        if (currentRoute.warehouse) {
            items.push({ type: 'warehouse', data: currentRoute.warehouse });
        }
        
        packages.forEach((pkg, index) => {
            items.push({ type: 'package', data: pkg });
            
            const breakAfterPackage = breaks.find(breakItem => 
                !usedBreaks.has(breakItem.id) &&
                breakItem.packagesBetween?.beforePackage === pkg.id
            );
            
            if (breakAfterPackage) {
                items.push({ type: 'break', data: breakAfterPackage });
                usedBreaks.add(breakAfterPackage.id);
            }
        });
        
        if (currentRoute.depot) {
            items.push({ type: 'depot', data: currentRoute.depot });
        }
        
        return items;
    }, [currentRoute, packages]);

    return (
        <div className="route-overview">
            <DriverHeader navigate={() => navigate('/driver/dashboard')} />
            
            <div className="route-overview-content">
                {loading ? (
                    <div className="loading-message">Loading route...</div>
                ) : packages.length > 0 ? (
                    <div className="route-stops-container">
                        {routeItems.map((item, index) => {
                            if (item.type === 'warehouse') {
                                const warehouse = item.data;
                                if (!warehouse) return null;
                                return (
                                    <div key={`warehouse-${warehouse.id}`} className="route-stop-card warehouse">
                                        <div className="stop-icon">
                                            <div className="warehouse-icon">📦</div>
                                        </div>
                                        <div className="stop-content">
                                            <div className="stop-type">Warehouse</div>
                                            <div className="stop-name">Package Collection</div>
                                            <div className="stop-address">{warehouse.address}</div>
                                            <div className="stop-location">{warehouse.city} {warehouse.postalCode}</div>
                                        </div>
                                        <div className="stop-arrow">›</div>
                                    </div>
                                );
                            } else if (item.type === 'depot') {
                                const depot = item.data;
                                if (!depot) return null;
                                return (
                                    <div key={`depot-${depot.id}`} className="route-stop-card depot">
                                        <div className="stop-icon">
                                            <div className="depot-icon">🏢</div>
                                        </div>
                                        <div className="stop-content">
                                            <div className="stop-type">Depot</div>
                                            <div className="stop-name">{depot.name || 'Return to Depot'}</div>
                                            <div className="stop-address">{depot.address}</div>
                                            <div className="stop-location">{depot.city} {depot.postalCode}</div>
                                        </div>
                                        <div className="stop-arrow">›</div>
                                    </div>
                                );
                            } else if (item.type === 'package') {
                                const pkg = item.data;
                                return (
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
                                            {pkg.estimatedTravelTime && (
                                                <div className="stop-travel-time">
                                                    <span className="travel-time-label">⏱️ Estimated travel time:</span>
                                                    <span className="travel-time-value">{formatTravelTime(pkg.estimatedTravelTime)}</span>
                                                </div>
                                            )}
                                            {pkg.deliveryInstructions && (
                                                <div className="stop-instructions">
                                                    <small>Instructions: {pkg.deliveryInstructions}</small>
                                                </div>
                                            )}
                                        </div>
                                        <div className="stop-arrow">›</div>
                                    </div>
                                );
                            } else {
                                const breakItem = item.data;
                                return (
                                    <div key={breakItem.id} className="route-stop-card break">
                                        <div className="stop-icon">
                                            <div className="break-icon">☕</div>
                                        </div>
                                        <div className="stop-content">
                                            <div className="stop-type">Break</div>
                                            <div className="stop-name">{breakItem.name}</div>
                                            {breakItem.scheduledTime && (
                                                <div className="stop-travel-time">
                                                    <span className="travel-time-label">🕐 Scheduled:</span>
                                                    <span className="travel-time-value">{breakItem.scheduledTime}</span>
                                                </div>
                                            )}
                                            {breakItem.duration && (
                                                <div className="stop-travel-time">
                                                    <span className="travel-time-label">⏱️ Duration:</span>
                                                    <span className="travel-time-value">{breakItem.duration}</span>
                                                </div>
                                            )}
                                        </div>
                                        <div className="stop-arrow">›</div>
                                    </div>
                                );
                            }
                        })}
                        
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