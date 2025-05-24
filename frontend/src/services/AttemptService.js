import {
  BASE_API_URL,
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_URL = `${BASE_API_URL}/attempts`;

export const AttemptService = {
  getTestAttemptsForTeacher: async (testId, searchQuery = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/teacher/test/${testId}`, {
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentAttemptsForTeacher: async (username, searchQuery = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/teacher/student`, {
        username,
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestAttemptsForAdmin: async (testId, searchQuery = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/admin/test/${testId}`, {
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentAttemptsForAdmin: async (username, searchQuery = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/admin/student`, {
        username,
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAttemptReview: async attemptId => {
    try {
      const axiosInstance = createAuthAxios();
      const url = `${API_URL}/review/${attemptId}`;

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentTestAttemptsForTeacher: async (testId, username) => {
    try {
      const axiosInstance = createAuthAxios();
      const url = `${API_URL}/teacher/test/${testId}/student/${username}`;

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentTestAttemptsForAdmin: async (testId, username) => {
    try {
      const axiosInstance = createAuthAxios();
      const url = `${API_URL}/admin/test/${testId}/student/${username}`;

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  submitAttemptReview: async (attemptId, reviewData) => {
    try {
      const axiosInstance = createAuthAxios();
      const url = `${API_URL}/review/${attemptId}`;

      const response = await axiosInstance.post(url, reviewData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
