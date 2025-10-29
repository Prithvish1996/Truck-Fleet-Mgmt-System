import { Route, Package, RouteBreak } from '../types';

interface BreakRequirement {
  required: boolean;
  duration: number;
  mustBeBefore: number;
}

function calculateCumulativeTimes(
  packages: Package[],
  startTime: string
): { package: Package; cumulativeSeconds: number; scheduledTime: string }[] {
  const [startHours, startMinutes] = startTime.split(':').map(Number);
  let cumulativeSeconds = 0;
  
  return packages.map((pkg, index) => {
    const cumulativeTime = cumulativeSeconds;
    const travelTime = pkg.estimatedTravelTime || 0;
    
    // Add delivery time (assume 5 minutes per delivery)
    const deliveryTime = 300;
    
    cumulativeSeconds += travelTime + deliveryTime;
    
    const totalMinutes = startMinutes + Math.floor(cumulativeTime / 60);
    const totalHours = startHours + Math.floor(totalMinutes / 60);
    const finalMinutes = totalMinutes % 60;
    const finalHours = totalHours % 24;
    const scheduledTime = `${String(finalHours).padStart(2, '0')}:${String(finalMinutes).padStart(2, '0')}`;
    
    return {
      package: pkg,
      cumulativeSeconds: cumulativeTime,
      scheduledTime
    };
  });
}


function calculateBreakRequirements(
  cumulativeSeconds: number,
  previousBreaks: number[]
): BreakRequirement {
  const fourAndHalfHours = 4.5 * 3600;
  const sixHours = 6 * 3600;
  const totalBreakTime = previousBreaks.reduce((sum, duration) => sum + duration, 0);
  
  if (cumulativeSeconds >= sixHours && totalBreakTime < 2700) {
    return {
      required: true,
      duration: 2700,
      mustBeBefore: cumulativeSeconds + 1800
    };
  }
  
  if (cumulativeSeconds >= fourAndHalfHours - 1800 && totalBreakTime === 0) {
    return {
      required: true,
      duration: 2700,
      mustBeBefore: fourAndHalfHours
    };
  }
  
  if (totalBreakTime === 900 && cumulativeSeconds < fourAndHalfHours) {
    return {
      required: true,
      duration: 900,
      mustBeBefore: fourAndHalfHours
    };
  }
  
  return {
    required: false,
    duration: 0,
    mustBeBefore: 0
  };
}

export function scheduleBreaksForRoute(
  route: Omit<Route, 'breaks'> & { breaks?: RouteBreak[] }
): RouteBreak[] {
  const scheduledBreaks: RouteBreak[] = [];
  const packages = route.packages;
  const startTime = route.startTime;
  
  if (packages.length === 0) {
    return scheduledBreaks;
  }
  
  const packageTimes = calculateCumulativeTimes(packages, startTime);
  
  let breakCounter = 1;
  const breakDurations: number[] = [];
  
  packageTimes.forEach(({ package: pkg, cumulativeSeconds, scheduledTime }, index) => {
    const requirement = calculateBreakRequirements(cumulativeSeconds, breakDurations);
    
    if (requirement.required) {
      if (index < packages.length - 1) {
        const nextPackage = packages[index + 1];
        
        const breakItem: RouteBreak = {
          id: `break-${route.id}-${breakCounter}`,
          type: 'break',
          name: requirement.duration === 2700 ? 'Mandatory Break' : 'Coffee Break',
          duration: `${Math.floor(requirement.duration / 60)} min`,
          scheduledTime: scheduledTime,
          packagesBetween: {
            beforePackage: pkg.id,
            afterPackage: nextPackage.id
          },
          location: {
            latitude: pkg.latitude,
            longitude: pkg.longitude,
            address: pkg.address,
            city: pkg.city,
            postalCode: pkg.postalCode
          }
        };
        
        scheduledBreaks.push(breakItem);
        breakDurations.push(requirement.duration);
        breakCounter++;
      }
    }
  });
  
  return scheduledBreaks;
}

export function processRouteWithBreaks(route: Omit<Route, 'breaks'>): Route {
  const breaks = scheduleBreaksForRoute(route);
  return {
    ...route,
    breaks
  };
}

