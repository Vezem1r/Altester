import { BASE_API_URL, handleAuthError, createAuthAxios } from './apiUtils';

const API_URL = `${BASE_API_URL}/email`;

export const EmailService = {
  requestChange: async (email, password) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(`${API_URL}/request`, {
        email,
        password,
      });
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },

  confirmChange: async data => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(`${API_URL}/confirm`, data);
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },

  resendVerificationCode: async email => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(`${API_URL}/resend`, { email });
      return response.data;
    } catch (error) {
      throw new Error(handleAuthError(error));
    }
  },
};
