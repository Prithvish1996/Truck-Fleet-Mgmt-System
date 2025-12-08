import React from 'react';
import DriverHeader from '../../components/driverHeader';
import MapComponent from '../../components/navigation/MapComponent';
import DeliveryConfirmation from '../../components/navigation/DeliveryConfirmation';
import PackageInfo from '../../components/navigation/PackageInfo';
import WarehouseInfo from '../../components/navigation/WarehouseInfo';
import DepotInfo from '../../components/navigation/DepotInfo';
import DepotArrivalConfirmation from '../../components/navigation/DepotArrivalConfirmation';
import DeliveryNavigationControls from '../../components/navigation/DeliveryNavigationControls';
import RouteOverviewButton from '../../components/navigation/RouteOverviewButton';
import { Package, Route } from '../../../../types';
import { DeliveryState } from '../../../../services/deliveryService';

interface NavigationViewProps {
  userLocation: [number, number];
  currentDestination: [number, number];
  deliveryState: DeliveryState;
  currentPackage: Package | null;
  packages: Package[];
  currentRoute: Route | null;
  isCollectingWarehouse: boolean;
  isNavigatingToDepot: boolean;
  routeId?: string;
  onBack: () => void;
  onLocationUpdate: (location: [number, number]) => void;
  onOpenNavigation: () => void;
  onNavigateToRouteOverview: () => void;
  onDeliveryResult: (confirmed: boolean) => Promise<void>;
  onDepotArrival: (confirmed: boolean) => Promise<void>;
}

const NavigationView: React.FC<NavigationViewProps> = ({
  userLocation,
  currentDestination,
  deliveryState,
  currentPackage,
  packages,
  currentRoute,
  isCollectingWarehouse,
  isNavigatingToDepot,
  routeId,
  onBack,
  onLocationUpdate,
  onOpenNavigation,
  onNavigateToRouteOverview,
  onDeliveryResult,
  onDepotArrival,
}) => {
  return (
    <div className="package-delivery-navigation">
      <DriverHeader navigate={onBack} />
      <div className="package-delivery-navigation__content">
        <RouteOverviewButton onNavigate={onNavigateToRouteOverview} />
        
        {isCollectingWarehouse && currentRoute?.warehouse ? (
          <WarehouseInfo
            warehouse={currentRoute.warehouse}
            packageCount={packages.filter(pkg => pkg.status === 'pending').length}
          />
        ) : isNavigatingToDepot && currentRoute?.depot ? (
          <DepotInfo
            depot={currentRoute.depot}
          />
        ) : currentPackage && (
          <PackageInfo
            package={currentPackage}
            packageNumber={packages.findIndex(p => p.id === currentPackage.id) + 1}
            totalPackages={packages.length}
            estimatedTime={currentPackage.estimatedTravelTime}
          />
        )}

        <div className="package-delivery-navigation__map">
          <MapComponent
            userLocation={userLocation}
            destination={currentDestination}
            onLocationUpdate={onLocationUpdate}
            onRouteUpdate={() => {}}
            navigationMode={false}
          />
        </div>

        {deliveryState === 'showing_navigation' && (
          <DeliveryNavigationControls
            state={deliveryState}
            onOpenNavigation={onOpenNavigation}
          />
        )}

        {deliveryState === 'waiting_confirmation' && isNavigatingToDepot && currentRoute?.depot && (
          <DepotArrivalConfirmation
            depot={currentRoute.depot}
            onConfirm={onDepotArrival}
          />
        )}

        {deliveryState === 'waiting_confirmation' && !isCollectingWarehouse && !isNavigatingToDepot && currentPackage && (
          <DeliveryConfirmation
            package={currentPackage}
            onConfirm={onDeliveryResult}
          />
        )}
      </div>
    </div>
  );
};

export default NavigationView;

