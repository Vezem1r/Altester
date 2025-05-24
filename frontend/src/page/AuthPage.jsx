import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  Login,
  Register,
  Verify,
  LdapLogin,
  ForgotPassword,
  ResetPassword,
  ThreeBackground,
  AIBackground,
} from '@/components/auth';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';
import SimpleLanguageSwitcher from '@/components/common/SimpleLanguageSwitcher';

export default function AuthPage() {
  const { t } = useTranslation();
  const [step, setStep] = useState('login');
  const [email, setEmail] = useState('');
  const [animating, setAnimating] = useState(false);
  const [prevStep, setPrevStep] = useState(null);
  const { isAuthenticated, userRole, authConfig, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      switch (userRole) {
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
    }
  }, [isAuthenticated, userRole, isLoading, navigate]);

  useEffect(() => {
    if (!isLoading) {
      if (authConfig.ldapAuthEnabled && !authConfig.standardAuthEnabled) {
        setStep('ldap');
      } else {
        setStep('login');
      }
    }
  }, [authConfig, isLoading]);

  const changeForm = (newStep, emailValue = null) => {
    if (step !== newStep) {
      setAnimating(true);
      setPrevStep(step);
      window.setTimeout(() => {
        setStep(newStep);
        if (emailValue) setEmail(emailValue);
        window.setTimeout(() => {
          setAnimating(false);
        }, 50);
      }, 300);
    }
  };

  const handleVerificationSuccess = email => {
    changeForm('login', email);
  };

  const handleForgotPassword = email => {
    setEmail(email);
    changeForm('forgotPassword', email);
  };

  const handleResetCodeSent = email => {
    setEmail(email);
    changeForm('resetPassword', email);
  };

  const handlePasswordResetSuccess = () => {
    changeForm('login');
    window.setTimeout(() => {
      toast.success(
        t(
          'authPage.passwordResetSuccess',
          'Password has been reset successfully! You can now log in with your new password.'
        )
      );
    }, 500);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-purple-500" />
      </div>
    );
  }

  return (
    <div className="relative h-screen w-screen overflow-hidden">
      <div
        className="absolute inset-0 z-0"
        style={{ backgroundImage: 'linear-gradient(135deg, #8049f2, #b551c6)' }}
      />

      <ThreeBackground />

      <div className="absolute top-6 right-6 z-20">
        <SimpleLanguageSwitcher />
      </div>

      <div className="relative z-10 flex items-center justify-center h-full w-full">
        <div className="w-full max-w-5xl flex overflow-hidden rounded-lg shadow-2xl">
          <div className="w-1/2 p-12 flex flex-col justify-center relative">
            <div className="absolute inset-0 overflow-hidden">
              <AIBackground />
            </div>

            <h1 className="text-4xl font-bold text-white mb-4 relative z-10">
              {t('authPage.title', 'AI-Powered Test Evaluation')}
            </h1>
            <p className="text-gray-100 relative z-10">
              {t(
                'authPage.description',
                'Welcome to our intelligent testing platform that combines traditional assessment with AI evaluation. Create, assign, and grade tests with the help of advanced language models. Designed for educators and students to streamline the assessment process.'
              )}
            </p>
          </div>

          <div
            className="w-1/2 bg-white p-12 relative"
            style={{ height: '600px' }}
          >
            <div className="h-full relative">
              {animating && prevStep === 'login' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <Login
                    onSwitch={() => {}}
                    onLdapSwitch={() => {}}
                    onForgotPassword={() => {}}
                    prefillEmail={email}
                  />
                </div>
              )}
              {animating && prevStep === 'register' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <Register onSwitch={() => {}} onSuccess={() => {}} />
                </div>
              )}
              {animating && prevStep === 'verify' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <Verify email={email} onLoginRedirect={() => {}} />
                </div>
              )}
              {animating && prevStep === 'ldap' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <LdapLogin onSwitch={() => {}} />
                </div>
              )}
              {animating && prevStep === 'forgotPassword' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <ForgotPassword onSwitch={() => {}} onSuccess={() => {}} />
                </div>
              )}
              {animating && prevStep === 'resetPassword' && (
                <div className="absolute inset-0 transition-all duration-300 transform opacity-0 translate-x-12">
                  <ResetPassword email={email} onSuccess={() => {}} />
                </div>
              )}

              <div
                className={`absolute inset-0 transition-all duration-300 transform ${animating ? 'opacity-0 -translate-x-12' : 'opacity-100 translate-x-0'} overflow-y-auto`}
              >
                {step === 'login' && authConfig.standardAuthEnabled && (
                  <Login
                    onSwitch={() =>
                      authConfig.registrationEnabled && changeForm('register')
                    }
                    onLdapSwitch={() =>
                      authConfig.ldapAuthEnabled && changeForm('ldap')
                    }
                    onForgotPassword={handleForgotPassword}
                    prefillEmail={email}
                    showRegisterOption={authConfig.registrationEnabled}
                    showLdapOption={authConfig.ldapAuthEnabled}
                  />
                )}
                {step === 'register' && authConfig.registrationEnabled && (
                  <Register
                    onSwitch={() => changeForm('login')}
                    onSuccess={email => {
                      setEmail(email);
                      changeForm('verify', email);
                    }}
                  />
                )}
                {step === 'verify' && (
                  <Verify
                    email={email}
                    onLoginRedirect={handleVerificationSuccess}
                  />
                )}
                {step === 'ldap' && authConfig.ldapAuthEnabled && (
                  <LdapLogin
                    onSwitch={() =>
                      authConfig.standardAuthEnabled && changeForm('login')
                    }
                    showStandardLoginOption={authConfig.standardAuthEnabled}
                  />
                )}
                {step === 'forgotPassword' && (
                  <ForgotPassword
                    onSwitch={() => changeForm('login')}
                    onSuccess={handleResetCodeSent}
                  />
                )}
                {step === 'resetPassword' && (
                  <ResetPassword
                    email={email}
                    onSuccess={handlePasswordResetSuccess}
                  />
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
