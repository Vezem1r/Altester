import {
  NOTIFICATION_URL,
  createAuthAxios,
  handleApiError,
  buildUrlWithParams,
} from './apiUtils';

const API_URL = `${NOTIFICATION_URL}/notifications`;

export const NotificationService = {
  getNotifications: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(API_URL);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getPaginatedNotifications: async params => {
    try {
      const axiosInstance = createAuthAxios();
      const url = buildUrlWithParams(`${API_URL}/paginated`, params);
      const response = await axiosInstance.get(url);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  markAsRead: async notificationId => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(
        `${API_URL}/${notificationId}/read`
      );
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  markAllAsRead: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.put(`${API_URL}/read-all`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getUnreadCount: async () => {
    try {
      const axiosInstance = createAuthAxios();
      const response = await axiosInstance.get(`${API_URL}/unread-count`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  connectWebSocket: async _token => {
    throw new Error(
      'This method should be implemented in the NotificationContext'
    );
  },
};
