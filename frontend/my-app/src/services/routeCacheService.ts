import { GraphhopperRoute } from './graphhopperService';

interface CacheEntry {
  route: GraphhopperRoute;
  timestamp: number;
  expiresAt: number;
}

interface RouteCacheConfig {
  maxAge: number; // Cache expiration time in milliseconds
  maxSize: number; // Maximum number of cached routes
}

class RouteCacheService {
  private cache: Map<string, CacheEntry> = new Map();
  private config: RouteCacheConfig;
  private lastDestination: [number, number] | null = null;

  constructor(config: RouteCacheConfig = { maxAge: 5 * 60 * 1000, maxSize: 50 }) {
    this.config = config;
  }

  /**
   * Create a cache key for a route
   */
  private createCacheKey(from: [number, number], to: [number, number]): string {
    // Round coordinates to 4 decimal places (~11 meters precision)
    const fromKey = `${from[0].toFixed(4)},${from[1].toFixed(4)}`;
    const toKey = `${to[0].toFixed(4)},${to[1].toFixed(4)}`;
    return `${fromKey}-${toKey}`;
  }

  /**
   * Check if destination has changed significantly
   */
  hasDestinationChanged(newDest: [number, number] | null): boolean {
    if (!this.lastDestination || !newDest) return true;
    
    const [lat1, lng1] = this.lastDestination;
    const [lat2, lng2] = newDest;
    const distance = Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lng2 - lng1, 2));
    
    // Consider destination changed if more than ~100 meters away
    return distance > 0.001;
  }

  /**
   * Update the last destination
   */
  updateDestination(destination: [number, number]): void {
    this.lastDestination = destination;
  }

  /**
   * Get cached route if available and not expired
   */
  getCachedRoute(from: [number, number], to: [number, number]): GraphhopperRoute | null {
    const cacheKey = this.createCacheKey(from, to);
    const entry = this.cache.get(cacheKey);

    if (!entry) {
      return null;
    }

    // Check if cache entry is expired
    if (Date.now() > entry.expiresAt) {
      this.cache.delete(cacheKey);
      return null;
    }

    console.log('Using cached route for:', cacheKey);
    return entry.route;
  }

  /**
   * Cache a route
   */
  cacheRoute(from: [number, number], to: [number, number], route: GraphhopperRoute): void {
    const cacheKey = this.createCacheKey(from, to);
    const now = Date.now();

    // Remove expired entries
    this.cleanupExpiredEntries();

    // If cache is full, remove oldest entry
    if (this.cache.size >= this.config.maxSize) {
      this.removeOldestEntry();
    }

    this.cache.set(cacheKey, {
      route,
      timestamp: now,
      expiresAt: now + this.config.maxAge
    });

    console.log('Cached route for:', cacheKey);
  }

  /**
   * Clear all cached routes
   */
  clearCache(): void {
    console.log('Clearing route cache');
    this.cache.clear();
  }

  /**
   * Clear cache when destination changes
   */
  clearCacheOnDestinationChange(newDest: [number, number] | null): void {
    if (this.hasDestinationChanged(newDest)) {
      console.log('Destination changed, clearing cache');
      this.clearCache();
      if (newDest) {
        this.updateDestination(newDest);
      }
    }
  }

  /**
   * Remove expired cache entries
   */
  private cleanupExpiredEntries(): void {
    const now = Date.now();
    const entries = Array.from(this.cache.entries());
    for (const [key, entry] of entries) {
      if (now > entry.expiresAt) {
        this.cache.delete(key);
      }
    }
  }

  /**
   * Remove the oldest cache entry
   */
  private removeOldestEntry(): void {
    let oldestKey = '';
    let oldestTimestamp = Date.now();

    const entries = Array.from(this.cache.entries());
    for (const [key, entry] of entries) {
      if (entry.timestamp < oldestTimestamp) {
        oldestTimestamp = entry.timestamp;
        oldestKey = key;
      }
    }

    if (oldestKey) {
      this.cache.delete(oldestKey);
    }
  }

  /**
   * Get cache statistics
   */
  getCacheStats(): { size: number; maxSize: number; entries: string[] } {
    return {
      size: this.cache.size,
      maxSize: this.config.maxSize,
      entries: Array.from(this.cache.keys())
    };
  }
}

// Export singleton instance
export const routeCacheService = new RouteCacheService();
