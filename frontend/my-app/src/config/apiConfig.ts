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
      baseURL: 'https://localhost:8443/api',
      useHTTPS: true,
      allowSelfSignedCerts: true
    };
  }
  
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
    mode: config.useHTTPS ? 'cors' : 'cors',
    credentials: 'include',
  };
};
