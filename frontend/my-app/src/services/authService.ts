import { apiConfig } from '../config/apiConfig';

const API_BASE_URL = apiConfig.baseURL;

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

class AuthService {
  private baseURL: string;

  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await fetch(`${this.baseURL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
        mode: 'cors',
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Login failed');
      }

      const apiResponse: ApiResponse<LoginResponse> = await response.json();
      
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Login failed');
      }

      if (apiResponse.data.email) {
        this.setUserEmail(apiResponse.data.email);
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  async logout(token: string): Promise<void> {
    try {
      const response = await fetch(`${this.baseURL}/auth/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Logout failed');
      }
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  }

  setToken(token: string): void {
    if (!token || token.trim() === '') {
      console.error('Attempted to store empty token');
      return;
    }
    const trimmedToken = token.trim();
    localStorage.setItem('authToken', trimmedToken);
    console.log('Token stored successfully. Length:', trimmedToken.length);
  }

  getToken(): string | null {
    const token = localStorage.getItem('authToken');
    if (token) {
      console.log('Token retrieved. Length:', token.length, 'First 50 chars:', token.substring(0, 50));
    } else {
      console.warn('No token found in localStorage');
    }
    return token;
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
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userId = payload.userID || payload.driverId || payload.userId || payload.id;
      
      if (userId !== null && userId !== undefined) {
        const numId = typeof userId === 'number' ? userId : parseInt(userId, 10);
        if (!isNaN(numId)) {
          return numId;
        }
      }
      
      console.warn('Driver ID not found in token. Token payload:', payload);
      console.warn('Available fields:', Object.keys(payload));
      return null;
    } catch (error) {
      console.error('Error parsing token:', error);
      return null;
    }
  }
}

export const authService = new AuthService();
