import { useTranslation } from 'react-i18next';

const SimpleLanguageSwitcher = () => {
  const { i18n } = useTranslation();

  const languages = [
    { code: 'en', label: 'EN' },
    { code: 'cs', label: 'CS' },
  ];

  const changeLanguage = code => {
    i18n.changeLanguage(code);
  };

  return (
    <div className="flex items-center bg-white/10 backdrop-blur-sm rounded-lg p-1 border border-white/20">
      {languages.map((language, index) => (
        <button
          key={language.code}
          onClick={() => changeLanguage(language.code)}
          className={`
            px-3 py-1.5 text-xs font-medium rounded-md transition-all duration-200
            ${
              language.code === i18n.language
                ? 'bg-white text-purple-700 shadow-sm'
                : 'text-white/80 hover:text-white hover:bg-white/10'
            }
            ${index !== 0 ? 'ml-1' : ''}
          `}
          title={`Switch to ${language.code.toUpperCase()}`}
        >
          {language.label}
        </button>
      ))}
    </div>
  );
};

export default SimpleLanguageSwitcher;
