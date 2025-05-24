import { useState } from 'react';
import { toast } from 'react-toastify';
import { PasswordService } from '@/services/PasswordService';
import { useTranslation } from 'react-i18next';

export default function ForgotPassword({ onSwitch, onSuccess }) {
  const { t } = useTranslation();

  const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const validateEmail = email => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  const validateForm = () => {
    const newErrors = {};

    if (!email.trim()) {
      newErrors.email = t('forgotPassword.emailRequired', 'Email is required');
    } else if (!validateEmail(email)) {
      newErrors.email = t(
        'forgotPassword.invalidEmail',
        'Invalid email format'
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
        await PasswordService.requestReset(email);
        toast.success(
          t('forgotPassword.successMessage', 'Reset code sent to your email!')
        );

        if (onSuccess) {
          onSuccess(email);
        }
      } catch (error) {
        toast.error(
          error.message ||
            t(
              'forgotPassword.failureMessage',
              'Failed to request password reset'
            )
        );

        if (error.message.includes("couldn't find an account")) {
          setErrors(prev => ({
            ...prev,
            email: t(
              'forgotPassword.noAccountFound',
              'No account found with this email'
            ),
          }));
        }
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="flex flex-col h-full justify-between px-4 sm:px-0">
      <div className="mb-4 sm:mb-6">
        <h2 className="text-lg sm:text-xl font-semibold text-gray-700 text-center">
          {t('forgotPassword.title', 'RESET PASSWORD')}
        </h2>
        <p className="text-center text-gray-600 mt-2 text-sm sm:text-base">
          {t(
            'forgotPassword.instructions',
            "Enter your email address and we'll send you a code to reset your password."
          )}
        </p>
      </div>

      <div className="flex-grow">
        <div className="space-y-3 sm:space-y-4">
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
                placeholder={t('forgotPassword.emailAddress', 'Email Address')}
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
              />
            </div>
            {errors.email && (
              <p className="mt-1 text-xs text-red-500">{errors.email}</p>
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
            ? t('forgotPassword.sending', 'SENDING...')
            : t('forgotPassword.sendResetCode', 'SEND RESET CODE')}
        </button>

        <p className="text-center text-gray-600 text-sm">
          <button
            type="button"
            onClick={onSwitch}
            disabled={loading}
            className={`font-medium text-purple-600 hover:text-purple-500 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
          >
            {t('forgotPassword.backToLogin', 'Back to Login')}
          </button>
        </p>
      </div>
    </div>
  );
}