import { useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';

const ModalTabNavigation = ({ tabs, activeTab, onChange }) => {
  const { t } = useTranslation();

  const handleTabChange = useCallback(
    tabId => {
      onChange(tabId);
    },
    [onChange]
  );

  const TRANSLATION_KEYS = {
    questions: 'modalTabNavigation.questions',
    preview: 'modalTabNavigation.preview',
    attempts: 'modalTabNavigation.attempts',
    settings: 'modalTabNavigation.settings',
    analytics: 'modalTabNavigation.analytics',
  };

  const tabButtons = useMemo(() => {
    return tabs.map(tab => {
      const translationKey =
        TRANSLATION_KEYS[tab.id] || `modalTabNavigation.${tab.id}`;
      const translatedLabel =
        typeof tab.label === 'string'
          ? t(translationKey, { defaultValue: tab.label })
          : tab.label;

      const isActive = activeTab === tab.id;

      return (
        <button
          key={tab.id}
          onClick={() => handleTabChange(tab.id)}
          className={`
            relative flex items-center justify-center px-4 py-4 transition-all duration-300 flex-1
            ${
              isActive
                ? 'border-b-2 border-purple-500 text-purple-600 bg-gradient-to-b from-purple-50 to-purple-100 shadow-inner'
                : 'text-gray-600 hover:text-gray-800 hover:bg-gray-50 hover:border-b-2 hover:border-purple-200 border-b-2 border-transparent'
            }
            font-medium text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-inset
          `}
          aria-selected={isActive}
          role="tab"
          tabIndex={isActive ? 0 : -1}
        >
          {tab.icon && (
            <svg
              className={`w-5 h-5 mr-2 transition-transform duration-300 ${isActive ? 'scale-110' : ''}`}
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d={tab.icon}
              />
            </svg>
          )}
          {translatedLabel}
          {isActive && (
            <span
              className="absolute bottom-0 left-0 w-full h-0.5 bg-gradient-to-r from-purple-400 to-purple-600"
              aria-hidden="true"
            />
          )}
        </button>
      );
    });
  }, [tabs, activeTab, handleTabChange, t]);

  return (
    <div className="border-b border-gray-200 -mx-6 sm:-mx-8 overflow-hidden">
      <nav
        className="flex w-full shadow-sm"
        role="tablist"
        aria-label="Modal navigation tabs"
      >
        {tabButtons}
      </nav>
    </div>
  );
};

export default ModalTabNavigation;
