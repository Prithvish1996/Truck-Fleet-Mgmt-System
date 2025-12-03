import axios, { AxiosInstance } from 'axios';
import { apiConfig } from './apiConfig';

const axiosInstance: AxiosInstance = axios.create({
  baseURL: apiConfig.baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      return Promise.reject(error);
    } else if (error.request) {
      return Promise.reject(new Error('Network error: No response received'));
    } else {
      return Promise.reject(error);
    }
  }
);

export default axiosInstance;

