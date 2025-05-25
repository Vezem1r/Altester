import axios from 'axios';
import { useTranslation } from 'react-i18next';
import i18n from 'i18next';

export const BASE_API_URL = import.meta.env.VITE_API_URL;
export const NOTIFICATION_URL = import.meta.env.VITE_NOTIFICATION_URL;

export const IS_DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true';

export const createAuthAxios = (contentType = 'application/json') => {
  const token = localStorage.getItem('token');
  const instance = axios.create({
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': contentType,
    },
    withCredentials: true,
  });

  if (IS_DEMO_MODE) {
    instance.interceptors.response.use(
      response => response,
      error => {
        return Promise.resolve({
          data: { success: true, message: 'Demo mode - operation simulated' },
          status: 200,
          statusText: 'OK'
        });
      }
    );
  }

  return instance;
};

export const useApiUtils = () => {
  const { t } = useTranslation();

  const handleApiError = error => {
    if (IS_DEMO_MODE) {
      return;
    }

    if (error.response) {
      const { status, data } = error.response;

      if (typeof data === 'string') {
        throw new Error(data);
      }

      if (data && data.message) {
        throw new Error(data.message);
      }

      if (status === 400) {
        throw new Error(
          t(
            'apiUtils.invalidRequest',
            'Invalid request data. Please check your input.'
          )
        );
      } else if (status === 401) {
        throw new Error(
          t('apiUtils.unauthorized', 'Unauthorized. Please log in again.')
        );
      } else if (status === 403) {
        throw new Error(
          t(
            'apiUtils.forbidden',
            "You don't have permission to perform this action."
          )
        );
      } else if (status === 404) {
        throw new Error(
          t('apiUtils.notFound', 'The requested resource was not found.')
        );
      } else if (status === 409) {
        throw new Error(
          t(
            'apiUtils.conflict',
            'This operation caused a conflict with an existing resource.'
          )
        );
      } else if (status === 500) {
        throw new Error(
          t(
            'apiUtils.serverError',
            'Internal server error. Please try again later.'
          )
        );
      }

      throw new Error(
        t('apiUtils.requestFailed', 'Request failed with status {{status}}', {
          status,
        })
      );
    }

    if (error.message) {
      throw new Error(error.message);
    }

    throw new Error(
      t('apiUtils.unexpectedError', 'An unexpected error occurred')
    );
  };

  const handleAuthError = errorResponse => {
    if (IS_DEMO_MODE) {
      return 'Demo mode - authentication simulated';
    }

    if (!errorResponse.response) {
      return t(
        'apiUtils.networkError',
        'Network error. Please check your connection.'
      );
    }

    if (
      errorResponse.response.status === 401 &&
      (!errorResponse.response.data ||
        errorResponse.response.data === '' ||
        Object.keys(errorResponse.response.data).length === 0)
    ) {
      return t(
        'apiUtils.invalidCredentials',
        'Invalid credentials. Please check your information and try again.'
      );
    }

    const errorData = errorResponse.response.data;

    if (errorData && errorData.errorCode) {
      switch (errorData.errorCode) {
        case 'AUTH-100':
          return t(
            'apiUtils.authInvalidCredentials',
            'Invalid username or password. Please check your credentials and try again.'
          );
        case 'AUTH-101':
          return t(
            'apiUtils.authAccountNotFound',
            'Account not found. Please check your email or username, or create a new account.'
          );
        case 'AUTH-102':
          return t(
            'apiUtils.authNeedsVerification',
            'Your account needs to be verified. Please check your email for a verification code.'
          );
        case 'AUTH-103':
          return t(
            'apiUtils.authIncorrectPassword',
            'Incorrect password. Please try again.'
          );
        case 'AUTH-200':
          return t(
            'apiUtils.authEmailExists',
            'This email is already registered. Please use a different email or try to login.'
          );
        case 'AUTH-201':
          return t(
            'apiUtils.authNoEmailCode',
            'No email change code exists for this account. Please request a new one.'
          );
        case 'AUTH-202':
          return t(
            'apiUtils.authEmailCodeExpired',
            'Your email change code has expired. Please request a new one.'
          );
        case 'AUTH-203':
          return t(
            'apiUtils.authIncorrectCode',
            'The code you entered is incorrect. Please try again.'
          );
        case 'AUTH-204':
          return t(
            'apiUtils.authEmailCodeRecent',
            'An email change code was just sent. Please wait at least one minute before requesting another one.'
          );
        case 'AUTH-300':
          return t(
            'apiUtils.authNoResetCode',
            'No password reset code exists for this account. Please request a new one.'
          );
        case 'AUTH-301':
          return t(
            'apiUtils.authResetCodeExpired',
            'Your password reset code has expired. Please request a new one.'
          );
        case 'AUTH-302':
          return t(
            'apiUtils.authIncorrectResetCode',
            'The reset code you entered is incorrect. Please try again.'
          );
        case 'AUTH-303':
          return t(
            'apiUtils.authResetCodeRecent',
            'A password reset code was just sent. Please wait at least one minute before requesting another one.'
          );
        case 'AUTH-401':
          return t(
            'apiUtils.authNoVerifyCode',
            'No verification code exists for this account. Please request a new one.'
          );
        case 'AUTH-402':
          return t(
            'apiUtils.authVerifyCodeExpired',
            'Your verification code has expired. Please request a new one.'
          );
        case 'AUTH-403':
          return t(
            'apiUtils.authIncorrectVerifyCode',
            'The verification code you entered is incorrect. Please try again.'
          );
        case 'AUTH-404':
          return t(
            'apiUtils.authVerifyCodeRecent',
            'A verification code was just sent. Please wait at least one minute before requesting another one.'
          );
        case 'AUTH-500':
          return t(
            'apiUtils.authLdapFailed',
            'LDAP authentication failed. Please check your credentials.'
          );
        case 'AUTH-501':
          return t(
            'apiUtils.authLdapRequired',
            'This account requires LDAP authentication. Please use the LDAP login option.'
          );
        case 'AUTH-601':
          return t(
            'apiUtils.authInvalidSession',
            'Your session is invalid. Please log in again.'
          );
        case 'AUTH-602':
          return t(
            'apiUtils.authExpiredSession',
            'Your session has expired. Please log in again.'
          );
        case 'AUTH-603':
          return t(
            'apiUtils.authInvalidToken',
            'Invalid authentication token. Please log in again.'
          );
        case 'AUTH-900':
          return t(
            'apiUtils.authInvalidRequest',
            'Your request was invalid. Please check the information you provided.'
          );
        case 'AUTH-999':
          return t(
            'apiUtils.authUnexpectedError',
            'An unexpected error occurred. Please try again later.'
          );
        default:
          if (errorData.message) {
            return errorData.message;
          }
      }
    }

    let errorMessage;

    if (typeof errorData === 'string') {
      errorMessage = errorData;
    } else if (errorData && typeof errorData === 'object') {
      errorMessage =
        errorData.message || errorData.error || JSON.stringify(errorData);
    } else {
      errorMessage = t('apiUtils.unknownError', 'An unknown error occurred');
    }

    return errorMessage;
  };

  return { handleApiError, handleAuthError };
};

export const handleApiError = error => {
  if (IS_DEMO_MODE) {
    return;
  }

  const t = i18n.t;

  if (error.response) {
    const { status, data } = error.response;

    if (typeof data === 'string') {
      throw new Error(data);
    }

    if (data && data.message) {
      throw new Error(data.message);
    }

    if (status === 400) {
      throw new Error(
        t(
          'apiUtils.invalidRequest',
          'Invalid request data. Please check your input.'
        )
      );
    } else if (status === 401) {
      throw new Error(
        t('apiUtils.unauthorized', 'Unauthorized. Please log in again.')
      );
    } else if (status === 403) {
      throw new Error(
        t(
          'apiUtils.forbidden',
          "You don't have permission to perform this action."
        )
      );
    } else if (status === 404) {
      throw new Error(
        t('apiUtils.notFound', 'The requested resource was not found.')
      );
    } else if (status === 409) {
      throw new Error(
        t(
          'apiUtils.conflict',
          'This operation caused a conflict with an existing resource.'
        )
      );
    } else if (status === 500) {
      throw new Error(
        t(
          'apiUtils.serverError',
          'Internal server error. Please try again later.'
        )
      );
    }

    throw new Error(
      t('apiUtils.requestFailed', 'Request failed with status {{status}}', {
        status,
      })
    );
  }

  if (error.message) {
    throw new Error(error.message);
  }

  throw new Error(
    t('apiUtils.unexpectedError', 'An unexpected error occurred')
  );
};

export const handleAuthError = errorResponse => {
  if (IS_DEMO_MODE) {
    return 'Demo mode - authentication simulated';
  }

  const t = i18n.t;

  if (!errorResponse.response) {
    return t(
      'apiUtils.networkError',
      'Network error. Please check your connection.'
    );
  }

  if (
    errorResponse.response.status === 401 &&
    (!errorResponse.response.data ||
      errorResponse.response.data === '' ||
      Object.keys(errorResponse.response.data).length === 0)
  ) {
    return t(
      'apiUtils.invalidCredentials',
      'Invalid credentials. Please check your information and try again.'
    );
  }

  const errorData = errorResponse.response.data;

  if (errorData && errorData.errorCode) {
    switch (errorData.errorCode) {
      case 'AUTH-100':
        return t(
          'apiUtils.authInvalidCredentials',
          'Invalid username or password. Please check your credentials and try again.'
        );
      case 'AUTH-101':
        return t(
          'apiUtils.authAccountNotFound',
          'Account not found. Please check your email or username, or create a new account.'
        );
      case 'AUTH-102':
        return t(
          'apiUtils.authNeedsVerification',
          'Your account needs to be verified. Please check your email for a verification code.'
        );
      case 'AUTH-103':
        return t(
          'apiUtils.authIncorrectPassword',
          'Incorrect password. Please try again.'
        );
      case 'AUTH-200':
        return t(
          'apiUtils.authEmailExists',
          'This email is already registered. Please use a different email or try to login.'
        );
      case 'AUTH-201':
        return t(
          'apiUtils.authNoEmailCode',
          'No email change code exists for this account. Please request a new one.'
        );
      case 'AUTH-202':
        return t(
          'apiUtils.authEmailCodeExpired',
          'Your email change code has expired. Please request a new one.'
        );
      case 'AUTH-203':
        return t(
          'apiUtils.authIncorrectCode',
          'The code you entered is incorrect. Please try again.'
        );
      case 'AUTH-204':
        return t(
          'apiUtils.authEmailCodeRecent',
          'An email change code was just sent. Please wait at least one minute before requesting another one.'
        );
      case 'AUTH-300':
        return t(
          'apiUtils.authNoResetCode',
          'No password reset code exists for this account. Please request a new one.'
        );
      case 'AUTH-301':
        return t(
          'apiUtils.authResetCodeExpired',
          'Your password reset code has expired. Please request a new one.'
        );
      case 'AUTH-302':
        return t(
          'apiUtils.authIncorrectResetCode',
          'The reset code you entered is incorrect. Please try again.'
        );
      case 'AUTH-303':
        return t(
          'apiUtils.authResetCodeRecent',
          'A password reset code was just sent. Please wait at least one minute before requesting another one.'
        );
      case 'AUTH-401':
        return t(
          'apiUtils.authNoVerifyCode',
          'No verification code exists for this account. Please request a new one.'
        );
      case 'AUTH-402':
        return t(
          'apiUtils.authVerifyCodeExpired',
          'Your verification code has expired. Please request a new one.'
        );
      case 'AUTH-403':
        return t(
          'apiUtils.authIncorrectVerifyCode',
          'The verification code you entered is incorrect. Please try again.'
        );
      case 'AUTH-404':
        return t(
          'apiUtils.authVerifyCodeRecent',
          'A verification code was just sent. Please wait at least one minute before requesting another one.'
        );
      case 'AUTH-500':
        return t(
          'apiUtils.authLdapFailed',
          'LDAP authentication failed. Please check your credentials.'
        );
      case 'AUTH-501':
        return t(
          'apiUtils.authLdapRequired',
          'This account requires LDAP authentication. Please use the LDAP login option.'
        );
      case 'AUTH-601':
        return t(
          'apiUtils.authInvalidSession',
          'Your session is invalid. Please log in again.'
        );
      case 'AUTH-602':
        return t(
          'apiUtils.authExpiredSession',
          'Your session has expired. Please log in again.'
        );
      case 'AUTH-603':
        return t(
          'apiUtils.authInvalidToken',
          'Invalid authentication token. Please log in again.'
        );
      case 'AUTH-900':
        return t(
          'apiUtils.authInvalidRequest',
          'Your request was invalid. Please check the information you provided.'
        );
      case 'AUTH-999':
        return t(
          'apiUtils.authUnexpectedError',
          'An unexpected error occurred. Please try again later.'
        );
      default:
        if (errorData.message) {
          return errorData.message;
        }
    }
  }

  let errorMessage;

  if (typeof errorData === 'string') {
    errorMessage = errorData;
  } else if (errorData && typeof errorData === 'object') {
    errorMessage =
      errorData.message || errorData.error || JSON.stringify(errorData);
  } else {
    errorMessage = t('apiUtils.unknownError', 'An unknown error occurred');
  }

  return errorMessage;
};

export const buildUrlWithParams = (baseUrl, params) => {
  const urlObject = new window.URL(baseUrl);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      urlObject.searchParams.append(key, value);
    }
  });

  return urlObject.toString();
};

export const setupGlobalInterceptors = () => {
  const handleLogout = () => {
    if (IS_DEMO_MODE) {
      return;
    }
    
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('isRegistered');
    localStorage.removeItem('username');
    window.location.replace('/');
  };

  axios.interceptors.response.use(
    response => response,
    responseError => {
      if (IS_DEMO_MODE) {
        return Promise.resolve({
          data: { success: true, message: 'Demo mode - operation simulated' },
          status: 200,
          statusText: 'OK'
        });
      }

      if (
        responseError.response &&
        (responseError.response.status === 401 ||
          responseError.response.status === 403 ||
          responseError.response?.data?.errorCode === 'ERR-900' ||
          responseError.response?.data?.errorCode === 'AUTH-601' ||
          responseError.response?.data?.errorCode === 'AUTH-602' ||
          responseError.response?.data?.errorCode === 'AUTH-603')
      ) {
        handleLogout();
      }

      return Promise.reject(responseError);
    }
  );
};

const requestTimestamps = {};
const MIN_REQUEST_INTERVAL = 300;

export const throttleRequest = async (key, requestFunction) => {
  const now = Date.now();
  const lastRequestTime = requestTimestamps[key] || 0;
  const timeSinceLastRequest = now - lastRequestTime;

  if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
    await new Promise(resolve =>
      window.setTimeout(resolve, MIN_REQUEST_INTERVAL - timeSinceLastRequest)
    );
  }

  requestTimestamps[key] = Date.now();
  return requestFunction();
};

export const setupDemoMode = () => {
  if (IS_DEMO_MODE) {
    const originalConsole = { ...console };
    
    console.log = () => {};
    console.error = () => {};
    console.warn = () => {};
    console.info = () => {};
    console.debug = () => {};
    
    window.restoreConsole = () => {
      Object.assign(console, originalConsole);
    };
    
    console.info('Demo mode enabled - all errors are suppressed');
  }
};