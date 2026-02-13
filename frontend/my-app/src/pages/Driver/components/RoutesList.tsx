import { Route } from '../../../types';
import RouteCard from './RouteCard';

interface RoutesListProps {
  routes: Route[];
  startRoute: (routeId: string) => void;
  showOtherRoutes: boolean;
  onToggleOtherRoutes: () => void;
}

export default function RoutesList({ 
  routes, 
  startRoute, 
  showOtherRoutes, 
  onToggleOtherRoutes 
}: RoutesListProps) {
  const ongoingRoutes = routes.filter(route => 
    route.status === 'parcels_retrieved'
  );
  const scheduledRoutes = routes.filter(route => 
    route.status === 'scheduled'
  );
  const otherRoutes = routes.filter(route => 
    route.status !== 'scheduled' && route.status !== 'parcels_retrieved'
  );

  return (
    <>
      {ongoingRoutes.length > 0 && (
        <div className="routes-section">
          <h2 className="routes-section-title">Ongoing Routes</h2>
          {ongoingRoutes.map((route) => (
            <RouteCard
              key={route.id}
              startRoute={startRoute}
              routeId={route.id}
              truckId={route.truckId}
              packages={route.packages.length}
              startTime={route.startTime}
              duration={route.duration}
              date={route.date}
              status={route.status}
            />
          ))}
        </div>
      )}
      {(scheduledRoutes.length > 0 || otherRoutes.length > 0) && (
        <div className="routes-section">
          {ongoingRoutes.length > 0 ? (
            <>
              <button 
                className="routes-section-toggle"
                onClick={onToggleOtherRoutes}
              >
                <h2 className="routes-section-title">
                  {showOtherRoutes ? '▼' : '▶'} Other Routes
                </h2>
              </button>
              {showOtherRoutes && (
                <>
                  {scheduledRoutes.map((route) => (
                    <RouteCard
                      key={route.id}
                      startRoute={startRoute}
                      routeId={route.id}
                      truckId={route.truckId}
                      packages={route.packages.length}
                      startTime={route.startTime}
                      duration={route.duration}
                      date={route.date}
                      status={route.status}
                    />
                  ))}
                  {otherRoutes.map((route) => (
                    <RouteCard
                      key={route.id}
                      startRoute={startRoute}
                      routeId={route.id}
                      truckId={route.truckId}
                      packages={route.packages.length}
                      startTime={route.startTime}
                      duration={route.duration}
                      date={route.date}
                      status={route.status}
                    />
                  ))}
                </>
              )}
            </>
          ) : (
            <>
              {scheduledRoutes.length > 0 && (
                <h2 className="routes-section-title">Scheduled Routes</h2>
              )}
              {scheduledRoutes.map((route) => (
                <RouteCard
                  key={route.id}
                  startRoute={startRoute}
                  routeId={route.id}
                  truckId={route.truckId}
                  packages={route.packages.length}
                  startTime={route.startTime}
                  duration={route.duration}
                  date={route.date}
                  status={route.status}
                />
              ))}
              {otherRoutes.map((route) => (
                <RouteCard
                  key={route.id}
                  startRoute={startRoute}
                  routeId={route.id}
                  truckId={route.truckId}
                  packages={route.packages.length}
                  startTime={route.startTime}
                  duration={route.duration}
                  date={route.date}
                  status={route.status}
                />
              ))}
            </>
          )}
        </div>
      )}
    </>
  );
}

