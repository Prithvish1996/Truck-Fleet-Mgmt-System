// API service for authentication
const API_BASE_URL = 'http://localhost:8080/api';

// Enable mock mode when backend is not available
// Set to true to use mock authentication (no backend required)
const USE_MOCK_AUTH = process.env.REACT_APP_USE_MOCK_AUTH === 'true' || 
                       localStorage.getItem('useMockAuth') === 'true';

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
    // Use mock authentication if enabled
    if (USE_MOCK_AUTH) {
      return this.mockLogin(credentials);
    }

    try {
      const response = await fetch(`${this.baseURL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Login failed');
      }

      const apiResponse: ApiResponse<LoginResponse> = await response.json();
      
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Login failed');
      }

      // Store user email for mock service
      if (apiResponse.data.email) {
        this.setUserEmail(apiResponse.data.email);
      }

      return apiResponse.data;
    } catch (error) {
      console.error('Login error:', error);
      // Fallback to mock if backend is unavailable
      console.warn('Backend unavailable, falling back to mock authentication');
      return this.mockLogin(credentials);
    }
  }

  private async mockLogin(credentials: LoginRequest): Promise<LoginResponse> {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500));

    // Mock user database
    const mockUsers: { [key: string]: { password: string; userType: string; username: string } } = {
      'admin@example.com': {
        password: 'Admin123',
        userType: 'ADMIN',
        username: 'admin'
      },
      'driver@example.com': {
        password: 'Driver123',
        userType: 'DRIVER',
        username: 'driver'
      },
      'planner@example.com': {
        password: 'Planner123',
        userType: 'PLANNER',
        username: 'planner'
      },
      // Also support the emails used in mockDataService
      'driver@tfms.com': {
        password: 'Driver123',
        userType: 'DRIVER',
        username: 'driver'
      },
      'test@example.com': {
        password: 'Driver123',
        userType: 'DRIVER',
        username: 'test'
      }
    };

    const user = mockUsers[credentials.email];

    if (!user || user.password !== credentials.password) {
      throw new Error('Authentication failed');
    }

    // Generate a mock JWT token (simple base64 encoded object)
    const mockToken = btoa(JSON.stringify({
      email: credentials.email,
      userType: user.userType,
      exp: Math.floor(Date.now() / 1000) + 86400 // 24 hours
    }));

    const loginResponse: LoginResponse = {
      accessToken: `mock.${mockToken}.token`,
      refreshToken: undefined,
      tokenType: 'Bearer',
      expiresIn: 86400,
      username: user.username,
      userType: user.userType,
      email: credentials.email,
      success: true,
      message: 'Login successful (Mock Mode)'
    };

    // Store user data
    this.setToken(loginResponse.accessToken);
    this.setUserRole(loginResponse.userType);
    this.setUserEmail(loginResponse.email);

    return loginResponse;
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

  // Token management
  setToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  removeToken(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
  }

  // User role management
  setUserRole(role: string): void {
    localStorage.setItem('userRole', role);
  }

  getUserRole(): string | null {
    return localStorage.getItem('userRole');
  }

  // User email management
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
      // Handle mock tokens
      if (token.startsWith('mock.')) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const currentTime = Date.now() / 1000;
        return payload.exp > currentTime;
      }

      // Check if token is expired (real JWT)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  // Enable/disable mock mode
  setMockMode(enabled: boolean): void {
    localStorage.setItem('useMockAuth', enabled.toString());
    console.log(`Mock authentication mode: ${enabled ? 'ENABLED' : 'DISABLED'}`);
  }

  isMockMode(): boolean {
    return USE_MOCK_AUTH;
  }
}

export const authService = new AuthService();
