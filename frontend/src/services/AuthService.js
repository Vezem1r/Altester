import axios from 'axios';
import {
  BASE_API_URL,
  handleAuthError,
  setupGlobalInterceptors,
} from './apiUtils';

const API_URL = `${BASE_API_URL}/auth`;

const authAxios = axios.create({
  baseURL: API_URL,
});

authAxios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response?.data?.errorCode === 'ERR-900') {
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      localStorage.removeItem('isRegistered');
      localStorage.removeItem('username');

      window.location.replace('/');
    }

    return Promise.reject(error);
  }
);

authAxios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

setupGlobalInterceptors();

export const AuthService = {
  getAuthConfig: () => {
    return {
      mode: AUTH_MODE,
      standardAuthEnabled:
        AUTH_MODE === AUTH_MODES.ALL || AUTH_MODE === AUTH_MODES.STANDARD_ONLY,
      ldapAuthEnabled:
        AUTH_MODE === AUTH_MODES.ALL || AUTH_MODE === AUTH_MODES.LDAP_ONLY,
      registrationEnabled:
        AUTH_MODE === AUTH_MODES.ALL || AUTH_MODE === AUTH_MODES.STANDARD_ONLY,
    };
  },

  login: async data => {
    try {
      const response = await authAxios.post(`/signin`, data);
      return response.data;
    } catch (error) {
      const message = handleAuthError(error);
      throw new Error(message);
    }
  },

  ldapLogin: async credentials => {
    try {
      const response = await authAxios.post(`/ldap/signin`, credentials);
      return response.data;
    } catch (error) {
      const message = handleAuthError(error);
      throw new Error(message);
    }
  },

  register: async data => {
    try {
      const response = await authAxios.post(`/signup`, data);
      return response.data;
    } catch (error) {
      const message = handleAuthError(error);
      throw new Error(message);
    }
  },

  verify: async (email, verificationCode) => {
    try {
      const response = await authAxios.post(`/verify`, {
        email,
        verificationCode,
      });
      return response.data;
    } catch (error) {
      const message = handleAuthError(error);
      throw new Error(message);
    }
  },

  resendVerification: async email => {
    try {
      const response = await authAxios.post(`/resend?email=${email}`);
      return response.data;
    } catch (error) {
      const message = handleAuthError(error);
      throw new Error(message);
    }
  },

  validateToken: async () => {
    const response = await authAxios.get(`/validate-token`);
    return response.data;
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  getToken: () => {
    return localStorage.getItem('token');
  },

  getUserRole: () => {
    return localStorage.getItem('userRole');
  },
};

export const AUTH_MODES = {
  ALL: 'ALL', // Both standard and LDAP authentication
  LDAP_ONLY: 'LDAP_ONLY', // Only LDAP authentication
  STANDARD_ONLY: 'STANDARD_ONLY', // Only standard authentication
};

export const AUTH_MODE = import.meta.env.VITE_AUTH_MODE || AUTH_MODES.ALL;
