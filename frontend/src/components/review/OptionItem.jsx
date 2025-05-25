import { memo, useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const OptionItem = memo(({ option }) => {
  const { t } = useTranslation();

  const optionStyles = useMemo(() => {
    let containerClass = 'rounded-lg border p-4 transition-all ';

    if (option.selected && option.correct) {
      containerClass += 'border-green-300 bg-green-50';
    } else if (option.selected && !option.correct) {
      containerClass += 'border-red-300 bg-red-50';
    } else if (!option.selected && option.correct) {
      containerClass += 'border-yellow-300 bg-yellow-50';
    } else {
      containerClass += 'border-gray-200 bg-white';
    }

    const textClass =
      option.selected && !option.correct
        ? 'line-through opacity-75'
        : 'font-medium';

    return { containerClass, textClass };
  }, [option.selected, option.correct]);

  const StatusBadge = () => {
    if (option.selected) {
      if (option.correct) {
        return (
          <div className="flex items-center h-8 px-3 py-1 rounded-full bg-green-100 text-green-800 text-xs font-medium">
            <svg
              className="h-4 w-4 mr-1 text-green-600"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                clipRule="evenodd"
              />
            </svg>
            {t('optionItem.selectedCorrectly', 'Selected correctly')}
          </div>
        );
      }
      return (
        <div className="flex items-center h-8 px-3 py-1 rounded-full bg-red-100 text-red-800 text-xs font-medium">
          <svg
            className="h-4 w-4 mr-1 text-red-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          {t('optionItem.incorrectSelection', 'Incorrect selection')}
        </div>
      );
    } else if (option.correct) {
      return (
        <div className="flex items-center h-8 px-3 py-1 rounded-full bg-yellow-100 text-yellow-800 text-xs font-medium">
          <svg
            className="h-4 w-4 mr-1 text-yellow-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          {t('optionItem.correctAnswer', 'Correct answer')}
        </div>
      );
    }

    return null;
  };

  const IndicatorIcon = () => {
    if (option.selected) {
      if (option.correct) {
        return (
          <div className="flex items-center justify-center h-6 w-6 rounded-full bg-green-100 border-2 border-green-500">
            <svg
              className="h-4 w-4 text-green-600"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                clipRule="evenodd"
              />
            </svg>
          </div>
        );
      }
      return (
        <div className="flex items-center justify-center h-6 w-6 rounded-full bg-red-100 border-2 border-red-500">
          <svg
            className="h-4 w-4 text-red-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          </svg>
        </div>
      );
    } else if (option.correct) {
      return (
        <div className="flex items-center justify-center h-6 w-6 rounded-full bg-yellow-100 border-2 border-yellow-500">
          <svg
            className="h-4 w-4 text-yellow-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              clipRule="evenodd"
            />
          </svg>
        </div>
      );
    }

    return (
      <div className="flex items-center justify-center h-6 w-6 rounded-full bg-gray-100 border border-gray-300">
        <svg
          className="h-4 w-4 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z"
            clipRule="evenodd"
          />
        </svg>
      </div>
    );
  };

  return (
    <div className={optionStyles.containerClass}>
      <div className="flex justify-between items-start mb-2">
        <div className="flex-shrink-0 mr-3">
          <StatusBadge />
        </div>
      </div>

      <div className="flex items-start mt-2">
        <div className="flex-shrink-0 mt-0.5 mr-3">
          <IndicatorIcon />
        </div>

        <div className="flex-1">
          <div
            className={`text-gray-800 text-base ${optionStyles.textClass}`}
            dangerouslySetInnerHTML={{ __html: option.text }}
          />
          {option.description && option.description.length > 0 && (
            <p
              className={`mt-1 text-sm text-gray-500 ${option.selected && !option.correct ? 'line-through opacity-75' : ''}`}
            >
              {option.description}
            </p>
          )}
        </div>
      </div>
    </div>
  );
});

OptionItem.displayName = 'OptionItem';

export default OptionItem;
