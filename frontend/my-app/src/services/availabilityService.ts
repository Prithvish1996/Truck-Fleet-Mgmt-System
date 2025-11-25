import { authService } from './authService';
import { apiConfig } from '../config/apiConfig';

export interface DriverAvailabilityResponseDto {
  id: number;
  availableAt: string;
  status: string;
}

export interface DriverResponseDto {
  id: number;
  userName: string;
  email: string;
  isAvailable: boolean;
  city?: string;
  address?: string;
  availability?: DriverAvailabilityResponseDto[];
  suggestions?: any[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

class AvailabilityService {
  private baseURL: string;

  constructor() {
    this.baseURL = apiConfig.baseURL;
  }

  async getDriverAvailability(driverId: number): Promise<DriverAvailabilityResponseDto[]> {
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      const response = await fetch(`${this.baseURL}/driver/get/${driverId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to fetch availability' }));
        throw new Error(errorData.message || `Failed to fetch availability (Status: ${response.status})`);
      }

      const apiResponse: ApiResponse<DriverResponseDto> = await response.json();

      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to fetch availability');
      }

      return apiResponse.data.availability || [];
    } catch (error) {
      console.error('Error fetching driver availability:', error);
      throw error;
    }
  }

  async createDriverAvailability(
    driverId: number,
    slots: Array<{ date: string; startTime: string; endTime: string }>
  ): Promise<string> {
    try {
      const token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      const zonedDateTimes: string[] = [];

      slots.forEach(slot => {
        const startDate = new Date(`${slot.date}T${slot.startTime}:00`);
        const endDate = new Date(`${slot.date}T${slot.endTime}:00`);

        let currentDate = new Date(startDate);
        while (currentDate < endDate) {
          const isoString = currentDate.toISOString();
          zonedDateTimes.push(isoString);
          currentDate = new Date(currentDate.getTime() + 60 * 60 * 1000);
        }
      });

      if (zonedDateTimes.length === 0) {
        throw new Error('No valid time slots to create');
      }

      const response = await fetch(`${this.baseURL}/driver/${driverId}/availability`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token.trim()}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(zonedDateTimes),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to create availability' }));
        throw new Error(errorData.message || `Failed to create availability (Status: ${response.status})`);
      }

      const apiResponse: ApiResponse<string> = await response.json();

      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to create availability');
      }

      return apiResponse.data || apiResponse.message;
    } catch (error) {
      console.error('Error creating driver availability:', error);
      throw error;
    }
  }

  convertAvailabilityToSlots(
    availability: DriverAvailabilityResponseDto[]
  ): { [date: string]: Array<{ id: string; date: string; startTime: string; endTime: string; isAvailable: boolean }> } {
    const slotsByDate: { [date: string]: Array<{ id: string; date: string; startTime: string; endTime: string; isAvailable: boolean }> } = {};

    const byDate: { [date: string]: DriverAvailabilityResponseDto[] } = {};
    availability.forEach(avail => {
      const date = new Date(avail.availableAt);
      const dateStr = date.toISOString().split('T')[0];
      if (!byDate[dateStr]) {
        byDate[dateStr] = [];
      }
      byDate[dateStr].push(avail);
    });

    Object.keys(byDate).forEach(dateStr => {
      const dateAvailabilities = byDate[dateStr]
        .map(avail => ({
          ...avail,
          time: new Date(avail.availableAt),
        }))
        .sort((a, b) => a.time.getTime() - b.time.getTime());

      if (dateAvailabilities.length === 0) {
        return;
      }

      const slots: Array<{ id: string; date: string; startTime: string; endTime: string; isAvailable: boolean }> = [];

      const hours = dateAvailabilities
        .map(avail => {
          const time = new Date(avail.availableAt);
          time.setMinutes(0, 0, 0);
          return {
            hour: time.getHours(),
            date: time,
            id: avail.id,
          };
        })
        .filter((value, index, self) => 
          index === self.findIndex(h => h.hour === value.hour && h.date.toDateString() === value.date.toDateString())
        )
        .sort((a, b) => a.hour - b.hour);

      type SlotInfo = { startHour: number; endHour: number; ids: number[] };
      let currentSlot: SlotInfo | null = null;

      hours.forEach((hourInfo) => {
        if (!currentSlot) {
          currentSlot = {
            startHour: hourInfo.hour,
            endHour: hourInfo.hour,
            ids: [hourInfo.id],
          };
        } else {
          if (hourInfo.hour === currentSlot.endHour + 1) {
            currentSlot.endHour = hourInfo.hour;
            currentSlot.ids.push(hourInfo.id);
          } else {
            const slotDate = new Date(dateStr);
            slotDate.setHours(currentSlot.startHour, 0, 0, 0);
            const endDate = new Date(dateStr);
            endDate.setHours(currentSlot.endHour + 1, 0, 0, 0);
            
            slots.push({
              id: `slot-${dateStr}-${currentSlot.startHour}-${currentSlot.endHour + 1}`,
              date: dateStr,
              startTime: this.formatTime(slotDate),
              endTime: this.formatTime(endDate),
              isAvailable: true,
            });
            currentSlot = {
              startHour: hourInfo.hour,
              endHour: hourInfo.hour,
              ids: [hourInfo.id],
            };
          }
        }
      });

      if (currentSlot !== null) {
        const finalSlot: SlotInfo = currentSlot;
        const slotDate = new Date(dateStr);
        slotDate.setHours(finalSlot.startHour, 0, 0, 0);
        const endDate = new Date(dateStr);
        endDate.setHours(finalSlot.endHour + 1, 0, 0, 0);
        
        slots.push({
          id: `slot-${dateStr}-${finalSlot.startHour}-${finalSlot.endHour + 1}`,
          date: dateStr,
          startTime: this.formatTime(slotDate),
          endTime: this.formatTime(endDate),
          isAvailable: true,
        });
      }

      slotsByDate[dateStr] = slots;
    });

    return slotsByDate;
  }

  private formatTime(date: Date): string {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }
}

export const availabilityService = new AvailabilityService();

