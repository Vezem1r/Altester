import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import NotificationIcon from '@/components/common/NotificationIcon';
import LanguageSwitcher from '@/components/common/LanguageSwitcher';

const AdminHeader = ({ onLogout }) => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogoClick = () => {
    navigate('/admin');
  };

  return (
    <header className="bg-white shadow-sm w-full">
      <div className="w-full px-0">
        <div className="flex h-16 justify-between">
          <div className="flex items-center flex-shrink-0 pl-2 sm:pl-4">
            <button
              onClick={handleLogoClick}
              className="text-xl font-bold text-purple-600 hover:text-purple-800 focus:outline-none"
            >
              {t('common.adminDashboard', 'AITester Admin')}
            </button>
          </div>

          <div className="flex items-center space-x-4 sm:space-x-6 pr-2 sm:pr-4">
            <NotificationIcon />

            <LanguageSwitcher />

            <button
              onClick={onLogout}
              className="flex items-center bg-white px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
            >
              <svg
                className="mr-1.5 h-4 w-4 text-gray-500"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                />
              </svg>
              {t('header.logout')}
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default AdminHeader;
