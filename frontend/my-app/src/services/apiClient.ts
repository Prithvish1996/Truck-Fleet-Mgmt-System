import axios from 'axios';
import { apiConfig } from '../config/apiConfig';
import { authService } from './authService';


const apiClient = axios.create({
  baseURL: apiConfig.baseURL,
  withCredentials: true,
});


apiClient.interceptors.request.use((config: any) => {

  const token = authService.getToken();
  if (token) {
    config.headers = config.headers ?? {};
    config.headers['Authorization'] = `Bearer ${token.trim()}`;
  }

  
  if (
    !config.headers?.['Content-Type'] &&
    config.method &&
    ['post', 'put', 'patch'].includes(config.method.toLowerCase())
  ) {
    config.headers['Content-Type'] = 'application/json';
  }

  return config;
});

export default apiClient;
