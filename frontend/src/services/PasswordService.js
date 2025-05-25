import axios from 'axios';
import { BASE_API_URL, handleAuthError } from './apiUtils';

const API_URL = `${BASE_API_URL}/password`;

export const PasswordService = {
  requestReset: async email => {
    try {
      const response = await axios.post(`${API_URL}/request?email=${email}`);
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },

  confirmReset: async data => {
    try {
      const response = await axios.post(`${API_URL}/confirm`, data);
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },

  resendResetCode: async email => {
    try {
      const response = await axios.post(`${API_URL}/resend?email=${email}`);
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },
};
