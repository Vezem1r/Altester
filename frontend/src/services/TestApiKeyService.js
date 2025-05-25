import {
  BASE_API_URL,
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_URL = `${BASE_API_URL}/tests/apiKeys`;

export const TestApiKeyService = {
  getTestApiKeys: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/${testId}/api-keys`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  assignApiKeyToTest: async (apiKeyId, testId, groupId) => {
    try {
      if (!groupId) {
        throw new Error('Group ID is required for API key assignment');
      }

      const axiosInstance = createAuthAxios();
      const data = {
        apiKeyId,
        testId,
        groupId,
      };
      const response = await axiosInstance.post(`${API_URL}/assign`, data);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  unassignApiKeyFromTest: async (testId, groupId = null) => {
    try {
      const axiosInstance = createAuthAxios();
      const params = {
        testId,
        ...(groupId && { groupId }),
      };
      const url = buildUrlWithParams(`${API_URL}/unassign`, params);
      const response = await axiosInstance.post(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  toggleAiEvaluation: async (testId, groupId) => {
    try {
      const axiosInstance = createAuthAxios();
      const params = {
        testId,
        groupId,
      };
      const url = buildUrlWithParams(
        `${BASE_API_URL}/tests/evaluation`,
        params
      );
      const response = await axiosInstance.put(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
