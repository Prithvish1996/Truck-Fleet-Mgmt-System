import { ParcelResponse, RouteResponse } from '../services/plannerService';

export const formatDate = (dateString: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = monthNames[date.getMonth()];
  const day = date.getDate();
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${month} ${day}, ${hours}:${minutes}`;
};

export const formatDateOnly = (dateString: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = monthNames[date.getMonth()];
  const day = date.getDate();
  return `${month} ${day}`;
};

export const extractParcelId = (id: string): number => {
  const match = id.match(/P(\d+)/);
  return match ? parseInt(match[1], 10) : parseInt(id, 10) || 0;
};

export const formatParcelId = (parcelId: number, index?: number): string => {
  if (index !== undefined) {
    return `P${parcelId}-${String(index + 1).padStart(2, '0')}`;
  }
  return `P${parcelId}`;
};

export const countParcelsInRoute = (route: RouteResponse): number => {
  if (!route.routeStops || route.routeStops.length === 0) return 0;
  return route.routeStops.reduce((count, stop) => {
    return count + (stop.parcelsToDeliver?.length || 0);
  }, 0);
};

export const getFullDeliveryAddress = (parcel: ParcelResponse): string => {
  const parts = [];
  if (parcel.deliveryAddress) parts.push(parcel.deliveryAddress);
  if (parcel.deliveryPostalCode) parts.push(parcel.deliveryPostalCode);
  if (parcel.deliveryCity) parts.push(parcel.deliveryCity);
  return parts.length > 0 ? parts.join(', ') : 'Address not available';
};

