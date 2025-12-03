import { useState, useEffect, useMemo } from 'react';

interface AvailabilitySlot {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  isAvailable: boolean;
}

interface WeeklyAvailability {
  [date: string]: AvailabilitySlot[];
}

interface UseAvailabilityReturn {
  availability: WeeklyAvailability;
  loading: boolean;
  currentWeek: Date;
  setCurrentWeek: (date: Date) => void;
  toggleAvailability: (slotId: string, date: string) => void;
  addTimeSlot: (slot: AvailabilitySlot) => void;
  removeTimeSlot: (slotId: string, date: string) => void;
  getTotalAvailableSlots: () => number;
  getAvailableSlotsForDate: (date: string) => AvailabilitySlot[];
}

export function useAvailability(): UseAvailabilityReturn {
  const [currentWeek, setCurrentWeek] = useState(new Date());
  const [availability, setAvailability] = useState<WeeklyAvailability>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadAvailability();
  }, [currentWeek]);

  const loadAvailability = async () => {
    setLoading(true);
    const weekDates = getWeekDates(currentWeek);
    const mockAvailability: WeeklyAvailability = {};
    
    weekDates.forEach(date => {
      mockAvailability[date] = [
        { id: `${date}-1`, date, startTime: '08:00', endTime: '12:00', isAvailable: true },
        { id: `${date}-2`, date, startTime: '13:00', endTime: '17:00', isAvailable: true },
      ];
    });
    
    setAvailability(mockAvailability);
    setLoading(false);
  };

  const getWeekDates = (date: Date): string[] => {
    const startOfWeek = new Date(date);
    startOfWeek.setDate(date.getDate() - date.getDay());
    const dates: string[] = [];
    
    for (let i = 1; i < 7; i++) {
      const currentDate = new Date(startOfWeek);
      currentDate.setDate(startOfWeek.getDate() + i);
      dates.push(currentDate.toISOString().split('T')[0]);
    }
    
    return dates;
  };

  const toggleAvailability = (slotId: string, date: string) => {
    setAvailability(prev => ({
      ...prev,
      [date]: prev[date]?.map(slot => 
        slot.id === slotId 
          ? { ...slot, isAvailable: !slot.isAvailable }
          : slot
      ) || []
    }));
  };

  const addTimeSlot = (slot: AvailabilitySlot) => {
    setAvailability(prev => ({
      ...prev,
      [slot.date]: [...(prev[slot.date] || []), slot]
    }));
  };

  const removeTimeSlot = (slotId: string, date: string) => {
    setAvailability(prev => ({
      ...prev,
      [date]: prev[date]?.filter(slot => slot.id !== slotId) || []
    }));
  };

  const getTotalAvailableSlots = () => {
    return Object.values(availability).flat().filter(slot => slot.isAvailable).length;
  };

  const getAvailableSlotsForDate = (date: string) => {
    return availability[date]?.filter(slot => slot.isAvailable) || [];
  };

  return {
    availability,
    loading,
    currentWeek,
    setCurrentWeek,
    toggleAvailability,
    addTimeSlot,
    removeTimeSlot,
    getTotalAvailableSlots,
    getAvailableSlotsForDate,
  };
}

