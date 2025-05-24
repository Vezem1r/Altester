import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

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

  return (
    <div className="bg-white shadow-lg rounded-xl overflow-hidden mb-6">
      <div className="bg-gradient-to-r from-purple-600 to-purple-700 px-6 py-5">
        <div className="flex flex-col">
          <div className="flex justify-between items-start mb-3">
            <div className="flex items-center">
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
              <h1 className="text-2xl font-bold text-white break-all">
                {title}
              </h1>
            </div>

            <div className="flex flex-wrap gap-2">
              {onSettingsClick && (
                <button
                  onClick={onSettingsClick}
                  className="inline-flex items-center px-3 py-2 border border-purple-200 text-sm font-medium rounded-lg shadow-sm bg-white text-purple-700 hover:bg-purple-50 transition-colors"
                >
                  <svg
                    className="h-4 w-4 mr-1.5"
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
                  {t('pageHeader.settings', 'Settings')}
                </button>
              )}

              {actions.map((action, index) => (
                <button
                  key={index}
                  onClick={action.onClick}
                  className={
                    action.primary
                      ? 'inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg shadow-sm text-white bg-purple-600 hover:bg-purple-700 transition-colors'
                      : 'inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-lg bg-white hover:bg-gray-50 transition-colors'
                  }
                >
                  {action.icon && <span className="mr-1.5">{action.icon}</span>}
                  {action.text}
                </button>
              ))}

              {onDeleteClick && (
                <button
                  onClick={onDeleteClick}
                  className="inline-flex items-center px-3 py-2 border border-red-300 text-sm font-medium rounded-lg bg-white hover:bg-red-50 text-red-600 transition-colors"
                >
                  <svg
                    className="h-4 w-4 mr-1.5"
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
                  {t('pageHeader.delete', 'Delete')}
                </button>
              )}
            </div>
          </div>

          {description && (
            <div className="w-full">
              <p className="text-sm text-purple-100 break-words whitespace-pre-wrap">
                {description}
              </p>
            </div>
          )}

          <div className="flex flex-wrap gap-2 mt-3">
            {badges.map((badge, index) => (
              <span
                key={index}
                className={`px-2.5 py-1 text-xs font-medium rounded-full ${badge.className || 'bg-purple-200 text-purple-800'}`}
              >
                {badge.text}
              </span>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PageHeader;
