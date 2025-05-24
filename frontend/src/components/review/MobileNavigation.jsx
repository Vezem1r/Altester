import { memo, useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { ReviewNavigationContext } from './ReviewNavigationContext';

const MobileNavigation = memo(
  ({ onPrevious, onNext, isFirstQuestion, isLastQuestion }) => {
    const { t } = useTranslation();
    const navigation = useContext(ReviewNavigationContext);

    const handlePrevious = onPrevious || navigation.goToPrevPage;
    const handleNext = onNext || navigation.goToNextPage;
    const firstQuestion =
      isFirstQuestion !== undefined
        ? isFirstQuestion
        : navigation.currentPage === 1;
    const lastQuestion =
      isLastQuestion !== undefined
        ? isLastQuestion
        : navigation.currentPage === navigation.totalPages;

    return (
      <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 md:hidden">
        <div className="flex justify-between">
          <button
            onClick={handlePrevious}
            disabled={firstQuestion}
            className="inline-flex items-center px-3 py-1.5 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50"
          >
            <svg
              className="mr-1.5 h-4 w-4"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                clipRule="evenodd"
              />
            </svg>
            {t('mobileNavigation.previous', 'Previous')}
          </button>
          <button
            onClick={handleNext}
            disabled={lastQuestion}
            className="inline-flex items-center px-3 py-1.5 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50"
          >
            {t('mobileNavigation.next', 'Next')}
            <svg
              className="ml-1.5 h-4 w-4"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>
      </div>
    );
  }
);

MobileNavigation.displayName = 'MobileNavigation';

export default MobileNavigation;
