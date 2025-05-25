import { useState, useRef, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

const LanguageSwitcher = () => {
  const { i18n, t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const languages = [
    { code: 'en', label: 'English', shortLabel: 'EN' },
    { code: 'cs', label: 'Čeština', shortLabel: 'CS' },
  ];

  const currentLanguage =
    languages.find(lang => lang.code === i18n.language) || languages[0];

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [dropdownRef]);

  const changeLanguage = code => {
    i18n.changeLanguage(code);
    setIsOpen(false);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={`
          group relative inline-flex items-center justify-center
          px-4 py-2 text-sm font-medium rounded-xl
          bg-gradient-to-r from-purple-50 to-indigo-50
          border border-purple-200/50
          text-purple-700 
          hover:from-purple-100 hover:to-indigo-100
          hover:border-purple-300
          hover:text-purple-800
          focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
          transition-all duration-200 ease-in-out
          transform hover:scale-105 hover:shadow-lg
          ${isOpen ? 'from-purple-100 to-indigo-100 border-purple-300 shadow-lg scale-105' : ''}
        `}
        title={t('header.switchLanguage', 'Switch Language')}
      >
        {/* Globe Icon */}
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className={`h-5 w-5 mr-2 transition-transform duration-200 ${isOpen ? 'rotate-12' : 'group-hover:rotate-6'}`}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
          />
        </svg>

        {/* Current Language */}
        <span className="font-semibold tracking-wide">
          {currentLanguage.shortLabel}
        </span>

        {/* Chevron */}
        <svg
          className={`ml-2 h-4 w-4 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fillRule="evenodd"
            d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
            clipRule="evenodd"
          />
        </svg>
      </button>

      {/* Dropdown Menu */}
      <div
        className={`
        absolute right-0 mt-3 w-52 
        bg-white rounded-2xl shadow-2xl ring-1 ring-black/5
        transform transition-all duration-200 ease-out origin-top-right
        ${
          isOpen
            ? 'opacity-100 scale-100 translate-y-0'
            : 'opacity-0 scale-95 translate-y-2 pointer-events-none'
        }
        z-50 overflow-hidden backdrop-blur-sm
      `}
      >
        {/* Dropdown Header */}
        <div className="px-4 py-3 bg-gradient-to-r from-purple-50 to-indigo-50 border-b border-purple-100">
          <p className="text-xs font-medium text-purple-600 uppercase tracking-wider">
            {t('header.selectLanguage', 'Select Language')}
          </p>
        </div>

        {/* Language Options */}
        <div className="py-2">
          {languages.map((language, index) => (
            <button
              key={language.code}
              onClick={() => changeLanguage(language.code)}
              className={`
                group w-full flex items-center px-4 py-3 text-sm
                transition-all duration-150 ease-in-out
                ${
                  language.code === i18n.language
                    ? 'bg-gradient-to-r from-purple-50 to-indigo-50 text-purple-700 font-medium'
                    : 'text-gray-700 hover:bg-gradient-to-r hover:from-purple-25 hover:to-indigo-25 hover:text-purple-600'
                }
                ${index !== languages.length - 1 ? 'border-b border-gray-50' : ''}
              `}
            >
              {/* Language Code Badge */}
              <div
                className={`
                flex items-center justify-center w-8 h-8 rounded-lg mr-3
                font-bold text-xs tracking-wider
                transition-all duration-150
                ${
                  language.code === i18n.language
                    ? 'bg-gradient-to-br from-purple-500 to-indigo-600 text-white shadow-md'
                    : 'bg-gray-100 text-gray-600 group-hover:bg-gradient-to-br group-hover:from-purple-400 group-hover:to-indigo-500 group-hover:text-white'
                }
              `}
              >
                {language.shortLabel}
              </div>

              {/* Language Name */}
              <span className="flex-1 text-left font-medium">
                {language.label}
              </span>

              {/* Active Indicator */}
              {language.code === i18n.language && (
                <div className="flex items-center">
                  <svg
                    className="h-4 w-4 text-purple-600"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2.5}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
              )}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LanguageSwitcher;
