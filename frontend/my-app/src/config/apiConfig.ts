// Environment configuration for API endpoints
interface ApiConfig {
  baseURL: string;
  useHTTPS: boolean;
  allowSelfSignedCerts: boolean;
}

const getApiConfig = (): ApiConfig => {
  const isDevelopment = process.env.NODE_ENV === 'development';
  
  if (isDevelopment) {
    return {
      baseURL: 'https://localhost:8443/api', // Backend uses HTTPS on 8443
      useHTTPS: true,
      allowSelfSignedCerts: true // Allow self-signed certs in development
    };
  }
  
  // Production configuration
  return {
    baseURL: 'https://your-production-domain.com/api',
    useHTTPS: true,
    allowSelfSignedCerts: false
  };
};

export const apiConfig = getApiConfig();

// Helper function to create fetch options with proper configuration
export const createFetchOptions = (options: RequestInit = {}): RequestInit => {
  const config = getApiConfig();
  
  return {
    ...options,
    // Add any additional configuration based on environment
    mode: config.useHTTPS ? 'cors' : 'cors',
    credentials: 'include', // Include cookies if needed
  };
};
