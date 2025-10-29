/**
 * Format seconds into a human-readable string
 * @param seconds - Time in seconds
 * @returns Formatted string like "12 min", "1 h 15 min", or "30 sec"
 */
export function formatTravelTime(seconds: number): string {
  if (seconds < 60) {
    return `${seconds} sec`;
  }

  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  if (minutes < 60) {
    if (remainingSeconds > 0) {
      return `${minutes} min ${remainingSeconds} sec`;
    }
    return `${minutes} min`;
  }

  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (remainingMinutes > 0) {
    return `${hours} h ${remainingMinutes} min`;
  }
  return `${hours} h`;
}

