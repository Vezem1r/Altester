import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ThreeBackground, AIBackground } from '@/components/auth';
import ProjectInfoModal from '@/components/auth/ProjectInfoModal';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';
import { IS_DEMO_MODE } from '@/services/apiUtils';
import { AuthService } from '@/services/AuthService';
import SimpleLanguageSwitcher from '@/components/common/SimpleLanguageSwitcher';

export default function AuthPage() {
  const { t } = useTranslation();
  const [showWelcomeModal, setShowWelcomeModal] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [loading, setLoading] = useState('');
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);
  const { isAuthenticated, userRole, isLoading, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 1024);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);

    return () => window.removeEventListener('resize', checkMobile);
  }, []);

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
    if (IS_DEMO_MODE) {
      const hasShownWelcome = localStorage.getItem('altester-welcome-shown');
      if (!hasShownWelcome) {
        setShowWelcomeModal(true);
        localStorage.setItem('altester-welcome-shown', 'true');
      }
    }
  }, []);

  const handleDemoLogin = async role => {
    if (!IS_DEMO_MODE) return;

    try {
      setLoading(role);
      const response = await AuthService.demoLogin(role);
      toast.success(
          t('demo.loginSuccess', 'Logged in as {{role}}', {
            role: role.charAt(0).toUpperCase() + role.slice(1),
          })
      );

      login({
        token: response.token,
        userRole: response.userRole,
      });
    } catch (error) {
      toast.error(
          error.message ||
          t('demo.loginError', 'Failed to login as {{role}}', { role })
      );
    } finally {
      setLoading('');
    }
  };

  if (isLoading) {
    return (
        <div className="flex items-center justify-center h-screen">
          <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-purple-500" />
        </div>
    );
  }

  const demoButtons = [
    {
      id: 'admin',
      label: t('demo.roles.admin', 'Admin'),
      description: t('demo.descriptions.admin', 'Full system access'),
      icon: (
          <svg
              className="w-6 h-6 lg:w-8 lg:h-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.031 9-11.622 0-1.042-.133-2.052-.382-3.016z"
            />
          </svg>
      ),
      color: 'from-purple-700 to-purple-900',
      isLogin: true,
    },
    {
      id: 'teacher',
      label: t('demo.roles.teacher', 'Teacher'),
      description: t('demo.descriptions.teacher', 'Create & manage tests'),
      icon: (
          <svg
              className="w-6 h-6 lg:w-8 lg:h-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
            />
          </svg>
      ),
      color: 'from-purple-600 to-purple-800',
      isLogin: true,
    },
    {
      id: 'student',
      label: t('demo.roles.student', 'Student'),
      description: t('demo.descriptions.student', 'Take tests & view results'),
      icon: (
          <svg
              className="w-6 h-6 lg:w-8 lg:h-8"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 14l9-5-9-5-9 5 9 5z"
            />
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"
            />
          </svg>
      ),
      color: 'from-purple-500 to-purple-700',
      isLogin: true,
    },
  ];

  if (isMobile) {
    return (
        <div className="relative min-h-screen w-screen overflow-x-hidden">
          <div
              className="absolute inset-0 z-0"
              style={{
                backgroundImage: 'linear-gradient(135deg, #8049f2, #b551c6)',
              }}
          />

          <div className="absolute top-4 right-4 z-20">
            <SimpleLanguageSwitcher />
          </div>

          <div className="relative z-10 min-h-screen flex flex-col p-4 pb-8">
            <div className="flex-shrink-0 text-center pt-8 pb-6">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-white/20 backdrop-blur-sm rounded-2xl mb-4 shadow-lg">
                <svg
                    className="w-8 h-8 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                  />
                </svg>
              </div>
              <h1 className="text-2xl font-bold text-white mb-2">
                {t('authPage.title', 'AI-Powered Test Evaluation')}
              </h1>
              <p className="text-white/90 text-sm px-4 leading-relaxed">
                {t(
                    'authPage.mobileDescription',
                    'Intelligent testing platform with AI evaluation capabilities'
                )}
              </p>
            </div>

            <div className="flex-1 flex flex-col justify-center max-w-sm mx-auto w-full">
              <div className="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl p-6 border border-white/20">
                <div className="text-center mb-6">
                  <h2 className="text-xl font-bold text-gray-800 mb-2">
                    {t('demo.title', 'Demo Access')}
                  </h2>
                  <p className="text-gray-600 text-sm">
                    {t('demo.mobileSubtitle', 'Choose your role to explore')}
                  </p>
                </div>

                <div className="space-y-3 mb-6">
                  {demoButtons.map(button => (
                      <button
                          key={button.id}
                          onClick={() => handleDemoLogin(button.id)}
                          disabled={loading !== ''}
                          className={`
                      group relative overflow-hidden rounded-xl p-4 transition-all duration-300 transform
                      ${
                              loading === button.id
                                  ? 'scale-95 shadow-inner'
                                  : 'hover:scale-102 active:scale-95'
                          }
                      disabled:opacity-50 disabled:cursor-not-allowed
                      bg-gradient-to-r ${button.color} text-white
                      w-full text-left shadow-lg
                    `}
                      >
                        <div className="relative flex items-center">
                          <div className="flex items-center justify-center w-10 h-10 bg-white/20 rounded-lg mr-3 backdrop-blur-sm flex-shrink-0">
                            {loading === button.id ? (
                                <div className="w-5 h-5 border-2 border-white/40 border-t-white rounded-full animate-spin"></div>
                            ) : (
                                <div className="text-white transition-transform duration-300 group-hover:scale-110">
                                  {button.icon}
                                </div>
                            )}
                          </div>

                          <div className="flex-1 min-w-0">
                            <h3 className="text-base font-semibold mb-0.5">
                              {button.label}
                            </h3>
                            <p className="text-white/90 text-xs truncate">
                              {button.description}
                            </p>
                          </div>

                          <svg
                              className="w-4 h-4 text-white/70 transition-transform duration-300 group-hover:translate-x-0.5 flex-shrink-0 ml-2"
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                          >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M9 5l7 7-7 7"
                            />
                          </svg>
                        </div>

                        <div className="absolute inset-0 bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                      </button>
                  ))}
                </div>

                <button
                    onClick={() => setIsProjectModalOpen(true)}
                    className="w-full py-3 px-4 border-2 border-purple-600 text-purple-600 font-medium rounded-xl hover:bg-purple-50 active:bg-purple-100 transition-all duration-200 flex items-center justify-center group"
                >
                  <svg
                      className="w-4 h-4 mr-2 transition-transform duration-300 group-hover:scale-110"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                  >
                    <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  <span className="text-sm">
                  {t('demo.about', 'About Project')}
                </span>
                </button>
              </div>
            </div>

            <div className="flex-shrink-0 h-8"></div>
          </div>

          <ProjectInfoModal
              isOpen={showWelcomeModal || isProjectModalOpen}
              onClose={() => {
                setShowWelcomeModal(false);
                setIsProjectModalOpen(false);
              }}
          />
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

        <div className="relative z-10 flex items-center justify-center h-full w-full p-6">
          <div className="w-full max-w-7xl flex flex-col">
            <div className="flex flex-col overflow-hidden rounded-lg shadow-2xl">
              <div className="flex flex-row flex-1 min-h-[600px]">
                <div className="w-1/2 p-12 flex flex-col justify-center relative bg-gradient-to-br from-purple-600/20 to-purple-800/20 backdrop-blur-sm">
                  <div className="absolute inset-0 overflow-hidden">
                    <AIBackground />
                  </div>

                  <h1 className="text-4xl font-bold text-white mb-4 relative z-10">
                    {t('authPage.title', 'AI-Powered Test Evaluation')}
                  </h1>
                  <p className="text-gray-100 relative z-10 text-base mb-8">
                    {t(
                        'authPage.description',
                        'Welcome to our intelligent testing platform that combines traditional assessment with AI evaluation. Create, assign, and grade tests with the help of advanced language models. Designed for educators and students to streamline the assessment process.'
                    )}
                  </p>
                  <div className="relative z-10 space-y-3">
                    <div className="flex items-center text-white/90">
                      <svg
                          className="w-5 h-5 mr-3 text-purple-300"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                      >
                        <path
                            fillRule="evenodd"
                            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                            clipRule="evenodd"
                        />
                      </svg>
                      {t('features.aiGrading', 'AI-Powered Automated Grading')}
                    </div>
                    <div className="flex items-center text-white/90">
                      <svg
                          className="w-5 h-5 mr-3 text-purple-300"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                      >
                        <path
                            fillRule="evenodd"
                            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                            clipRule="evenodd"
                        />
                      </svg>
                      {t(
                          'features.realTime',
                          'Real-time Communication & Notifications'
                      )}
                    </div>
                    <div className="flex items-center text-white/90">
                      <svg
                          className="w-5 h-5 mr-3 text-purple-300"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                      >
                        <path
                            fillRule="evenodd"
                            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                            clipRule="evenodd"
                        />
                      </svg>
                      {t('features.microservices', 'Microservices Architecture')}
                    </div>
                  </div>
                </div>
                <div className="w-1/2 bg-white relative">
                  <div className="p-12 h-full flex flex-col">
                    <div className="text-center mb-8">
                      <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-purple-600 to-pink-600 rounded-xl mb-4 shadow-lg">
                        <svg
                            className="w-8 h-8 text-white"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                          <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                          />
                        </svg>
                      </div>
                      <h2 className="text-3xl font-bold text-gray-800 mb-2">
                        {t('demo.title', 'Demo Access')}
                      </h2>
                      <p className="text-gray-600">
                        {t(
                            'demo.subtitle',
                            'Experience the platform instantly with different user roles'
                        )}
                      </p>
                    </div>

                    <div className="flex-1 flex flex-col justify-center">
                      <div className="space-y-4 mb-8">
                        {demoButtons.map(button => (
                            <button
                                key={button.id}
                                onClick={() => handleDemoLogin(button.id)}
                                disabled={loading !== ''}
                                className={`
                            group relative overflow-hidden rounded-xl p-6 transition-all duration-300 transform
                            ${
                                    loading === button.id
                                        ? 'scale-95 shadow-inner'
                                        : 'hover:scale-105 hover:shadow-xl'
                                }
                            disabled:opacity-50 disabled:cursor-not-allowed
                            bg-gradient-to-r ${button.color} text-white
                            w-full text-left
                          `}
                            >
                              <div className="relative flex items-center">
                                <div className="flex items-center justify-center w-12 h-12 bg-white/20 rounded-lg mr-4 backdrop-blur-sm">
                                  {loading === button.id ? (
                                      <div className="w-6 h-6 border-2 border-white/40 border-t-white rounded-full animate-spin"></div>
                                  ) : (
                                      <div className="text-white transition-transform duration-300 group-hover:scale-110">
                                        {button.icon}
                                      </div>
                                  )}
                                </div>

                                <div className="flex-1">
                                  <h3 className="text-lg font-semibold mb-1">
                                    {button.label}
                                  </h3>
                                  <p className="text-white/90 text-sm">
                                    {button.description}
                                  </p>
                                </div>

                                <svg
                                    className="w-5 h-5 text-white/70 transition-transform duration-300 group-hover:translate-x-1"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                >
                                  <path
                                      strokeLinecap="round"
                                      strokeLinejoin="round"
                                      strokeWidth="2"
                                      d="M9 5l7 7-7 7"
                                  />
                                </svg>
                              </div>

                              <div className="absolute inset-0 bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                            </button>
                        ))}
                      </div>

                      <div className="border-t border-gray-200 pt-6">
                        <button
                            onClick={() => setIsProjectModalOpen(true)}
                            className="w-full py-3 px-4 border-2 border-purple-600 text-purple-600 font-medium rounded-xl hover:bg-purple-50 transition-all duration-200 flex items-center justify-center group"
                        >
                          <svg
                              className="w-5 h-5 mr-2 transition-transform duration-300 group-hover:scale-110"
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                          >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                            />
                          </svg>
                          {t('demo.about', 'About This Project')}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <ProjectInfoModal
            isOpen={showWelcomeModal || isProjectModalOpen}
            onClose={() => {
              setShowWelcomeModal(false);
              setIsProjectModalOpen(false);
            }}
        />
      </div>
  );
}
