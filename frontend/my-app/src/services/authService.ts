import axios, { AxiosError } from 'axios';
import { apiConfig } from '../config/apiConfig';

const API_BASE_URL = apiConfig.baseURL;
const isDevelopment = process.env.NODE_ENV === 'development';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  userType: string;
  email: string;
  success: boolean;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export class AuthenticationError extends Error {
  constructor(message: string, public readonly statusCode?: number) {
    super(message);
    this.name = 'AuthenticationError';
    Object.setPrototypeOf(this, AuthenticationError.prototype);
  }
}

export class NetworkError extends Error {
  constructor(message: string, public readonly originalError?: unknown) {
    super(message);
    this.name = 'NetworkError';
    Object.setPrototypeOf(this, NetworkError.prototype);
  }
}

export class ValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'ValidationError';
    Object.setPrototypeOf(this, ValidationError.prototype);
  }
}

export class ServerError extends Error {
  constructor(message: string, public readonly statusCode?: number) {
    super(message);
    this.name = 'ServerError';
    Object.setPrototypeOf(this, ServerError.prototype);
  }
}

class AuthService {
  private baseURL: string;

  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    if (!credentials.email || !credentials.email.trim()) {
      throw new ValidationError('Email is required');
    }

    if (!credentials.password || !credentials.password.trim()) {
      throw new ValidationError('Password is required');
    }

    try {
      const response = await axios.post<ApiResponse<LoginResponse>>(
        `${this.baseURL}/auth/login`,
        credentials,
        {
          headers: {
            'Content-Type': 'application/json',
          },
          withCredentials: true,
          timeout: 30000,
        }
      );

      const apiResponse = response.data;
      
      if (!apiResponse.success) {
        const errorMessage = apiResponse.message || 'Invalid email or password';
        throw new AuthenticationError(errorMessage, response.status);
      }

      if (apiResponse.data.email) {
        this.setUserEmail(apiResponse.data.email);
      }

      return apiResponse.data;
    } catch (error: unknown) {
      if (error instanceof AuthenticationError || error instanceof ValidationError) {
        throw error;
      }

      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError<{ message?: string; error?: string }>;
        const statusCode = axiosError.response?.status;
        const errorData = axiosError.response?.data;

        if (statusCode === 401) {
          const message = errorData?.message || 'Invalid email or password';
          if (isDevelopment) {
            console.error('Authentication failed:', { statusCode, errorData });
          }
          throw new AuthenticationError(message, statusCode);
        }

        if (statusCode === 400) {
          const message = errorData?.message || 'Invalid request. Please check your credentials.';
          throw new ValidationError(message);
        }

        if (statusCode === 403) {
          const message = errorData?.message || 'Access denied. Your account may be restricted.';
          throw new AuthenticationError(message, statusCode);
        }

        if (statusCode === 429) {
          const message = 'Too many login attempts. Please try again later.';
          throw new AuthenticationError(message, statusCode);
        }

        if (statusCode && statusCode >= 500) {
          const message = 'Server error. Please try again later.';
          if (isDevelopment) {
            console.error('Server error during login:', { statusCode, errorData, error: axiosError });
          }
          throw new ServerError(message, statusCode);
        }

        if (axiosError.code === 'ERR_NETWORK' || axiosError.code === 'ECONNABORTED') {
          const message = isDevelopment
            ? 'Unable to connect to the server. Please check your connection and try again.'
            : 'Connection failed. Please check your internet connection and try again.';
          if (isDevelopment) {
            console.error('Network error:', axiosError);
          }
          throw new NetworkError(message, axiosError);
        }

        if (axiosError.code === 'ERR_CERT_AUTHORITY_INVALID' || 
            axiosError.message?.includes('certificate')) {
          const message = isDevelopment
            ? 'SSL certificate error. Please visit the server URL directly to accept the certificate, then try again.'
            : 'Security certificate error. Please contact support if this issue persists.';
          if (isDevelopment) {
            console.error('SSL certificate error:', axiosError);
          }
          throw new NetworkError(message, axiosError);
        }

        const message = errorData?.message || errorData?.error || 'Login failed. Please try again.';
        if (isDevelopment) {
          console.error('Unexpected login error:', { statusCode, errorData, error: axiosError });
        }
        throw new AuthenticationError(message, statusCode);
      }

      if (error instanceof Error) {
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
          const message = isDevelopment
            ? 'Unable to connect to the server. Please check your connection and try again.'
            : 'Connection failed. Please check your internet connection and try again.';
          throw new NetworkError(message, error);
        }
      }

      const message = 'An unexpected error occurred. Please try again.';
      if (isDevelopment) {
        console.error('Unknown login error:', error);
      }
      throw new Error(message);
    }
  }

  async logout(token: string): Promise<void> {
    if (!token || !token.trim()) {
      this.removeToken();
      return;
    }

    try {
      await axios.post(
        `${this.baseURL}/auth/logout`,
        {},
        {
          headers: {
            'Authorization': `Bearer ${token.trim()}`,
            'Content-Type': 'application/json',
          },
          withCredentials: true,
          timeout: 10000,
        }
      );
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        const axiosError = error as AxiosError;
        
        if (axiosError.code === 'ERR_NETWORK' || 
            axiosError.code === 'ERR_CERT_AUTHORITY_INVALID' ||
            axiosError.code === 'ECONNABORTED') {
          if (isDevelopment) {
            console.warn('Logout request failed due to network issue, continuing with local logout:', axiosError);
          }
          return;
        }

        const statusCode = axiosError.response?.status;
        if (statusCode === 401 || statusCode === 403) {
          if (isDevelopment) {
            console.warn('Logout request failed due to authentication issue, continuing with local logout');
          }
          return;
        }

        if (statusCode && statusCode >= 500) {
          if (isDevelopment) {
            console.error('Server error during logout:', { statusCode, error: axiosError });
          }
          return;
        }

        if (isDevelopment) {
          console.warn('Logout request failed, continuing with local logout:', { statusCode, error: axiosError });
        }
        return;
      }

      if (isDevelopment) {
        console.warn('Unknown error during logout, continuing with local logout:', error);
      }
    } finally {
      this.removeToken();
    }
  }

  setToken(token: string): void {
    if (!token || token.trim() === '') {
      if (isDevelopment) {
        console.error('Attempted to store empty token');
      }
      return;
    }
    const trimmedToken = token.trim();
    try {
      localStorage.setItem('authToken', trimmedToken);
      if (isDevelopment) {
        console.log('Token stored successfully');
      }
    } catch (error) {
      if (isDevelopment) {
        console.error('Failed to store token in localStorage:', error);
      }
      throw new Error('Failed to save authentication token. Please check your browser settings.');
    }
  }

  getToken(): string | null {
    try {
      const token = localStorage.getItem('authToken');
      return token;
    } catch (error) {
      if (isDevelopment) {
        console.error('Failed to retrieve token from localStorage:', error);
      }
      return null;
    }
  }

  removeToken(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
  }

  setUserRole(role: string): void {
    localStorage.setItem('userRole', role);
  }

  getUserRole(): string | null {
    return localStorage.getItem('userRole');
  }

  setUserEmail(email: string): void {
    localStorage.setItem('userEmail', email);
  }

  getUserEmail(): string | null {
    return localStorage.getItem('userEmail');
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  getDriverId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        if (isDevelopment) {
          console.warn('Invalid token format: expected JWT with 3 parts');
        }
        return null;
      }

      const payload = JSON.parse(atob(parts[1]));
      const userId = payload.userID || payload.driverId || payload.userId || payload.id;
      
      if (userId !== null && userId !== undefined) {
        const numId = typeof userId === 'number' ? userId : parseInt(String(userId), 10);
        if (!isNaN(numId) && numId > 0) {
          return numId;
        }
      }
      
      if (isDevelopment) {
        console.warn('Driver ID not found in token payload. Available fields:', Object.keys(payload));
      }
      return null;
    } catch (error) {
      if (isDevelopment) {
        console.error('Error parsing token:', error);
      }
      return null;
    }
  }
}

export const authService = new AuthService();
