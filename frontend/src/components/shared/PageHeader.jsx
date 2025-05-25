import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';

const PageHeader = ({
                      title,
                      description,
                      backUrl,
                      badges = [],
                      actions = [],
                      onSettingsClick,
                      onDeleteClick,
                    }) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [showMobileMenu, setShowMobileMenu] = useState(false);

  const allActions = [
    ...(onSettingsClick
        ? [
          {
            text: t('pageHeader.settings', 'Settings'),
            onClick: onSettingsClick,
            icon: (
                <svg
                    className="h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
                  />
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
            ),
            variant: 'secondary',
          },
        ]
        : []),
    ...actions,
    ...(onDeleteClick
        ? [
          {
            text: t('pageHeader.delete', 'Delete'),
            onClick: onDeleteClick,
            icon: (
                <svg
                    className="h-4 w-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                  <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
            ),
            variant: 'danger',
          },
        ]
        : []),
  ];

  const getActionButtonClass = action => {
    if (action.variant === 'danger') {
      return 'inline-flex items-center px-3 py-2 border border-red-300 text-sm font-medium rounded-lg bg-white hover:bg-red-50 text-red-600 transition-colors';
    }
    if (action.primary) {
      return 'inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg shadow-sm text-white bg-purple-600 hover:bg-purple-700 transition-colors';
    }
    return 'inline-flex items-center px-3 py-2 border border-purple-200 text-sm font-medium rounded-lg shadow-sm bg-white text-purple-700 hover:bg-purple-50 transition-colors';
  };

  return (
      <div className="bg-white shadow-lg rounded-xl overflow-hidden mb-6">
        <div className="bg-gradient-to-r from-purple-600 to-purple-700 px-4 sm:px-6 py-5">
          <div className="flex flex-col">
            {/* Header Top Row */}
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center min-w-0 flex-1">
                {backUrl && (
                    <button
                        onClick={() => navigate(backUrl)}
                        className="mr-3 p-1 text-white hover:text-purple-200 transition-colors flex-shrink-0"
                    >
                      <svg
                          className="h-5 w-5"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                      >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M10 19l-7-7m0 0l7-7m-7 7h18"
                        />
                      </svg>
                    </button>
                )}
                <h1 className="text-xl sm:text-2xl font-bold text-white break-words min-w-0">
                  {title}
                </h1>
              </div>

              {/* Desktop Actions */}
              <div className="hidden lg:flex flex-wrap gap-2 ml-4">
                {allActions.map((action, index) => (
                    <button
                        key={index}
                        onClick={action.onClick}
                        className={getActionButtonClass(action)}
                    >
                      {action.icon && <span className="mr-1.5">{action.icon}</span>}
                      {action.text}
                    </button>
                ))}
              </div>

              {/* Mobile Actions Menu */}
              {allActions.length > 0 && (
                  <div className="lg:hidden relative ml-4">
                    <button
                        onClick={() => setShowMobileMenu(!showMobileMenu)}
                        className="inline-flex items-center px-3 py-2 border border-purple-200 text-sm font-medium rounded-lg shadow-sm bg-white text-purple-700 hover:bg-purple-50 transition-colors"
                    >
                      <svg
                          className="h-4 w-4"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                      >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
                        />
                      </svg>
                    </button>

                    {/* Mobile Dropdown Menu */}
                    {showMobileMenu && (
                        <div className="absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-50">
                          <div className="py-1">
                            {allActions.map((action, index) => (
                                <button
                                    key={index}
                                    onClick={() => {
                                      action.onClick();
                                      setShowMobileMenu(false);
                                    }}
                                    className={`
                            w-full text-left px-4 py-2 text-sm flex items-center hover:bg-gray-100 transition-colors
                            ${action.variant === 'danger' ? 'text-red-600 hover:bg-red-50' : 'text-gray-700'}
                          `}
                                >
                                  {action.icon && (
                                      <span className="mr-2">{action.icon}</span>
                                  )}
                                  {action.text}
                                </button>
                            ))}
                          </div>
                        </div>
                    )}
                  </div>
              )}
            </div>

            {/* Description */}
            {description && (
                <div className="w-full mb-3">
                  <p className="text-sm text-purple-100 break-words whitespace-pre-wrap">
                    {description}
                  </p>
                </div>
            )}

            {/* Badges */}
            {badges.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {badges.map((badge, index) => (
                      <span
                          key={index}
                          className={`px-2.5 py-1 text-xs font-medium rounded-full ${badge.className || 'bg-purple-200 text-purple-800'}`}
                      >
                  {badge.text}
                </span>
                  ))}
                </div>
            )}
          </div>
        </div>

        {allActions.length > 0 && (
            <div className="hidden md:flex lg:hidden px-4 py-3 bg-gray-50 border-t border-gray-200 gap-2 overflow-x-auto">
              {allActions.map((action, index) => (
                  <button
                      key={index}
                      onClick={action.onClick}
                      className={`${getActionButtonClass(action)} whitespace-nowrap`}
                  >
                    {action.icon && <span className="mr-1.5">{action.icon}</span>}
                    {action.text}
                  </button>
              ))}
            </div>
        )}

        {showMobileMenu && (
            <div
                className="fixed inset-0 z-40"
                onClick={() => setShowMobileMenu(false)}
            />
        )}
      </div>
  );
};

export default PageHeader;
