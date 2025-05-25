import { useState } from 'react';
import { AuthService } from '../../services/AuthService';
import { toast } from 'react-toastify';
import { useAuth } from '../../context/AuthContext';
import { useTranslation } from 'react-i18next';

export default function Login({
  onSwitch,
  onLdapSwitch,
  onForgotPassword,
  prefillEmail = '',
  showRegisterOption = true,
  showLdapOption = true,
}) {
  const { t } = useTranslation();

  const [form, setForm] = useState({
    usernameOrEmail: prefillEmail || '',
    password: '',
    rememberMe: false,
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();

  const validateEmail = email => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  const validateForm = () => {
    const newErrors = {};

    if (!form.usernameOrEmail.trim()) {
      newErrors.usernameOrEmail = t(
        'login.usernameOrEmailRequired',
        'Username or email is required'
      );
    } else if (
      form.usernameOrEmail.includes('@') &&
      !validateEmail(form.usernameOrEmail)
    ) {
      newErrors.usernameOrEmail = t(
        'login.invalidEmail',
        'Invalid email format'
      );
    }

    if (!form.password) {
      newErrors.password = t('login.passwordRequired', 'Password is required');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async e => {
    e.preventDefault();

    if (validateForm()) {
      try {
        setLoading(true);
        const response = await AuthService.login(form);
        toast.success(t('login.successMessage', 'Login successful!'));

        login({
          token: response.token,
          userRole: response.userRole,
        });
      } catch (error) {
        toast.error(error.message || t('login.failureMessage', 'Login error!'));
      } finally {
        setLoading(false);
      }
    }
  };

  const handleForgotPassword = () => {
    if (onForgotPassword) {
      if (
        form.usernameOrEmail.includes('@') &&
        validateEmail(form.usernameOrEmail)
      ) {
        onForgotPassword(form.usernameOrEmail);
      } else {
        onForgotPassword('');
      }
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="flex flex-col h-full justify-between px-4 sm:px-0">
      <div className="mb-4 sm:mb-6">
        <h2 className="text-lg sm:text-xl font-semibold text-gray-700 text-center">
          {t('login.title', 'USER LOGIN')}
        </h2>
      </div>

      <div className="flex-grow">
        <form className="space-y-4 sm:space-y-6" onSubmit={handleSubmit}>
          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-4 h-4 sm:w-5 sm:h-5 text-purple-500"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                </svg>
              </div>
              <input
                id="usernameOrEmail"
                type="text"
                className={`w-full pl-8 sm:pl-10 px-3 sm:px-4 py-2 sm:py-2 border ${errors.usernameOrEmail ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                placeholder={t('login.usernameOrEmail', 'Username or Email')}
                value={form.usernameOrEmail}
                onChange={e =>
                  setForm({ ...form, usernameOrEmail: e.target.value })
                }
                required
              />
            </div>
            {errors.usernameOrEmail && (
              <p className="mt-1 text-xs text-red-500">
                {errors.usernameOrEmail}
              </p>
            )}
          </div>

          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-4 h-4 sm:w-5 sm:h-5 text-purple-500"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    fillRule="evenodd"
                    d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className={`w-full pl-8 sm:pl-10 pr-8 sm:pr-10 py-2 border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                placeholder={t('login.password', 'Password')}
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                required
              />
              <div
                className="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                onClick={togglePasswordVisibility}
              >
                {showPassword ? (
                  <svg
                    className="w-4 h-4 sm:w-5 sm:h-5 text-purple-500"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7A9.97 9.97 0 014.02 8.971m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
                    />
                  </svg>
                ) : (
                  <svg
                    className="w-4 h-4 sm:w-5 sm:h-5 text-purple-500"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                    />
                  </svg>
                )}
              </div>
            </div>
            {errors.password && (
              <p className="mt-1 text-xs text-red-500">{errors.password}</p>
            )}
          </div>

          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center space-y-2 sm:space-y-0">
            <div className="flex items-center">
              <input
                id="rememberMe"
                type="checkbox"
                className="h-4 w-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
                checked={form.rememberMe}
                onChange={e =>
                  setForm({ ...form, rememberMe: e.target.checked })
                }
              />
              <label
                htmlFor="rememberMe"
                className="ml-2 block text-sm text-gray-600"
              >
                {t('login.rememberMe', 'Remember me')}
              </label>
            </div>

            <button
              type="button"
              onClick={handleForgotPassword}
              className="text-sm text-gray-500 hover:text-purple-500 text-left sm:text-right"
            >
              {t('login.forgotPassword', 'Forgot password?')}
            </button>
          </div>
        </form>
      </div>

      <div className="mt-auto space-y-3 sm:space-y-4">
        <button
          type="button"
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 text-sm sm:text-base ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
        >
          {loading
            ? t('login.loggingIn', 'LOGGING IN...')
            : t('login.login', 'LOGIN')}
        </button>

        {showLdapOption && (
          <>
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">
                  {t('login.or', 'Or')}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={onLdapSwitch}
              disabled={loading}
              className={`w-full py-2 px-4 border border-gray-300 text-purple-600 font-medium rounded-full hover:bg-purple-50 transition-colors duration-200 text-sm sm:text-base ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {t('login.ldapLogin', 'LDAP LOGIN')}
            </button>
          </>
        )}

        {showRegisterOption && (
          <p className="text-center text-gray-600 text-sm">
            {t('login.dontHaveAccount', "Don't have an account?")}{' '}
            <button
              type="button"
              onClick={onSwitch}
              disabled={loading}
              className={`font-medium text-purple-600 hover:text-purple-500 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {t('login.register', 'Register')}
            </button>
          </p>
        )}
      </div>
    </div>
  );
}