import { createContext, useState, useEffect, useContext, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { AuthService } from '@/services/AuthService';
import { useTranslation } from 'react-i18next';

const AuthContext = createContext(null);
const API_URL = import.meta.env.VITE_API_URL;

const apiAxios = axios.create({
  baseURL: API_URL,
});

const isAuthTokenError = error => {
  if (!error.response || !error.response.data) return false;

  const errorData = error.response.data;
  const tokenErrorCodes = ['AUTH-601', 'AUTH-602', 'AUTH-603'];

  if (
    error.response.status === 401 &&
    errorData.errorCode &&
    tokenErrorCodes.includes(errorData.errorCode)
  ) {
    return true;
  }

  if (
    errorData.errorCode === 'ERR-900' &&
    errorData.message &&
    (errorData.message.includes('authentication token') ||
      errorData.message.includes('Invalid token') ||
      errorData.message.includes('Expired token'))
  ) {
    return true;
  }

  return false;
};

const handleAuthError = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userRole');
  localStorage.removeItem('isRegistered');
  localStorage.removeItem('username');

  if (window.location.pathname !== '/') {
    window.location.href = '/';
  }
};

apiAxios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

apiAxios.interceptors.response.use(
  response => response,
  error => {
    if (isAuthTokenError(error)) {
      handleAuthError();
    }
    return Promise.reject(error);
  }
);

axios.interceptors.response.use(
  response => response,
  error => {
    if (isAuthTokenError(error)) {
      handleAuthError();
    }
    return Promise.reject(error);
  }
);

export const api = apiAxios;

export const AuthProvider = ({ children }) => {
  const { t } = useTranslation();
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole'));
  const [isRegistered, setIsRegistered] = useState(
    localStorage.getItem('isRegistered') === 'true'
  );
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [authConfig, _setAuthConfig] = useState(AuthService.getAuthConfig());

  const isInitializedRef = useRef(false);
  const tokenValidationRef = useRef(false);

  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (isInitializedRef.current) return;
    isInitializedRef.current = true;

    const initializeAuth = async () => {
      setIsLoading(true);

      const storedToken = localStorage.getItem('token');

      if (storedToken) {
        const storedUserRole = localStorage.getItem('userRole');
        const storedIsRegistered =
          localStorage.getItem('isRegistered') === 'true';
        const storedUsername = localStorage.getItem('username');

        setToken(storedToken);
        setUserRole(storedUserRole);
        setIsRegistered(storedIsRegistered);

        setUser({
          username: storedUsername || 'user',
          registered: storedIsRegistered,
        });

        if (!tokenValidationRef.current) {
          tokenValidationRef.current = true;
          try {
            await AuthService.validateToken();

            if (location.pathname === '/') {
              redirectBasedOnRole(storedUserRole);
            }
          } catch {
            logout(true);
            setIsLoading(false);
            return;
          }
        }
      }

      setIsLoading(false);
    };

    initializeAuth();
  }, [t]);

  const redirectBasedOnRole = role => {
    switch (role) {
      case 'TEACHER':
        navigate('/teacher');
        break;
      case 'STUDENT':
        navigate('/student');
        break;
      case 'ADMIN':
        navigate('/admin');
        break;
      default:
        navigate('/');
    }
  };

  const login = authData => {
    const token = authData?.token;
    const userRole = authData?.userRole;
    const registered = authData?.registered;
    const username = authData?.username || userRole || 'user';

    const isUserRegistered = registered === true;

    setToken(token);
    setUserRole(userRole);
    setIsRegistered(isUserRegistered);

    setUser({
      username,
      registered: isUserRegistered,
    });

    localStorage.setItem('token', token);
    localStorage.setItem('userRole', userRole);
    localStorage.setItem('isRegistered', isUserRegistered.toString());
    localStorage.setItem('username', username);

    tokenValidationRef.current = true;
    redirectBasedOnRole(userRole);
  };

  const logout = (shouldNavigate = true) => {
    if (process.env.NODE_ENV === 'development') {
      console.log(
        t('authContext.executingLogout', 'Executing logout function')
      );
    }

    setToken(null);
    setUserRole(null);
    setIsRegistered(false);
    setUser(null);

    localStorage.clear();
    sessionStorage.removeItem('token_validated_session');
    tokenValidationRef.current = false;

    if (shouldNavigate) {
      window.location.href = '/';
    }
  };

  const getToken = async () => {
    return localStorage.getItem('token');
  };

  const refreshUserData = async () => {
    try {
      return user;
    } catch (error) {
      return user;
    }
  };

  const value = {
    token,
    userRole,
    isAuthenticated: !!token,
    isRegistered,
    user,
    login,
    logout,
    isLoading,
    authConfig,
    api: apiAxios,
    getToken,
    refreshUserData,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  const { t } = useTranslation();
  if (!context) {
    throw new Error(
      t(
        'authContext.errorUseAuth',
        'useAuth must be used within an AuthProvider'
      )
    );
  }
  return context;
};
