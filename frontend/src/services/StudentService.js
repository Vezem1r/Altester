import { BASE_API_URL, createAuthAxios, handleApiError } from './apiUtils';

const API_URL = BASE_API_URL;

export const StudentService = {
  getStudentDashboard: async (searchQuery = '', groupId = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = `${API_URL}/student/dashboard`;
      const params = {};

      if (searchQuery) {
        params.searchQuery = searchQuery;
      }

      if (groupId) {
        params.groupId = groupId;
      }

      const response = await axiosInstance.get(url, { params });
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAcademicHistory: async (academicYear, semester, searchQuery = '') => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        academicYear,
        semester,
      };

      if (searchQuery) {
        params.searchQuery = searchQuery;
      }

      const response = await axiosInstance.get(
        `${API_URL}/student/academic-history`,
        { params }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAvailableAcademicPeriods: async () => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/student/available-periods`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentTestAttempts: async testId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/student/test-attempts/${testId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  startAttempt: async testId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.post(
        `${API_URL}/student/test-attempts/start`,
        {
          testId: parseInt(testId),
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getQuestion: async (attemptId, questionNumber) => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/student/test-attempts/question/${attemptId}/${questionNumber}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  saveAnswer: async (attemptId, answerData) => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.post(
        `${API_URL}/student/test-attempts/save-answer`,
        {
          attemptId: parseInt(attemptId),
          answer: answerData,
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  nextQuestion: async (
    attemptId,
    currentQuestionNumber,
    currentAnswer = null
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const requestData = {
        attemptId: parseInt(attemptId),
        currentQuestionNumber,
      };

      if (currentAnswer) {
        requestData.currentAnswer = currentAnswer;
      }

      const response = await axiosInstance.post(
        `${API_URL}/student/test-attempts/next-question`,
        requestData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  previousQuestion: async (
    attemptId,
    currentQuestionNumber,
    currentAnswer = null
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const requestData = {
        attemptId: parseInt(attemptId),
        currentQuestionNumber,
      };

      if (currentAnswer) {
        requestData.currentAnswer = currentAnswer;
      }

      const response = await axiosInstance.post(
        `${API_URL}/student/test-attempts/previous-question`,
        requestData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAttemptStatus: async attemptId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/student/test-attempts/status/${attemptId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  completeAttempt: async attemptId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.post(
        `${API_URL}/student/test-attempts/complete`,
        {
          attemptId: parseInt(attemptId),
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAttemptReview: async attemptId => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.get(
        `${API_URL}/student/attempt-review/${attemptId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  requestRegrade: async submissionIds => {
    try {
      const axiosInstance = createAuthAxios();

      const response = await axiosInstance.post(
        `${API_URL}/student/request-regrade`,
        {
          submissionIds,
        }
      );

      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
