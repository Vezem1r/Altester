import { useState } from 'react';
import { toast } from 'react-toastify';
import { AuthService } from '@/services/AuthService';
import { useAuth } from '@/context/AuthContext';
import { IS_DEMO_MODE } from '@/services/apiUtils';
import ProjectInfoModal from './ProjectInfoModal';

export default function DemoLoginBlock() {
  const [loading, setLoading] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { login } = useAuth();

  const handleDemoLogin = async (role) => {
    if (!IS_DEMO_MODE) return;

    try {
      setLoading(role);
      const response = await AuthService.demoLogin(role);
      toast.success(`Logged in as ${role.charAt(0).toUpperCase() + role.slice(1)}`);

      login({
        token: response.token,
        userRole: response.userRole,
      });
    } catch (error) {
      toast.error(error.message || `Failed to login as ${role}`);
    } finally {
      setLoading('');
    }
  };

  if (!IS_DEMO_MODE) return null;

  const roles = [
    { 
      id: 'admin', 
      label: 'Admin',
      icon: 'M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z'
    },
    { 
      id: 'teacher', 
      label: 'Teacher',
      icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'
    },
    { 
      id: 'student', 
      label: 'Student',
      icon: 'M12 14l9-5-9-5-9 5 9 5z M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z'
    }
  ];

  return (
    <>
      <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2">
        <div className="bg-white/5 backdrop-blur-md rounded-full p-1.5 border border-white/10 shadow-lg">
          <div className="flex items-center space-x-2">
            <div className="text-white/60 text-xs font-medium px-3 py-1.5">
              Demo Access
            </div>
            
            <div className="h-4 w-px bg-white/20"></div>
            
            <div className="flex space-x-1">
              {roles.map((role) => (
                <button
                  key={role.id}
                  onClick={() => handleDemoLogin(role.id)}
                  disabled={loading !== ''}
                  className={`
                    group relative px-3 py-1.5 rounded-full transition-all duration-200
                    ${loading === role.id 
                      ? 'bg-purple-600/80 text-white' 
                      : 'hover:bg-white/10 text-white/80 hover:text-white'
                    }
                    disabled:opacity-50 disabled:cursor-not-allowed
                  `}
                >
                  <div className="flex items-center space-x-2">
                    {loading === role.id ? (
                      <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                    ) : (
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d={role.icon} />
                      </svg>
                    )}
                    <span className="text-xs font-medium">{role.label}</span>
                  </div>
                  
                  {/* Tooltip */}
                  <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none">
                    <div className="bg-gray-900 text-white text-xs rounded-lg px-2 py-1 whitespace-nowrap">
                      Quick login as {role.label}
                      <div className="absolute top-full left-1/2 transform -translate-x-1/2 -mt-1">
                        <div className="border-4 border-transparent border-t-gray-900"></div>
                      </div>
                    </div>
                  </div>
                </button>
              ))}
            </div>
            
            <div className="h-4 w-px bg-white/20"></div>
            
            <button
              onClick={() => setIsModalOpen(true)}
              className="p-1.5 rounded-full hover:bg-white/10 text-white/60 hover:text-white transition-all duration-200"
              title="About Altester"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </button>
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