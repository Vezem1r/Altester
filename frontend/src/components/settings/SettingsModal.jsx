import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import ResetPassword from './ResetPassword';
import ChangeEmail from './ChangeEmail';

const SettingsModal = ({ isOpen, onClose, userEmail }) => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState('password');

  const handleSuccess = () => {
    setTimeout(() => {
      onClose();
    }, 1500);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 transition-opacity" aria-hidden="true">
          <div
            className="absolute inset-0 bg-gray-500 opacity-75"
            onClick={onClose}
          />
        </div>

        <span
          className="hidden sm:inline-block sm:align-middle sm:h-screen"
          aria-hidden="true"
        >
          &#8203;
        </span>
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-xl sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-8">
            <div className="absolute top-0 right-0 pt-4 pr-4">
              <button
                type="button"
                className="text-gray-400 hover:text-gray-500 focus:outline-none"
                onClick={onClose}
                aria-label={t('settingsModal.close', 'Close')}
              >
                <span className="sr-only">
                  {t('settingsModal.close', 'Close')}
                </span>
                <svg
                  className="h-6 w-6"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  aria-hidden="true"
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

            <div className="sm:flex sm:items-start">
              <div className="mt-3 text-center sm:mt-0 sm:text-left w-full">
                <h3 className="text-lg leading-6 font-medium text-gray-900 text-center mb-4">
                  {t('settingsModal.accountSettings', 'Account Settings')}
                </h3>
              </div>
            </div>

            <div className="flex w-full border-b border-gray-200 mb-6">
              <button
                onClick={() => setActiveTab('password')}
                className={`flex-1 py-3 text-center font-medium transition-colors ${
                  activeTab === 'password'
                    ? 'text-purple-600 border-b-2 border-purple-500'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                {t('settingsModal.changePassword', 'Change Password')}
              </button>
              <button
                onClick={() => setActiveTab('email')}
                className={`flex-1 py-3 text-center font-medium transition-colors ${
                  activeTab === 'email'
                    ? 'text-purple-600 border-b-2 border-purple-500'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                {t('settingsModal.changeEmail', 'Change Email')}
              </button>
            </div>

            <div className="min-h-[400px] flex flex-col">
              {activeTab === 'password' ? (
                <ResetPassword email={userEmail} onSuccess={handleSuccess} />
              ) : (
                <ChangeEmail
                  onSuccess={handleSuccess}
                  currentEmail={userEmail}
                  onCancel={onClose}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SettingsModal;
