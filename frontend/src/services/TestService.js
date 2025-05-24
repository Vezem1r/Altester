import {
  BASE_API_URL,
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_URL = BASE_API_URL;

export const TestService = {
  getAllTests: async (
    page = 0,
    size = 10,
    sort = 'id,desc',
    searchQuery = '',
    isActive = null
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
        sort,
        searchQuery,
        isActive,
      };

      const url = buildUrlWithParams(`${API_URL}/admin/tests`, params);
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTeacherTests: async (
    page = 0,
    size = 10,
    sort = 'id,desc',
    searchQuery = '',
    isActive = null,
    allowTeacherEdit = null
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
        sort,
        searchQuery,
        isActive,
        allowTeacherEdit,
      };

      const url = buildUrlWithParams(`${API_URL}/teacher/tests/my`, params);
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestPreview: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/teacher/tests/${testId}/preview`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestSummary: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/teacher/tests/${testId}/summary`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createTest: async testData => {
    try {
      if (testData.groupIds) {
        testData.groupIds = [...new Set(testData.groupIds)];
        testData.assignmentType = 'groups';
      }

      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(
        `${API_URL}/teacher/tests`,
        testData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateTest: async (testId, testData) => {
    try {
      if (testData.groupIds) {
        testData.groupIds = [...new Set(testData.groupIds)];
        testData.assignmentType = 'groups';
      }

      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/teacher/tests/${testId}`,
        testData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteTest: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.delete(
        `${API_URL}/teacher/tests/${testId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  toggleTestActivity: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/teacher/tests/${testId}/activity`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  toggleTeacherEditPermission: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/admin/tests/${testId}/teacher-edit`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  toggleTestAiEvaluation: async testId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/tests/${testId}/evaluation`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestsBySubject: async (
    subjectId,
    searchQuery = '',
    isActive = null,
    page = 0,
    size = 10
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
        searchQuery,
        isActive,
      };

      const url = buildUrlWithParams(
        `${API_URL}/teacher/tests/subject/${subjectId}`,
        params
      );
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestsByGroup: async (
    groupId,
    searchQuery = '',
    isActive = null,
    page = 0,
    size = 10
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
        searchQuery,
        isActive,
      };

      const url = buildUrlWithParams(
        `${API_URL}/teacher/tests/group/${groupId}`,
        params
      );
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getSubjectsForTest: async (page = 0, searchQuery = '') => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size: 12,
        searchQuery,
      };

      const url = buildUrlWithParams(`${API_URL}/admin/subject/all`, params);
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getGroupsForTest: async (
    page = 0,
    searchQuery = '',
    activityFilter = 'active'
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size: 12,
        searchQuery,
        activityFilter,
      };

      const url = buildUrlWithParams(`${API_URL}/admin/group/all`, params);
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getSubjectDetails: async subjectId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/admin/subject/${subjectId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTestQuestions: async (testId, page = 0, size = 20, difficulty = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
        difficulty,
      };
      const url = buildUrlWithParams(
        `${API_URL}/teacher/tests/${testId}/questions`,
        params
      );
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudentTestPreview: async (testId, page = 0, size = 20) => {
    try {
      const axiosInstance = createAuthAxios();

      const params = {
        page,
        size,
      };

      const url = buildUrlWithParams(
        `${API_URL}/teacher/tests/${testId}/student-preview`,
        params
      );
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
