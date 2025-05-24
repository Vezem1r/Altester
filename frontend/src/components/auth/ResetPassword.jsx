import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { PasswordService } from '@/services/PasswordService';
import { useTranslation } from 'react-i18next';

export default function ResetPassword({ email, onSuccess }) {
  const { t } = useTranslation();

  const [form, setForm] = useState({
    code: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(60); // Start at 60 seconds immediately
  const [isResending, setIsResending] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState({
    length: false,
    uppercase: false,
    number: false,
  });

  useEffect(() => {
    let timerId;
    if (resendCooldown > 0) {
      timerId = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }

    return () => {
      if (timerId) clearTimeout(timerId);
    };
  }, [resendCooldown]);

  useEffect(() => {
    const checkPasswordStrength = () => {
      const { newPassword } = form;
      setPasswordStrength({
        length: newPassword.length >= 8,
        uppercase: /[A-Z]/.test(newPassword),
        number: /[0-9]/.test(newPassword),
      });
    };

    checkPasswordStrength();
  }, [form.newPassword]);

  const validateForm = () => {
    const newErrors = {};

    if (!form.code.trim()) {
      newErrors.code = t(
        'resetPassword.codeRequired',
        'Verification code is required'
      );
    } else if (form.code.length !== 6 || !/^\d+$/.test(form.code)) {
      newErrors.code = t('resetPassword.codeFormat', 'Code should be 6 digits');
    }

    if (!form.newPassword) {
      newErrors.newPassword = t(
        'resetPassword.passwordRequired',
        'Password is required'
      );
    } else {
      if (!passwordStrength.length)
        newErrors.newPassword = t(
          'resetPassword.passwordLength',
          'Password must be at least 8 characters'
        );
      else if (!passwordStrength.uppercase)
        newErrors.newPassword = t(
          'resetPassword.passwordUppercase',
          'Password must contain at least 1 uppercase letter'
        );
      else if (!passwordStrength.number)
        newErrors.newPassword = t(
          'resetPassword.passwordNumber',
          'Password must contain at least 1 number'
        );
    }

    if (form.newPassword !== form.confirmPassword) {
      newErrors.confirmPassword = t(
        'resetPassword.passwordsMismatch',
        'Passwords do not match'
      );
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleResetPassword = async () => {
    if (validateForm()) {
      try {
        setLoading(true);

        await PasswordService.confirmReset({
          email,
          verificationCode: form.code,
          newPassword: form.newPassword,
        });

        toast.success(
          t(
            'resetPassword.successMessage',
            'Password has been reset successfully!'
          )
        );

        if (onSuccess) {
          onSuccess();
        }
      } catch (error) {
        toast.error(
          error.message ||
            t('resetPassword.failureMessage', 'Failed to reset password')
        );

        if (error.message.includes('incorrect')) {
          setErrors(prev => ({
            ...prev,
            code: t(
              'resetPassword.incorrectCode',
              'Incorrect verification code'
            ),
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
      await PasswordService.resendResetCode(email);
      toast.success(
        t('resetPassword.resendSuccess', 'Reset code resent to your email!')
      );
      setResendCooldown(60);
    } catch (error) {
      toast.error(
        error.message ||
          t('resetPassword.resendFailure', 'Failed to resend reset code')
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
          {t('resetPassword.title', 'RESET YOUR PASSWORD')}
        </h2>
      </div>

      {/* Content */}
      <div className="flex-grow">
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
                d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
              />
            </svg>
          </div>
          <p className="mt-4 text-gray-600">
            {t(
              'resetPassword.codeSentMessage',
              "We've sent a verification code to"
            )}
            <br />
            <span className="font-medium text-gray-800">{email}</span>
          </p>
        </div>

        <div className="space-y-4">
          {/* Verification Code */}
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
                id="verificationCode"
                type="text"
                className={`w-full pl-10 px-4 py-2 border ${errors.code ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500 tracking-widest text-center`}
                placeholder={t('resetPassword.enterCode', 'Enter 6-digit code')}
                value={form.code}
                onChange={e => {
                  const value = e.target.value
                    .replace(/[^\d]/g, '')
                    .slice(0, 6);
                  setForm({ ...form, code: value });
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
                  ? t('resetPassword.sending', 'Sending...')
                  : resendCooldown > 0
                    ? t(
                        'resetPassword.resendCountdown',
                        'Resend in {{seconds}}s',
                        { seconds: resendCooldown }
                      )
                    : t('resetPassword.resendCode', 'Resend Code')}
              </button>
            </div>
          </div>

          {/* New Password */}
          <div className="mt-4">
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
                id="newPassword"
                type="password"
                className={`w-full pl-10 px-4 py-2 border ${errors.newPassword ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
                placeholder={t('resetPassword.newPassword', 'New Password')}
                value={form.newPassword}
                onChange={e =>
                  setForm({ ...form, newPassword: e.target.value })
                }
                required
              />
            </div>
            {errors.newPassword && (
              <p className="mt-1 text-xs text-red-500">{errors.newPassword}</p>
            )}

            {/* Password requirements checklist */}
            {form.newPassword && (
              <div className="mt-2 space-y-1">
                <div className="flex items-center">
                  <div
                    className={`w-4 h-4 mr-2 rounded-full ${passwordStrength.length ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t(
                      'resetPassword.passwordRequirement1',
                      'At least 8 characters'
                    )}
                  </span>
                </div>
                <div className="flex items-center">
                  <div
                    className={`w-4 h-4 mr-2 rounded-full ${passwordStrength.uppercase ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t(
                      'resetPassword.passwordRequirement2',
                      'At least 1 uppercase letter'
                    )}
                  </span>
                </div>
                <div className="flex items-center">
                  <div
                    className={`w-4 h-4 mr-2 rounded-full ${passwordStrength.number ? 'bg-green-500' : 'bg-gray-300'}`}
                  />
                  <span className="text-xs text-gray-600">
                    {t(
                      'resetPassword.passwordRequirement3',
                      'At least 1 number'
                    )}
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Confirm Password */}
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
                id="confirmPassword"
                type="password"
                className={`w-full pl-10 px-4 py-2 border ${errors.confirmPassword ? 'border-red-500' : 'border-gray-300'} rounded-lg focus:ring-purple-500 focus:border-purple-500`}
                placeholder={t(
                  'resetPassword.confirmPassword',
                  'Confirm Password'
                )}
                value={form.confirmPassword}
                onChange={e =>
                  setForm({ ...form, confirmPassword: e.target.value })
                }
                required
              />
            </div>
            {errors.confirmPassword && (
              <p className="mt-1 text-xs text-red-500">
                {errors.confirmPassword}
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Footer with button */}
      <div className="mt-auto">
        <button
          type="button"
          onClick={handleResetPassword}
          disabled={loading}
          className={`w-full py-2 px-4 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-full transition-colors duration-200 ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
        >
          {loading
            ? t('resetPassword.resetting', 'RESETTING...')
            : t('resetPassword.resetPassword', 'RESET PASSWORD')}
        </button>
      </div>
    </div>
  );
}
