import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import OptionItem from './OptionItem';

const OptionsList = memo(({ options }) => {
  const { t } = useTranslation();

  return (
    <div className="px-6 py-5">
      <h3 className="text-md font-medium text-gray-900 mb-3 flex items-center">
        <svg
          className="h-5 w-5 text-purple-500 mr-2"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
          <path
            fillRule="evenodd"
            d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
            clipRule="evenodd"
          />
        </svg>
        {t('optionsList.answerOptions', 'Answer Options')}
      </h3>

      <div className="space-y-4">
        {options.map(option => (
          <OptionItem key={option.optionId} option={option} />
        ))}
      </div>
    </div>
  );
});

OptionsList.displayName = 'OptionsList';

export default OptionsList;
