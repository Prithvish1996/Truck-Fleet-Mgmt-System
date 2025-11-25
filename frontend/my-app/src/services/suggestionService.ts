import axios from 'axios';
import { apiConfig } from '../config/apiConfig';
import { authService } from './authService';

export interface SuggestionResponse {
  success: boolean;
  message: string;
  data: string;
  timestamp?: string;
}

class SuggestionService {
  private baseURL: string;

  constructor() {
    this.baseURL = apiConfig.baseURL;
  }

  async submitSuggestion(suggestion: string): Promise<string> {
    try {
      const driverId = authService.getDriverId();
      if (!driverId) {
        throw new Error('Driver ID not found in token. Please log out and log back in to get a new token with your user ID.');
      }

      const token = authService.getToken();
      if (!token) {
        throw new Error('Authentication token not found');
      }

      const trimmedToken = token.trim();
      const trimmedSuggestion = suggestion.trim();

      if (!trimmedSuggestion) {
        throw new Error('Suggestion cannot be empty');
      }

      const response = await axios.post<SuggestionResponse>(
        `${this.baseURL}/driver/${driverId}/suggestion`,
        trimmedSuggestion,
        {
          headers: {
            'Authorization': `Bearer ${trimmedToken}`,
            'Content-Type': 'application/json',
          },
          withCredentials: true,
        }
      );

      const apiResponse = response.data;
      
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Failed to submit suggestion');
      }

      return apiResponse.message || 'Suggestion saved successfully';
    } catch (error) {
      console.error('Error submitting suggestion:', error);
      
      if (axios.isAxiosError(error)) {
        const errorMessage = error.response?.data?.message || `Failed to submit suggestion (Status: ${error.response?.status})`;
        
        if (error.response?.status === 401) {
          throw new Error(`${errorMessage}. Please log out and log back in to refresh your token.`);
        }
        
        throw new Error(errorMessage);
      }
      
      throw error;
    }
  }
}

export const suggestionService = new SuggestionService();

