import { BASE_API_URL, createAuthAxios, handleApiError } from './apiUtils';

const API_URL = `${BASE_API_URL}/prompts`;

export const PromptService = {
  // Get all prompts (admin only)
  getAllPrompts: async (page = 0, size = 20) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}?page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getMyPrompts: async (page = 0, size = 20) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/my?page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getPublicPrompts: async (page = 0, size = 20) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/public?page=${page}&size=${size}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getPromptDetails: async id => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createPrompt: async promptData => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(API_URL, promptData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updatePrompt: async (id, promptData) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(`${API_URL}/${id}`, promptData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deletePrompt: async id => {
    try {
      const axiosInstance = createAuthAxios();
      await axiosInstance.delete(`${API_URL}/${id}`);
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
