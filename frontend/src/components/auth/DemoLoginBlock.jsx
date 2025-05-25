import { useState } from 'react';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';
import { AuthService } from '@/services/AuthService';
import { useAuth } from '@/context/AuthContext';
import { IS_DEMO_MODE } from '@/services/apiUtils';
import ProjectInfoModal from './ProjectInfoModal';

export default function DemoLoginBlock() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { login } = useAuth();

  const handleDemoLogin = async (role) => {
    if (!IS_DEMO_MODE) return;

    try {
      setLoading(role);
      const response = await AuthService.demoLogin(role);
      toast.success(t('demo.loginSuccess', 'Logged in as {{role}}', {
        role: role.charAt(0).toUpperCase() + role.slice(1)
      }));

      login({
        token: response.token,
        userRole: response.userRole,
      });
    } catch (error) {
      toast.error(error.message || t('demo.loginError', 'Failed to login as {{role}}', { role }));
    } finally {
      setLoading('');
    }
  };

  if (!IS_DEMO_MODE) return null;

  const buttons = [
    {
      id: 'admin',
      label: t('demo.roles.admin', 'Admin'),
      icon: 'M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      isLogin: true
    },
    {
      id: 'teacher',
      label: t('demo.roles.teacher', 'Teacher'),
      icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253',
      isLogin: true
    },
    {
      id: 'student',
      label: t('demo.roles.student', 'Student'),
      icon: 'M12 14l9-5-9-5-9 5 9 5z M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z',
      isLogin: true
    },
    {
      id: 'info',
      label: t('demo.about', 'About'),
      icon: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
      isLogin: false
    }
  ];

  return (
      <>
        <div className="w-full bg-gradient-to-br from-purple-600/20 to-blue-600/20 backdrop-blur-sm rounded-b-lg shadow-2xl border-t border-white/10 p-4 sm:p-6">
          <div className="flex flex-col sm:flex-row items-center justify-between space-y-3 sm:space-y-0 sm:space-x-4">
            <div className="text-center sm:text-left">
              <h3 className="text-lg sm:text-xl font-bold text-white mb-1">
                {t('demo.title', 'Demo Access')}
              </h3>
              <p className="text-gray-100 text-sm opacity-90">
                {t('demo.subtitle', 'Experience the platform instantly')}
              </p>
            </div>

            <div className="flex gap-3 sm:gap-4">
              {buttons.map((button) => (
                  <button
                      key={button.id}
                      onClick={() => button.isLogin ? handleDemoLogin(button.id) : setIsModalOpen(true)}
                      disabled={loading !== ''}
                      className={`
                  group relative overflow-hidden rounded-lg transition-all duration-300 transform
                  ${loading === button.id
                          ? 'bg-white/20 shadow-inner scale-95'
                          : 'bg-white/10 hover:bg-white/20 hover:shadow-lg hover:scale-105'
                      }
                  disabled:opacity-50 disabled:cursor-not-allowed
                  backdrop-blur-sm border border-white/20
                  w-16 h-16 sm:w-20 sm:h-20
                `}
                  >
                    <div className="relative flex flex-col items-center justify-center h-full">
                      <div className="flex items-center justify-center transition-all duration-300 group-hover:scale-110">
                        {loading === button.id ? (
                            <div className="w-5 h-5 sm:w-6 sm:h-6 border-2 border-white/40 border-t-white rounded-full animate-spin"></div>
                        ) : (
                            <svg className="w-5 h-5 sm:w-6 sm:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={button.icon} />
                            </svg>
                        )}
                      </div>

                      <span className="text-white font-medium text-xs mt-1 transition-all duration-300 group-hover:text-gray-100">
                    {button.label}
                  </span>
                    </div>

                    <div className="absolute inset-0 bg-gradient-to-br from-purple-500/20 to-blue-500/20 opacity-0 group-hover:opacity-100 transition-all duration-300"></div>
                  </button>
              ))}
            </div>
          </div>
        </div>

        <ProjectInfoModal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
        />
      </>
  );
}