import { BASE_API_URL, createAuthAxios, handleApiError } from './apiUtils';

const API_URL = `${BASE_API_URL}/api/keys`;

export const ApiKeyService = {
  getAllApiKeys: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAvailableApiKeys: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/available`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createApiKey: async apiKeyData => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(`${API_URL}`, apiKeyData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateApiKey: async (id, apiKeyData) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(`${API_URL}/${id}`, apiKeyData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteApiKey: async id => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.delete(`${API_URL}/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  toggleApiKeyStatus: async id => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(`${API_URL}/${id}/toggle`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateAssignmentPrompt: async (testId, groupId, promptId) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(`${API_URL}/assignment-prompt`, {
        testId,
        groupId,
        promptId,
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
