import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';
import { EmailService } from '@/services/EmailService';
import { useAuth } from '@/context/AuthContext';

const ChangeEmail = ({ onSuccess, onCancel, currentEmail }) => {
  const { t } = useTranslation();
  const { user, refreshUserData } = useAuth();
  const [currentStep, setCurrentStep] = useState('request');
  const [newEmail, setNewEmail] = useState('');
  const [password, setPassword] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [resendCooldown, setResendCooldown] = useState(0);
  const [isResending, setIsResending] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const currentUserEmail = currentEmail || user?.email;

  useEffect(() => {
    let timerId;
    if (resendCooldown > 0) {
      timerId = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }

    return () => {
      if (timerId) clearTimeout(timerId);
    };
  }, [resendCooldown]);

  const validateEmailForm = () => {
    const newErrors = {};

    if (!newEmail.trim()) {
      newErrors.email = t('changeEmail.emailRequired', 'Email is required');
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newEmail)) {
      newErrors.email = t(
        'changeEmail.validEmailRequired',
        'Please enter a valid email address'
      );
    } else if (newEmail.toLowerCase() === user?.email?.toLowerCase()) {
      newErrors.email = t(
        'changeEmail.differentEmail',
        'New email must be different from your current email'
      );
    }

    if (!password.trim()) {
      newErrors.password = t(
        'changeEmail.passwordRequired',
        'Password is required to confirm this change'
      );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateVerificationForm = () => {
    const newErrors = {};

    if (!verificationCode.trim()) {
      newErrors.code = t(
        'changeEmail.codeRequired',
        'Verification code is required'
      );
    } else if (
      verificationCode.length !== 6 ||
      !/^\d+$/.test(verificationCode)
    ) {
      newErrors.code = t('changeEmail.invalidCode', 'Code should be 6 digits');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRequestEmailChange = async () => {
    if (validateEmailForm()) {
      try {
        setLoading(true);

        await EmailService.requestChange(newEmail, password);

        toast.success(
          t(
            'changeEmail.codeSent',
            'Verification code has been sent to your new email address!'
          )
        );
        setCurrentStep('verify');
        setResendCooldown(60);
      } catch (error) {
        toast.error(
          error.message ||
            t('changeEmail.requestFailed', 'Failed to initiate email change')
        );
      } finally {
        setLoading(false);
      }
    }
  };

  const handleVerifyEmailChange = async () => {
    if (validateVerificationForm()) {
      try {
        setLoading(true);

        await EmailService.confirmChange({
          email: newEmail,
          emailCode: verificationCode,
        });

        toast.success(
          t('changeEmail.emailChanged', 'Email has been changed successfully!')
        );

        if (refreshUserData) {
          await refreshUserData();
        }

        if (onSuccess) {
          onSuccess();
        }
      } catch (error) {
        toast.error(
          error.message ||
            t('changeEmail.verificationFailed', 'Failed to verify email change')
        );

        if (error.message.includes('incorrect')) {
          setErrors(prev => ({
            ...prev,
            code: t('changeEmail.incorrectCode', 'Incorrect verification code'),
          }));
        }
      } finally {
        setLoading(false);
      }
    }
  };

  const handleResendCode = async () => {
    if (resendCooldown > 0) return;

    try {
      setIsResending(true);
      await EmailService.resendVerificationCode(newEmail);
      toast.success(
        t(
          'changeEmail.codeResent',
          'Verification code resent to your new email!'
        )
      );
      setResendCooldown(60);
    } catch (error) {
      toast.error(
        error.message ||
          t('changeEmail.resendFailed', 'Failed to resend verification code')
      );
    } finally {
      setIsResending(false);
    }
  };

  const toggleShowPassword = () => {
    setShowPassword(!showPassword);
  };

  const renderRequestStep = () => (
    <div className="space-y-4">
      <div className="text-center mb-6">
        <div className="flex items-center justify-center w-16 h-16 mx-auto bg-purple-100 rounded-full">
          <svg
            className="w-8 h-8 text-purple-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            />
          </svg>
        </div>
        <p className="mt-4 text-gray-600">
          {t(
            'changeEmail.enterNewEmailInstructions',
            "Enter your new email address and current password. We'll send a verification code to confirm the change."
          )}
        </p>
      </div>

      <div>
        <div className="bg-gray-50 px-4 py-3 border rounded-md mb-4">
          <span className="text-sm text-gray-700">
            {t('changeEmail.currentEmail', 'Current Email:')}
          </span>
          <p className="font-medium">
            {currentUserEmail || t('changeEmail.notAvailable', 'Not available')}
          </p>
        </div>

        <label
          htmlFor="email"
          className="block text-sm font-medium text-gray-700"
        >
          {t('changeEmail.newEmailAddress', 'New Email Address')}
        </label>
        <div className="relative mt-1">
          <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
            <svg
              className="w-5 h-5 text-purple-500"
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
            className={`w-full pl-10 px-4 py-2 border ${errors.email ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
            placeholder={t(
              'changeEmail.enterNewEmailPlaceholder',
              'Enter your new email address'
            )}
            value={newEmail}
            onChange={e => setNewEmail(e.target.value)}
            required
          />
        </div>
        {errors.email && (
          <p className="mt-1 text-xs text-red-500">{errors.email}</p>
        )}

        <label
          htmlFor="password"
          className="block text-sm font-medium text-gray-700 mt-4"
        >
          {t('changeEmail.currentPassword', 'Current Password')}
        </label>
        <div className="relative mt-1">
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
            className={`w-full pl-10 px-4 py-2 border ${errors.password ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
            placeholder={t(
              'changeEmail.enterCurrentPasswordPlaceholder',
              'Enter your current password'
            )}
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          <div className="absolute inset-y-0 right-0 flex items-center pr-3">
            <button
              type="button"
              onClick={toggleShowPassword}
              className="text-gray-400 hover:text-gray-500 focus:outline-none"
              aria-label={t(
                'changeEmail.togglePasswordVisibility',
                'Toggle password visibility'
              )}
            >
              {showPassword ? (
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
                  />
                </svg>
              ) : (
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
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
            </button>
          </div>
        </div>
        {errors.password && (
          <p className="mt-1 text-xs text-red-500">{errors.password}</p>
        )}
      </div>
    </div>
  );

  const renderVerifyStep = () => (
    <div className="space-y-4">
      <div className="text-center mb-6">
        <div className="flex items-center justify-center w-16 h-16 mx-auto bg-purple-100 rounded-full">
          <svg
            className="w-8 h-8 text-purple-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
            />
          </svg>
        </div>
        <p className="mt-4 text-gray-600">
          {t('changeEmail.codeSentTo', "We've sent a verification code to")}
          <br />
          <span className="font-medium text-gray-800">{newEmail}</span>
        </p>
      </div>

      <div>
        <label
          htmlFor="verificationCode"
          className="block text-sm font-medium text-gray-700"
        >
          {t('changeEmail.verificationCode', 'Verification Code')}
        </label>
        <div className="relative mt-1">
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
            id="verificationCode"
            type="text"
            className={`w-full pl-10 px-4 py-2 border ${errors.code ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 tracking-widest text-center`}
            placeholder={t(
              'changeEmail.enterCodePlaceholder',
              'Enter 6-digit code'
            )}
            value={verificationCode}
            onChange={e => {
              const value = e.target.value.replace(/[^\d]/g, '').slice(0, 6);
              setVerificationCode(value);
            }}
            maxLength="6"
            required
          />
        </div>
        {errors.code && (
          <p className="mt-1 text-xs text-red-500">{errors.code}</p>
        )}

        <div className="text-center mt-2">
          <button
            type="button"
            onClick={handleResendCode}
            disabled={resendCooldown > 0 || isResending}
            className={`text-sm font-medium ${resendCooldown > 0 || isResending ? 'text-gray-400 cursor-not-allowed' : 'text-purple-600 hover:text-purple-500'}`}
          >
            {isResending
              ? t('changeEmail.sendingIs', 'Sending...')
              : resendCooldown > 0
                ? t('changeEmail.resendCountdown', 'Resend in {{seconds}}s', {
                    seconds: resendCooldown,
                  })
                : t('changeEmail.resendCode', 'Resend Code')}
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="flex flex-col h-full justify-between">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-700 text-center">
          {currentStep === 'request'
            ? t('changeEmail.changeEmail', 'CHANGE EMAIL')
            : t('changeEmail.verifyEmail', 'VERIFY YOUR EMAIL')}
        </h2>
      </div>

      <div className="flex-grow">
        {currentStep === 'request' ? renderRequestStep() : renderVerifyStep()}
      </div>

      <div className="mt-8">
        {currentStep === 'request' ? (
          <div className="flex space-x-2">
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 py-3 px-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium rounded-full transition-colors duration-200"
              disabled={loading}
            >
              {t('changeEmail.cancel', 'CANCEL')}
            </button>
            <button
              type="button"
              onClick={handleRequestEmailChange}
              disabled={loading}
              className={`flex-1 py-3 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {loading
                ? t('changeEmail.sending', 'SENDING...')
                : t('changeEmail.continue', 'CONTINUE')}
            </button>
          </div>
        ) : (
          <div className="flex space-x-2">
            <button
              type="button"
              onClick={() => setCurrentStep('request')}
              className="flex-1 py-3 px-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium rounded-full transition-colors duration-200"
              disabled={loading}
            >
              {t('changeEmail.back', 'BACK')}
            </button>
            <button
              type="button"
              onClick={handleVerifyEmailChange}
              disabled={loading}
              className={`flex-1 py-3 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
            >
              {loading
                ? t('changeEmail.processing', 'PROCESSING...')
                : t('changeEmail.changeEmail', 'CHANGE EMAIL')}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChangeEmail;
