import { useState, useEffect } from 'react';
import { AuthService } from '@/services/AuthService';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

export default function Verify({ email, onLoginRedirect }) {
  const { t } = useTranslation();

  const [code, setCode] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(60);
  const [isResending, setIsResending] = useState(false);

  useEffect(() => {
    let timerId;
    if (resendCooldown > 0) {
      timerId = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }

    return () => {
      if (timerId) clearTimeout(timerId);
    };
  }, [resendCooldown]);

  const validateForm = () => {
    const newErrors = {};

    if (!code.trim()) {
      newErrors.code = t(
        'verify.codeRequired',
        'Verification code is required'
      );
    } else if (code.length !== 6 || !/^\d+$/.test(code)) {
      newErrors.code = t('verify.codeFormat', 'Code should be 6 digits');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleVerify = async () => {
    if (validateForm()) {
      try {
        setLoading(true);
        await AuthService.verify(email, code);
        toast.success(
          t('verify.successMessage', 'Account verified successfully!')
        );

        if (onLoginRedirect) {
          onLoginRedirect(email);
        }
      } catch (error) {
        toast.error(
          error.message || t('verify.failureMessage', 'Verification failed!')
        );
      } finally {
        setLoading(false);
      }
    }
  };

  const handleResend = async () => {
    if (resendCooldown > 0) return;

    try {
      setIsResending(true);
      await AuthService.resendVerification(email);
      toast.success(
        t('verify.resendSuccess', 'Verification code resent to your email!')
      );
      setResendCooldown(60);
    } catch (error) {
      toast.error(
        error.message ||
          t('verify.resendFailure', 'Error resending verification code!')
      );
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="flex flex-col h-full justify-between">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-gray-700 text-center">
          {t('verify.title', 'VERIFY YOUR EMAIL')}
        </h2>
      </div>

      {/* Content */}
      <div className="flex-grow">
        <div className="text-center mb-8">
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
            {t('verify.codeSentMessage', "We've sent a verification code to")}
            <br />
            <span className="font-medium text-gray-800">{email}</span>
          </p>
        </div>

        <div className="space-y-6">
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
              id="verificationCode"
              type="text"
              className={`w-full pl-10 px-4 py-2 border ${errors.code ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 tracking-widest text-center`}
              value={code}
              onChange={e => {
                const value = e.target.value.replace(/[^\d]/g, '').slice(0, 6);
                setCode(value);
              }}
              maxLength="6"
              placeholder={t('verify.enterCode', 'Enter 6-digit code')}
              required
            />
            {errors.code && (
              <p className="mt-1 text-xs text-red-500">{errors.code}</p>
            )}
          </div>
        </div>
      </div>

      {/* Footer with buttons */}
      <div className="mt-auto">
        <button
          type="button"
          onClick={handleVerify}
          disabled={loading}
          className={`w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 mb-4 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
        >
          {loading
            ? t('verify.verifying', 'VERIFYING...')
            : t('verify.verifyAccount', 'VERIFY ACCOUNT')}
        </button>

        <div className="text-center">
          <p className="text-gray-600 text-sm">
            {t('verify.didntReceiveCode', "Didn't receive the code?")}{' '}
            <button
              type="button"
              onClick={handleResend}
              disabled={resendCooldown > 0 || isResending}
              className={`font-medium ${resendCooldown > 0 || isResending ? 'text-gray-400 cursor-not-allowed' : 'text-purple-600 hover:text-purple-500'}`}
            >
              {isResending
                ? t('verify.sending', 'Sending...')
                : resendCooldown > 0
                  ? t('verify.resendCountdown', 'Resend in {{seconds}}s', {
                      seconds: resendCooldown,
                    })
                  : t('verify.resendCode', 'Resend Code')}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
