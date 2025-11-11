class DateTimeService {
  convertTimeStringToDateTime(timeString: string | null | undefined): string {
    if (!timeString) {
      return new Date().toISOString();
    }

    try {
      // Check if the string is in "yyyy-MM-dd HH:mm:ss" format (from backend)
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(timeString)) {
        // Replace space with 'T' to make it ISO-like format
        const isoString = timeString.replace(' ', 'T');
        const parsedDate = new Date(isoString);
        
        if (!isNaN(parsedDate.getTime())) {
          return parsedDate.toISOString();
        } else {
          console.warn('Failed to parse datetime string, using current date/time:', timeString);
          return new Date().toISOString();
        }
      }

      // Handle old format: time-only string (e.g., "07:30.000")
      const timeParts = timeString.split('.');
      const timeOnly = timeParts[0];

      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const day = String(today.getDate()).padStart(2, '0');

      let fractionalSeconds = '';
      if (timeParts[1]) {
        fractionalSeconds = '.' + timeParts[1].substring(0, 3);
      }

      const dateTimeString = `${year}-${month}-${day}T${timeOnly}${fractionalSeconds}`;
      const parsedDate = new Date(dateTimeString);

      if (!isNaN(parsedDate.getTime())) {
        return parsedDate.toISOString();
      } else {
        console.warn('Failed to parse time string, using current date/time:', timeString);
        return new Date().toISOString();
      }
    } catch (error) {
      console.warn('Error parsing time string:', timeString, error);
      return new Date().toISOString();
    }
  }

  extractDate(dateTimeString: string | null | undefined): string {
    if (!dateTimeString) {
      return new Date().toISOString().split('T')[0];
    }

    try {
      // Handle ISO format (with 'T')
      if (dateTimeString.includes('T')) {
        return dateTimeString.split('T')[0];
      }
      
      // Handle "yyyy-MM-dd HH:mm:ss" format (with space)
      if (dateTimeString.includes(' ')) {
        return dateTimeString.split(' ')[0];
      }
      
      // If it's already just a date string (yyyy-MM-dd), return it
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateTimeString)) {
        return dateTimeString;
      }

      return new Date().toISOString().split('T')[0];
    } catch (error) {
      console.warn('Error extracting date:', dateTimeString, error);
      return new Date().toISOString().split('T')[0];
    }
  }

  convertTimeStringToDateTimeAndDate(timeString: string | null | undefined): {
    datetime: string;
    date: string;
  } {
    const datetime = this.convertTimeStringToDateTime(timeString);
    const date = this.extractDate(datetime);
    
    return { datetime, date };
  }

  formatTimeString(timeString: string | null | undefined): string {
    if (!timeString) {
      const now = new Date();
      return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    }

    try {
      // Check if the string is in "yyyy-MM-dd HH:mm:ss" format (from backend)
      if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(timeString)) {
        // Extract time part (HH:mm:ss) and take only hours and minutes
        const timePart = timeString.split(' ')[1];
        const [hours, minutes] = timePart.split(':');
        // Ensure proper formatting: pad hours and minutes to 2 digits
        return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
      }

      // Handle old format: time-only string (e.g., "07:30.000" or "07:30")
      const timeParts = timeString.split(':');
      if (timeParts.length >= 2) {
        const hours = String(timeParts[0]).padStart(2, '0');
        const minutes = String(timeParts[1].split('.')[0]).padStart(2, '0');
        return `${hours}:${minutes}`;
      }
      return new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
    } catch (error) {
      console.warn('Error formatting time string:', timeString, error);
      const now = new Date();
      return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    }
  }

  getRelativeDateLabel(dateString: string | null | undefined): string {
    if (!dateString) {
      return 'Today';
    }

    try {
      let dateOnly = dateString;
      if (dateString.includes('T')) {
        dateOnly = dateString.split('T')[0];
      } else if (dateString.includes(' ')) {
        dateOnly = dateString.split(' ')[0];
      }

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      
      const inputDate = new Date(dateOnly);
      inputDate.setHours(0, 0, 0, 0);

      if (inputDate.getTime() === today.getTime()) {
        return 'Today';
      } else if (inputDate.getTime() === tomorrow.getTime()) {
        return 'Tomorrow';
      } else {
        return inputDate.toLocaleDateString('en-US', { 
          month: 'short', 
          day: 'numeric', 
          year: 'numeric' 
        });
      }
    } catch (error) {
      console.warn('Error getting relative date label:', dateString, error);
      return 'Today';
    }
  }
}

export const dateTimeService = new DateTimeService();

