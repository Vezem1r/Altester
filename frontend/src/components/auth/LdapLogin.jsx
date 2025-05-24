import { useState } from 'react';
import { toast } from 'react-toastify';
import { AuthService } from '@/services/AuthService';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';

export default function LdapLogin({
  onSwitch,
  showStandardLoginOption = true,
}) {
  const { t } = useTranslation();

  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login: authLogin } = useAuth();

  const validateForm = () => {
    const newErrors = {};

    if (!login.trim()) {
      newErrors.login = t('ldapLogin.usernameRequired', 'Username is required');
    }

    if (!password) {
      newErrors.password = t(
        'ldapLogin.passwordRequired',
        'Password is required'
      );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async e => {
    e.preventDefault();

    if (validateForm()) {
      try {
        setLoading(true);
        const response = await AuthService.ldapLogin({ login, password });
        toast.success(t('ldapLogin.successMessage', 'Login successful!'));

        authLogin({
          token: response.token,
          userRole: response.userRole,
        });
      } catch (error) {
        toast.error(
          error.message || t('ldapLogin.failureMessage', 'LDAP login failed')
        );
      } finally {
        setLoading(false);
      }
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="flex flex-col h-full justify-between">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-700 text-center">
          {t('ldapLogin.title', 'LDAP LOGIN')}
        </h2>
      </div>

      {/* Content */}
      <div className="flex-grow">
        <div className="space-y-6">
          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-5 h-5 text-purple-500"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 1.944A11.954 11.954 0 012.166 5C2.056 5.649 2 6.319 2 7c0 5.225 3.34 9.67 8 11.317C14.66 16.67 18 12.225 18 7c0-.682-.057-1.35-.166-2.001A11.954 11.954 0 0110 1.944zM11 14a1 1 0 11-2 0 1 1 0 012 0zm0-7a1 1 0 10-2 0v3a1 1 0 102 0V7z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <input
                id="login"
                type="text"
                className={`w-full pl-10 px-4 py-2 border ${errors.login ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
                placeholder={t('ldapLogin.ldapUsername', 'LDAP Username')}
                value={login}
                onChange={e => setLogin(e.target.value)}
                required
              />
            </div>
            {errors.login && (
              <p className="mt-1 text-xs text-red-500">{errors.login}</p>
            )}
          </div>

          <div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-5 h-5 text-purple-500"
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
                className={`w-full pl-10 pr-10 py-2 border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
                placeholder={t('ldapLogin.ldapPassword', 'LDAP Password')}
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
              <div
                className="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                onClick={togglePasswordVisibility}
              >
                {showPassword ? (
                  <svg
                    className="w-5 h-5 text-purple-500"
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
                    className="w-5 h-5 text-purple-500"
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
        </div>
      </div>

      {/* Footer with buttons */}
      <div className="mt-auto">
        <button
          type="button"
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 mb-4 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
        >
          {loading
            ? t('ldapLogin.loggingIn', 'LOGGING IN...')
            : t('ldapLogin.loginWithLdap', 'LOGIN WITH LDAP')}
        </button>

        {showStandardLoginOption && (
          <>
            <div className="relative mb-4">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">
                  {t('ldapLogin.or', 'Or')}
                </span>
              </div>
            </div>

            <p className="text-center text-gray-600">
              <button
                type="button"
                onClick={onSwitch}
                disabled={loading}
                className={`font-medium text-purple-600 hover:text-purple-500 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
              >
                {t('ldapLogin.backToStandardLogin', 'Back to standard login')}
              </button>
            </p>
          </>
        )}
      </div>
    </div>
  );
}
