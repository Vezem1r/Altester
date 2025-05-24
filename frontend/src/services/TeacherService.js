import { BASE_API_URL, createAuthAxios, handleApiError } from './apiUtils';

const API_URL = BASE_API_URL;

export const TeacherService = {
  getTeacherPage: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/teacher`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTeacherStudents: async (page = 0, size = 20, search = '') => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/teacher/getStudents?page=${page}&size=${size}${search ? `&search=${search}` : ''}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTeacherGroups: async (page = 0, size = 20, search = '', status = '') => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/teacher/getGroups?page=${page}&size=${size}${search ? `&search=${search}` : ''}${status ? `&status=${status}` : ''}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getTeacherGroup: async groupId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(
        `${API_URL}/teacher/group/${groupId}`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  moveStudent: async (studentUsername, fromGroupId, toGroupId) => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.post(
        `${API_URL}/teacher/moveStudent`,
        {
          studentUsername,
          fromGroupId,
          toGroupId,
        }
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
