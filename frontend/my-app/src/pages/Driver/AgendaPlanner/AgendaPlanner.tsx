import { useState, useEffect } from 'react';
import './AgendaPlanner.css';

//TODO: This is a mock component for the agenda planner. This needs to be refactored to use the backend and actual data.

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

export default function AgendaPlanner() {
  const [currentWeek, setCurrentWeek] = useState(new Date());
  const [availability, setAvailability] = useState<WeeklyAvailability>({});
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newSlot, setNewSlot] = useState({
    date: new Date().toISOString().split('T')[0],
    startTime: '09:00',
    endTime: '17:00'
  });
  const [viewMode, setViewMode] = useState<'weekly' | 'daily'>('weekly');

  useEffect(() => {
    loadAvailability();
  }, [currentWeek]);

  const loadAvailability = async () => {
    setLoading(true);
    // Mock data - in real app, this would fetch from backend
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

  const getWeekDatesWithInfo = (date: Date) => {
    const dates = getWeekDates(date);
    return dates.map(dateStr => {
      const date = new Date(dateStr);
      return {
        date: dateStr,
        dayName: date.toLocaleDateString('en-US', { weekday: 'short' }),
        dayNumber: date.getDate(),
        monthName: date.toLocaleDateString('en-US', { month: 'short' })
      };
    });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
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

  const addTimeSlot = (date?: string) => {
    setNewSlot(prev => ({ 
      ...prev, 
      date: date || new Date().toISOString().split('T')[0] 
    }));
    setShowAddForm(true);
  };

  const handleNewSlotChange = (field: 'date' | 'startTime' | 'endTime', value: string) => {
    setNewSlot(prev => ({ ...prev, [field]: value }));
  };

  const saveNewSlot = () => {
    if (newSlot.startTime >= newSlot.endTime) {
      alert('End time must be after start time');
      return;
    }

    const slot: AvailabilitySlot = {
      id: `${newSlot.date}-${Date.now()}`,
      date: newSlot.date,
      startTime: newSlot.startTime,
      endTime: newSlot.endTime,
      isAvailable: true
    };
    
    setAvailability(prev => ({
      ...prev,
      [newSlot.date]: [...(prev[newSlot.date] || []), slot]
    }));
    setShowAddForm(false);
    setNewSlot({ 
      date: new Date().toISOString().split('T')[0],
      startTime: '09:00', 
      endTime: '17:00' 
    });
  };

  const cancelAddSlot = () => {
    setShowAddForm(false);
    setNewSlot({ 
      date: new Date().toISOString().split('T')[0],
      startTime: '09:00', 
      endTime: '17:00' 
    });
  };

  const removeTimeSlot = (slotId: string, date: string) => {
    setAvailability(prev => ({
      ...prev,
      [date]: prev[date]?.filter(slot => slot.id !== slotId) || []
    }));
  };

  const navigateWeek = (direction: 'prev' | 'next') => {
    const newWeek = new Date(currentWeek);
    newWeek.setDate(currentWeek.getDate() + (direction === 'next' ? 7 : -7));
    setCurrentWeek(newWeek);
  };

  const getTotalAvailableSlots = () => {
    return Object.values(availability).flat().filter(slot => slot.isAvailable).length;
  };

  const getAvailableSlotsForDate = (date: string) => {
    return availability[date]?.filter(slot => slot.isAvailable) || [];
  };

  const saveAvailability = async () => {
    setLoading(true);
    // Mock save - in real app, this would send to backend
    console.log('Saving availability:', availability);
    setTimeout(() => {
      setLoading(false);
      alert('Availability saved successfully!');
    }, 1000);
  };

  const weekDates = getWeekDatesWithInfo(currentWeek);

  return (
    <div className="agenda-planner">
      <div className="agenda-header">
        <div className="header-content">
          <h2>Weekly Availability Planner</h2>
          <p className="header-subtitle">Set your availability for the work week (Monday - Saturday)</p>
        </div>
        <div className="view-controls">
          <div className="week-navigation">
            <button 
              className="nav-btn prev" 
              onClick={() => navigateWeek('prev')}
              title="Previous week"
            >
              ←
            </button>
            <span className="week-display">
              {currentWeek.toLocaleDateString('en-US', { 
                month: 'long', 
                year: 'numeric' 
              })}
            </span>
            <button 
              className="nav-btn next" 
              onClick={() => navigateWeek('next')}
              title="Next week"
            >
              →
            </button>
          </div>
          <button 
            className="add-availability-btn"
            onClick={() => addTimeSlot()}
          >
            <span>+</span> Add Availability
          </button>
        </div>
      </div>

      {showAddForm && (
        <div className="add-availability-modal">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Add New Availability</h3>
              <button className="close-btn" onClick={cancelAddSlot}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="availability-date">Date:</label>
                  <input
                    id="availability-date"
                    type="date"
                    value={newSlot.date}
                    onChange={(e) => handleNewSlotChange('date', e.target.value)}
                    className="form-input"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="start-time">Start Time:</label>
                  <input
                    id="start-time"
                    type="time"
                    value={newSlot.startTime}
                    onChange={(e) => handleNewSlotChange('startTime', e.target.value)}
                    className="form-input"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="end-time">End Time:</label>
                  <input
                    id="end-time"
                    type="time"
                    value={newSlot.endTime}
                    onChange={(e) => handleNewSlotChange('endTime', e.target.value)}
                    className="form-input"
                  />
                </div>
              </div>
            </div>
            <div className="modal-actions">
              <button className="cancel-btn" onClick={cancelAddSlot}>
                Cancel
              </button>
              <button className="save-btn" onClick={saveNewSlot}>
                <span>✓</span> Add Availability
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="weekly-overview">
        <div className="week-grid">
          {weekDates.map((dayInfo) => {
            const daySlots = availability[dayInfo.date] || [];
            const availableSlots = getAvailableSlotsForDate(dayInfo.date);
            const isToday = dayInfo.date === new Date().toISOString().split('T')[0];
            
            return (
              <div key={dayInfo.date} className={`day-card ${isToday ? 'today' : ''}`}>
                <div className="day-header">
                  <div className="day-info">
                    <div className="day-name">{dayInfo.dayName}</div>
                    <div className="day-number">{dayInfo.dayNumber}</div>
                    <div className="day-month">{dayInfo.monthName}</div>
                  </div>
                  <div className="day-status">
                    {availableSlots.length > 0 ? (
                      <span className="status-available">
                        {availableSlots.length} slot{availableSlots.length !== 1 ? 's' : ''}
                      </span>
                    ) : (
                      <span className="status-unavailable">No slots</span>
                    )}
                  </div>
                </div>
                
                <div className="day-slots">
                  {daySlots.length === 0 ? (
                    <div className="empty-day">
                      <div className="empty-icon">⏰</div>
                      <p>No availability</p>
                      <button 
                        className="add-slot-btn-small"
                        onClick={() => addTimeSlot(dayInfo.date)}
                      >
                        + Add
                      </button>
                    </div>
                  ) : (
                    <div className="slots-list">
                      {daySlots.map((slot, index) => (
                        <div key={slot.id} className={`time-slot ${slot.isAvailable ? 'available' : 'unavailable'}`}>
                          <div className="slot-time">
                            <span className="time-range">{slot.startTime} - {slot.endTime}</span>
                          </div>
                          <div className="slot-actions">
                            <button 
                              className={`toggle-btn ${slot.isAvailable ? 'available' : 'unavailable'}`}
                              onClick={() => toggleAvailability(slot.id, dayInfo.date)}
                              title={slot.isAvailable ? 'Mark unavailable' : 'Mark available'}
                            >
                              {slot.isAvailable ? '✓' : '✗'}
                            </button>
                            <button 
                              className="remove-btn"
                              onClick={() => removeTimeSlot(slot.id, dayInfo.date)}
                              title="Remove time slot"
                            >
                              🗑
                            </button>
                          </div>
                        </div>
                      ))}
                      <button 
                        className="add-slot-btn-small"
                        onClick={() => addTimeSlot(dayInfo.date)}
                      >
                        + Add More
                      </button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {getTotalAvailableSlots() > 0 && (
        <div className="save-section">
          <div className="save-info">
            <p>You have {getTotalAvailableSlots()} available time slots this week</p>
          </div>
          <button 
            className={`save-btn ${loading ? 'loading' : ''}`}
            onClick={saveAvailability}
            disabled={loading}
          >
            {loading ? (
              <>
                <div className="btn-spinner"></div>
                Saving...
              </>
            ) : (
              <>
                <span>✓</span>
                Save Weekly Availability
              </>
            )}
          </button>
        </div>
      )}
    </div>
  );
}
