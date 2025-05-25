import {
  BASE_API_URL,
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_URL = `${BASE_API_URL}/admin`;

export const AdminService = {
  getAdminStats: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getStudents: async (page = 0, searchParams = {}) => {
    try {
      const axiosInstance = createAuthAxios();
      const { searchQuery, searchField, registrationFilter } = searchParams;

      const url = buildUrlWithParams(`${API_URL}/getStudents`, {
        page,
        searchQuery,
        searchField,
        registrationFilter,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTeachers: async (page = 0, searchParams = {}) => {
    try {
      const axiosInstance = createAuthAxios();
      const { searchQuery, searchField, registrationFilter } = searchParams;

      const url = buildUrlWithParams(`${API_URL}/getTeachers`, {
        page,
        searchQuery,
        searchField,
        registrationFilter,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getGroupStudents: async (
    page = 0,
    groupId = null,
    searchQuery = null,
    includeCurrentMembers = false,
    size = 10
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/group/getGroupStudents`, {
        page,
        size,
        groupId,
        searchQuery,
        includeCurrentMembers,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getGroupTeachers: async (page = 0, searchQuery = null, size = 10) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/group/getGroupTeachers`, {
        page,
        size,
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAllSubjects: async (page = 0, searchQuery = null) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/subject/all`, {
        page,
        size: 12,
        searchQuery,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createSubject: async subjectData => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(
        `${API_URL}/subject/create`,
        subjectData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateSubject: async (subjectId, subjectData) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/subject/update/${subjectId}`,
        subjectData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteSubject: async subjectId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.delete(
        `${API_URL}/subject/delete/${subjectId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getAllGroups: async (
    page = 0,
    searchQuery = null,
    activityFilter = null,
    available = null,
    subjectId = null
  ) => {
    try {
      const axiosInstance = createAuthAxios();

      const url = buildUrlWithParams(`${API_URL}/group/all`, {
        page,
        searchQuery,
        activityFilter,
        available,
        subjectId,
      });

      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getGroup: async groupId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/group/${groupId}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createGroup: async groupData => {
    try {
      if (!groupData.subjectId) {
        throw new Error('Subject ID is required when creating a group');
      }

      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(
        `${API_URL}/group/create`,
        groupData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateGroup: async (groupId, groupData) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/group/update/${groupId}`,
        groupData
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteGroup: async groupId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.delete(
        `${API_URL}/group/delete/${groupId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateUser: async (username, userData) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/updateUser`,
        userData,
        {
          params: { username },
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  promoteToTeacher: async username => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/promoteTeacher`,
        null,
        {
          params: { username },
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  promoteToStudent: async username => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/promoteStudent`,
        null,
        {
          params: { username },
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
