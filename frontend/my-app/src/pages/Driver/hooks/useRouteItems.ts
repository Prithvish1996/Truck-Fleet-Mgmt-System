import { useMemo } from 'react';
import { Route, Package, RouteBreak } from '../../../types';

type RouteItem = 
  | { type: 'package'; data: Package } 
  | { type: 'break'; data: RouteBreak } 
  | { type: 'warehouse'; data: Route['warehouse'] } 
  | { type: 'depot'; data: Route['depot'] };

export function useRouteItems(
  route: Route | null,
  packages: Package[]
): RouteItem[] {
  return useMemo(() => {
    if (!route) return [];
    
    const items: RouteItem[] = [];
    const breaks = route.breaks || [];
    const usedBreaks = new Set<string>();
    
    if (route.warehouse) {
      items.push({ type: 'warehouse', data: route.warehouse });
    }
    
    packages.forEach((pkg) => {
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
    
    if (route.depot) {
      items.push({ type: 'depot', data: route.depot });
    }
    
    return items;
  }, [route, packages]);
}

