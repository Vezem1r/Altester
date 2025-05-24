import { useState, useEffect } from 'react';
import { AuthService } from '@/services/AuthService';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

export default function Register({ onSwitch, onSuccess }) {
  const { t } = useTranslation();

  const [form, setForm] = useState({
    name: '',
    surname: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState({
    length: false,
    uppercase: false,
    number: false,
  });

  const validateEmail = email => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  useEffect(() => {
    const checkPasswordStrength = () => {
      const { password } = form;
      setPasswordStrength({
        length: password.length >= 8,
        uppercase: /[A-Z]/.test(password),
        number: /[0-9]/.test(password),
      });
    };

    checkPasswordStrength();
  }, [form.password]);

  const validateForm = () => {
    const newErrors = {};

    if (!form.name.trim())
      newErrors.name = t(
        'register.firstNameRequired',
        'First name is required'
      );
    if (!form.surname.trim())
      newErrors.surname = t(
        'register.lastNameRequired',
        'Last name is required'
      );

    if (!form.email.trim()) {
      newErrors.email = t('register.emailRequired', 'Email is required');
    } else if (!validateEmail(form.email)) {
      newErrors.email = t('register.invalidEmail', 'Invalid email format');
    }

    if (!form.password) {
      newErrors.password = t(
        'register.passwordRequired',
        'Password is required'
      );
    } else {
      if (!passwordStrength.length)
        newErrors.password = t(
          'register.passwordLength',
          'Password must be at least 8 characters'
        );
      else if (!passwordStrength.uppercase)
        newErrors.password = t(
          'register.passwordUppercase',
          'Password must contain at least 1 uppercase letter'
        );
      else if (!passwordStrength.number)
        newErrors.password = t(
          'register.passwordNumber',
          'Password must contain at least 1 number'
        );
    }

    if (form.password !== form.confirmPassword) {
      newErrors.confirmPassword = t(
        'register.passwordsMismatch',
        'Passwords do not match'
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
        const { confirmPassword, ...submitData } = form;
        await AuthService.register(submitData);
        toast.success(
          t(
            'register.successMessage',
            'Registration successful! Check your email for verification.'
          )
        );

        if (onSuccess) {
          onSuccess(form.email);
        }
      } catch (error) {
        toast.error(
          error.message || t('register.failureMessage', 'Registration failed!')
        );

        if (error.message.includes('email is already registered')) {
          setErrors(prev => ({
            ...prev,
            email: t(
              'register.emailAlreadyRegistered',
              'This email is already registered'
            ),
          }));
        }
      } finally {
        setLoading(false);
      }
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  const toggleConfirmPasswordVisibility = () => {
    setShowConfirmPassword(!showConfirmPassword);
  };

  return (
    <div className="flex flex-col h-full justify-between px-4 sm:px-0">
      <div className="mb-4 sm:mb-6">
        <h2 className="text-lg sm:text-xl font-semibold text-gray-700 text-center">
          {t('register.createAccount', 'CREATE ACCOUNT')}
        </h2>
      </div>

      <div className="flex-grow">
        <div className="space-y-3 sm:space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
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
                  id="name"
                  type="text"
                  className={`w-full pl-8 sm:pl-10 px-3 sm:px-4 py-2 border ${errors.name ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                  placeholder={t('register.firstName', 'First Name')}
                  value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })}
                  required
                />
              </div>
              {errors.name && (
                <p className="mt-1 text-xs text-red-500">{errors.name}</p>
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
                    <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                  </svg>
                </div>
                <input
                  id="surname"
                  type="text"
                  className={`w-full pl-8 sm:pl-10 px-3 sm:px-4 py-2 border ${errors.surname ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                  placeholder={t('register.lastName', 'Last Name')}
                  value={form.surname}
                  onChange={e => setForm({ ...form, surname: e.target.value })}
                  required
                />
              </div>
              {errors.surname && (
                <p className="mt-1 text-xs text-red-500">{errors.surname}</p>
              )}
            </div>
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
                  <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                  <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                </svg>
              </div>
              <input
                id="email"
                type="email"
                className={`w-full pl-8 sm:pl-10 px-3 sm:px-4 py-2 border ${errors.email ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                placeholder={t('register.emailAddress', 'Email Address')}
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            {errors.email && (
              <p className="mt-1 text-xs text-red-500">{errors.email}</p>
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
                className={`w-full pl-8 sm:pl-10 pr-8 sm:pr-10 px-3 sm:px-4 py-2 border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                placeholder={t('register.password', 'Password')}
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

            {form.password && (
              <div className="mt-2 space-y-1">
                <div className="flex items-center">
                  <div
                    className={`w-3 h-3 sm:w-4 sm:h-4 mr-2 rounded-full ${passwordStrength.length ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t(
                      'register.passwordRequirement1',
                      'At least 8 characters'
                    )}
                  </span>
                </div>
                <div className="flex items-center">
                  <div
                    className={`w-3 h-3 sm:w-4 sm:h-4 mr-2 rounded-full ${passwordStrength.uppercase ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t(
                      'register.passwordRequirement2',
                      'At least 1 uppercase letter'
                    )}
                  </span>
                </div>
                <div className="flex items-center">
                  <div
                    className={`w-3 h-3 sm:w-4 sm:h-4 mr-2 rounded-full ${passwordStrength.number ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t('register.passwordRequirement3', 'At least 1 number')}
                  </span>
                </div>
              </div>
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
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                className={`w-full pl-8 sm:pl-10 pr-8 sm:pr-10 px-3 sm:px-4 py-2 border ${errors.confirmPassword ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 text-sm sm:text-base`}
                placeholder={t('register.confirmPassword', 'Confirm Password')}
                value={form.confirmPassword}
                onChange={e =>
                  setForm({ ...form, confirmPassword: e.target.value })
                }
                required
              />
              <div
                className="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer"
                onClick={toggleConfirmPasswordVisibility}
              >
                {showConfirmPassword ? (
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
            {errors.confirmPassword && (
              <p className="mt-1 text-xs text-red-500">
                {errors.confirmPassword}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="mt-auto space-y-3 sm:space-y-4">
        <button
          type="button"
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 text-sm sm:text-base ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
        >
          {loading
            ? t('register.registering', 'REGISTERING...')
            : t('register.register', 'REGISTER')}
        </button>

        <p className="text-center text-gray-600 text-sm">
          {t('register.alreadyHaveAccount', 'Already have an account?')}{' '}
          <button
            type="button"
            onClick={onSwitch}
            disabled={loading}
            className={`font-medium text-purple-600 hover:text-purple-500 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
          >
            {t('register.signIn', 'Sign In')}
          </button>
        </p>
      </div>
    </div>
  );
}