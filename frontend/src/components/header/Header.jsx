import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useTranslation } from 'react-i18next';
import SettingsModal from '@/components/settings/SettingsModal';
import NotificationIcon from '@/components/common/NotificationIcon';
import ChatIcon from '@/components/chat/ChatIcon';
import LanguageSwitcher from '@/components/common/LanguageSwitcher';

const Header = ({
  user: propUser,
  onLogout,
  resetToCurrentSemester,
  logoText = 'AITester',
  onLogoClick = null,
}) => {
  const [settingsModalOpen, setSettingsModalOpen] = useState(false);
  const { t } = useTranslation();

  const authContext = useAuth();

  const displayUser = propUser || authContext.user;

  const isUserRegistered = propUser?.registered === true;

  useEffect(() => {
    if (settingsModalOpen) {
      document.body.classList.add('overflow-hidden');
    } else {
      document.body.classList.remove('overflow-hidden');
    }

    return () => {
      document.body.classList.remove('overflow-hidden');
    };
  }, [settingsModalOpen]);

  const openSettingsModal = () => {
    setSettingsModalOpen(true);
  };

  const closeSettingsModal = () => {
    setSettingsModalOpen(false);
  };

  const handleLogoClick = () => {
    if (resetToCurrentSemester) {
      resetToCurrentSemester();
    } else if (onLogoClick) {
      onLogoClick();
    }
  };

  return (
    <>
      <header className="bg-white shadow-sm w-full">
        <div className="w-full px-0">
          <div className="flex h-16 justify-between">
            <div className="flex items-center flex-shrink-0 pl-2 sm:pl-4">
              <button
                onClick={handleLogoClick}
                className="text-xl font-bold text-purple-600 hover:text-purple-800 focus:outline-none"
              >
                {t('common.appName', logoText)}
              </button>
            </div>

            <div className="flex items-center space-x-4 sm:space-x-6 pr-2 sm:pr-4">
              <div className="hidden md:block text-sm font-medium text-gray-700">
                {displayUser?.name || ''} {displayUser?.surname || ''}
                <span className="ml-1 text-xs font-normal text-gray-500">
                  {displayUser?.username || authContext.userRole || ''}
                </span>
              </div>

              <NotificationIcon />

              <ChatIcon />

              <LanguageSwitcher />

              {isUserRegistered && (
                <button
                  type="button"
                  onClick={openSettingsModal}
                  className="relative bg-white p-1 rounded-full text-gray-500 hover:text-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500"
                >
                  <span className="sr-only">{t('header.settings')}</span>
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
                      d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                    />
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                </button>
              )}

              {/* Logout button */}
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

      {settingsModalOpen && (
        <SettingsModal
          isOpen={settingsModalOpen}
          onClose={closeSettingsModal}
          userEmail={displayUser?.email}
        />
      )}
    </>
  );
};

export default Header;
