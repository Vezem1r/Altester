import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@/context/AuthContext';
import AdminSidebar from '@/components/sidebar/AdminSidebar';
import AdminHeader from '@/components/header/AdminHeader';
import { AdminService } from '@/services/AdminService';

const AdminLayout = ({ children }) => {
  const { t } = useTranslation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [_adminInfo, setAdminInfo] = useState({
    username: '',
  });
  const [_loading, setLoading] = useState(true);
  const { logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchAdminInfo = async () => {
      try {
        const data = await AdminService.getAdminStats();
        setAdminInfo({
          username: data.username || t('adminLayoutAdmin', 'Admin'),
        });
      } catch {
      } finally {
        setLoading(false);
      }
    };

    fetchAdminInfo();
  }, [t]);

  const toggleSidebar = () => setSidebarOpen(!sidebarOpen);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleOverlayClick = e => {
    if (e.key && e.key !== 'Enter' && e.key !== ' ') {
      return;
    }
    toggleSidebar();
  };

  return (
    <div className="flex flex-col h-screen bg-gray-100">
      <AdminHeader onLogout={handleLogout} />

      <div className="flex flex-1 overflow-hidden">
        <div className="hidden md:flex md:flex-shrink-0">
          <div className="flex flex-col w-64">
            <div className="flex flex-col flex-grow pt-2 pb-4 overflow-y-auto bg-white border-r border-gray-200">
              <div className="mt-1 flex-grow flex flex-col">
                <AdminSidebar />
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col w-0 flex-1 overflow-hidden">
          <div className="md:hidden pl-1 pt-1 sm:pl-3 sm:pt-3 bg-white shadow-sm">
            <button
              onClick={toggleSidebar}
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-600 hover:text-purple-600 hover:bg-gray-100 focus:outline-none"
              aria-label={t('adminLayoutMenu', 'Menu')}
            >
              <svg
                className="h-6 w-6"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>

          {sidebarOpen && (
            <div className="md:hidden">
              <div className="fixed inset-0 z-40 flex">
                <div className="fixed inset-0">
                  <button
                    type="button"
                    className="absolute inset-0 bg-gray-600 opacity-75 w-full h-full cursor-pointer focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
                    onClick={handleOverlayClick}
                    onKeyDown={handleOverlayClick}
                    aria-label={t('adminLayoutCloseSidebar', 'Close sidebar')}
                  />
                </div>
                <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white">
                  <div className="pt-2 pb-4">
                    <div className="flex items-center px-4 justify-end">
                      <button
                        onClick={toggleSidebar}
                        className="flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                        aria-label={t('adminLayoutClose', 'Close')}
                      >
                        <svg
                          className="h-6 w-6 text-gray-600"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </button>
                    </div>
                    <div className="mt-2">
                      <AdminSidebar />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          <main className="flex-1 relative overflow-y-auto focus:outline-none">
            <div className="py-4">
              <div className="px-4 sm:px-6 lg:px-8">{children}</div>
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default AdminLayout;
